package com.yyds.hrcsserver.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcscommon.tools.ToolResult;
import com.yyds.hrcscommon.utils.JwtUtils;
import com.yyds.hrcspojo.ToolCall;
import com.yyds.hrcsserver.AIHRServiceWrapper;
import com.yyds.hrcsserver.ToolRouter;
import com.yyds.hrcsserver.service.AIHRService;
import com.yyds.hrcsserver.service.UserService;
import com.yyds.hrcspojo.search.EmployeeSearchResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;



@RestController
@RequestMapping("/Ai_HR")
@Tag(name = "AIHR接口", description = "AIHR相关接口")
@Slf4j
public class AIHRController {

    @Resource
    private AIHRServiceWrapper aiHRServiceWrapper;  // 使用包装器而非原始服务
    @Resource
    private UserService userService;

    @Resource
    private ToolRouter toolRouter;
    @Resource
    private ObjectMapper objectMapper;

    @Operation(summary = "聊天", description = "SSE聊天接口")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam("memoryId") String memoryId,
                           @RequestParam("message") String message,
                           @RequestParam(required = false) String token) {

        // 立即创建 emitter
        SseEmitter emitter = new SseEmitter(60_000L);

        // 异步处理（不依赖主线程）
        CompletableFuture.runAsync(() -> {
            try {
                String systemPrompt = String.format("""
                尊敬的用户您好，我是你的专属客服助手，
                帮助用户解答相关问题和给出促进个人成长的相关路线。
                请保持专业、温暖、鼓励性的沟通风格。
                当前对话ID: %s
                """, memoryId);

                emitter.send(SseEmitter.event()
                        .name("system")  // 事件名为 system
                        .data(systemPrompt));
                // 调用安全包装器（内部已捕获异常）
                Flux<String> safeFlux = aiHRServiceWrapper.safeChatStream(memoryId, message);

                //订阅并发送
                safeFlux.subscribe(
                        chunk -> sendChunk(emitter, chunk),
                        error -> sendError(emitter, "流错误: " + error.getMessage()),
                        () -> completeEmitter(emitter)
                );

            } catch (Exception e) {
                // 捕获 Controller 层剩余异常
                log.error("Controller 层未知异常", e);
                sendError(emitter, "系统致命错误: " + e.getMessage());
            }
        });

        return emitter;
    }

    /**
     * 员工搜索（AI客服辅助）
     * @param keyword 关键字（姓名/用户名/邮箱/手机号/工号）
     */
    @Operation(summary = "员工搜索", description = "根据关键字搜索员工基础信息")
    @GetMapping("/searchEmployee")
    public Result<EmployeeSearchResultVO> searchEmployee(@RequestParam("keyword") String keyword) {
        EmployeeSearchResultVO result = userService.searchEmployees(keyword);
        return Result.getSuccessResult(result);
    }

    @GetMapping("/chatMany")
    public SseEmitter chat(@RequestParam String message,
                           @RequestParam String token) {

        Long userId = JwtUtils.getUserId(token); // 从token解析

        SseEmitter emitter = new SseEmitter(60_000L);

        CompletableFuture.runAsync(() -> {

            try {

                // 1️⃣ 规则识别（不是AI）
                ToolCall toolCall = aiHRServiceWrapper.analyze(message, userId);

                if (toolCall != null) {

                    // 2️⃣ 执行工具
                    ToolResult result = toolRouter.execute(
                            toolCall.getTool(),
                            toolCall.getArgs()
                    );

                    // 3️⃣ Jackson 转 JSON
                    String json = objectMapper.writeValueAsString(result);

                    emitter.send(json);

                } else {

                    emitter.send("我暂时无法理解您的问题");

                }

                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send("系统错误：" + e.getMessage());
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    // 提取辅助方法（确保响应提交）
    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().data(chunk));
        } catch (IOException e) {
            log.error("发送 chunk 失败", e);
            emitter.completeWithError(e);
        }
    }

    private void sendError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMsg));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("complete").data("done"));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
