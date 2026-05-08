package com.smartconstruct.backend_core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * 提供 JWT Token 的生成、解析和验证功能。
 *
 * 配置项：
 * - jwt.secret: 签名密钥（默认256位）
 * - jwt.expiration: 过期时间（默认24小时，单位毫秒）
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥 */
    @Value("${jwt.secret:smartconstruct-default-secret-key-min-256bits!!}")
    private String secret;

    /** Token 过期时间（毫秒） */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * 获取签名密钥
     *
     * 使用 HMAC-SHA 算法生成密钥。
     *
     * @return SecretKey 密钥对象
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * 构建包含用户名、签发时间、过期时间的 Token。
     *
     * @param username 用户名（主题）
     * @return JWT Token 字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * 验证并解析 Token，提取用户名（主题）。
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 验证 JWT Token
     *
     * 检查 Token 是否有效（签名正确且未过期）。
     *
     * @param token JWT Token 字符串
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
