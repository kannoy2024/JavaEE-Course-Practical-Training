package com.example.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建 CORS 配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 2. 设置允许的来源（生产环境建议指定具体域名，如 "http://localhost:5173"）
        corsConfiguration.addAllowedOriginPattern("*"); // 允许所有来源（开发环境可用，生产环境不推荐）

        // 3. 设置允许的请求方法（GET, POST, PUT, DELETE, OPTIONS 等）
        corsConfiguration.addAllowedMethod("*");

        // 4. 设置允许的请求头（如 Content-Type, Authorization 等）
        corsConfiguration.addAllowedHeader("*");

        // 5. 是否允许携带 Cookie（如果前端需要携带 Cookie，必须设置为 true）
        corsConfiguration.setAllowCredentials(true);

        // 6. 设置预检请求的缓存时间（单位：秒），减少预检请求次数
        corsConfiguration.setMaxAge(3600L);

        // 7. 创建 CORS 配置源，并注册到所有路径（/**）
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        // 8. 返回 CorsFilter
        return new CorsFilter(corsConfigurationSource);
    }
}