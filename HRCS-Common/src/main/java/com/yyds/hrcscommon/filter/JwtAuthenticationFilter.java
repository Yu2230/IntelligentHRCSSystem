package com.yyds.hrcscommon.filter;

import com.yyds.hrcscommon.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("请求地址: {}", requestURI);
        //  放行 SSE 路径
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        System.err.println("请求路径: " + path + ", Method: " + request.getMethod());
        if (path.startsWith("/Ai_HR/chat")) {
            chain.doFilter(request, response);
            return;
        }
        // 放行登录注册接口和 SSE 聊天接口
        if (requestURI.startsWith("/user/login")
                || requestURI.startsWith("/user/register")
                || requestURI.startsWith("/user/sendCode")
                || requestURI.startsWith("/Ai_HR/chat")) {
            chain.doFilter(request, response);
            return;
        }

        // 获取 token
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 解析 token 获取 userId
                String userId = JwtUtils.getUserID(token);
                request.setAttribute("userID", userId);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                log.error("Token 解析失败: {}", e.getMessage());
                // 返回 401，不阻止 SSE
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // 没有 token 且接口需要认证时
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }
}
