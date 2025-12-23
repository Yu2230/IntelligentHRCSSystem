package com.yyds.hrcscommon.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String KEY;

    @Bean
    public McpToolProvider mcpToolProvider() {


        //MCP服务通讯
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("http://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        McpToolProvider mcpToolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        return mcpToolProvider;
    }

}
