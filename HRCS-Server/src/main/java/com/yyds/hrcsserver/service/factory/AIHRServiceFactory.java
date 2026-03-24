package com.yyds.hrcsserver.service.factory;


import com.yyds.hrcsserver.service.AIHRService;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
@Configuration
@Slf4j
public class AIHRServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private  StreamingChatModel qwenStreamingChatModel;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private ContentRetriever contentRetriever;


    @Bean
    public AIHRService AIHRService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造 AI Service
        AIHRService service = AiServices.builder(AIHRService.class)
                .chatModel(chatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever)
                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return service;
    }

}*/@Configuration
@Slf4j
public class AIHRServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private McpToolProvider mcpToolProvider;
    @Resource
    private ContentRetriever contentRetriever;


    @Bean
    public AIHRService AIHRService() {
        try {
            log.info("正在创建 AIHRService...");

            // 验证依赖不为空
            if (chatModel == null || qwenStreamingChatModel == null) {
                throw new IllegalStateException("ChatModel 依赖未注入");
            }

            AIHRService service = AiServices.builder(AIHRService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(qwenStreamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(contentRetriever)
                    .toolProvider(mcpToolProvider)
                    .build();

            log.info("AIHRService 创建成功！");
            return service;

        } catch (Exception e) {
            log.error("AIHRService 创建失败", e);
            //返回 null 或抛出运行时异常
            throw new RuntimeException("AIHRService 初始化失败: " + e.getMessage());
        }
    }
}

