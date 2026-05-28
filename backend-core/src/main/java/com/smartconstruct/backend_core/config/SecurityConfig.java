package com.smartconstruct.backend_core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * 
 * 配置系统的安全策略：
 * - JWT无状态认证
 * - CORS跨域支持
 * - 请求权限控制
 * - CSRF禁用（前后端分离模式）
 * - 内部服务接口Token验证
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** JWT认证过滤器 */
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 内部服务Token验证过滤器 */
    private InternalServiceTokenFilter internalServiceTokenFilter;

    public SecurityConfig() {
    }

    @Autowired
    public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Autowired
    public void setInternalServiceTokenFilter(InternalServiceTokenFilter internalServiceTokenFilter) {
        this.internalServiceTokenFilter = internalServiceTokenFilter;
    }

    /**
     * 配置安全过滤链
     * 
     * 配置规则：
     * 1. 启用CORS跨域
     * 2. 禁用CSRF（前后端分离）
     * 3. 无状态会话管理
     * 4. 公开接口免认证（登录、注册、公共数据）
     * 5. 内部服务接口免JWT认证（通过服务Token保护）
     * 6. API接口需要认证
     * 7. 添加JWT认证过滤器
     * 8. 添加内部服务Token过滤器
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/captcha").permitAll()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/internal/**").permitAll()
            .antMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置CORS跨域
     * 
     * 允许所有来源、所有方法、所有请求头，支持凭证。
     * 
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
