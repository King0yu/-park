package com.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 
 * 配置前后端分离项目的跨域访问
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器 - 小区停车场管理系统
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许来源（前端地址）
        config.addAllowedOriginPattern("*");
        
        // 允许携带认证信息（Cookie、Authorization 等）
        config.setAllowCredentials(true);
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的请求方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 暴露响应头（允许前端访问的响应头）
        config.addExposedHeader("Authorization");
        
        // 创建 URL 配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用跨域配置
        source.registerCorsConfiguration("/**", config);
        
        // 返回跨域过滤器
        return new CorsFilter(source);
    }
}
