package com.yyds.hrcscommon.config;

import com.yyds.hrcscommon.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //CSRF 禁用
                .csrf(csrf -> csrf.disable())

                // 会话无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 权限配置
                .authorizeHttpRequests(auth -> auth
                        // 放行 OPTIONS 预检请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 登录注册接口放行
                        .requestMatchers("/user/login", "/user/register", "/user/sendCode").permitAll()
                        .requestMatchers(HttpMethod.GET, "/Ai_HR/chat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/Ai_HR/chat/**").permitAll()
                        .requestMatchers("/user/uploadAvatar").permitAll()

                        // 其余请求需要认证
                        .anyRequest().authenticated()
                )

                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 修正为前端实际端口
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // 明确指定允许的 methods（必须包含 OPTIONS）
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 明确指定允许的 headers（不要用 *，与 credentials 一起用可能有问题）
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Cache-Control"
        ));

        // 暴露的响应头（如果需要前端访问）
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        //允许凭证（cookie）
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/ **", configuration);
        return source;
    }
}