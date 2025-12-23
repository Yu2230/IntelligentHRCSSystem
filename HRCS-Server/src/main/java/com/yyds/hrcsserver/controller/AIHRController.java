package com.yyds.hrcsserver.controller;


import com.yyds.hrcsserver.service.AIHRService;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/Ai_HR")
@Api(tags = "AIHR接口")
@Slf4j
public class AIHRController {


    @Resource
    private AIHRService aiHRService;
    /**
     * 聊天
     *
     * @param memoryId 会话id
     * @param message  用户输入
     * @return
     */
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
        return aiHRService.chatStream(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
