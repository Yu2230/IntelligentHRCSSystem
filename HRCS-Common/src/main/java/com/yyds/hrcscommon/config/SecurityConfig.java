package com.yyds.hrcscommon.config;

import com.yyds.hrcscommon.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // JWT过滤器，用于解析token并设置认证信息
    private final HandlerMappingIntrospector introspector;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ 正确：禁用CSRF（JWT无状态认证不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ 正确：设置无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔥 关键配置：路径权限
                .authorizeHttpRequests(auth -> auth
                        // ⚠️ 问题根源：只放行了 /auth/login
                        .requestMatchers(
                                new MvcRequestMatcher(introspector, "/user/login"),
                                new AntPathRequestMatcher("/static/**")
                        ).permitAll()
                        .anyRequest().authenticated()  // 其他请求都需要认证
                )
                // ✅ 正确：JWT过滤器在用户名密码过滤器之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
