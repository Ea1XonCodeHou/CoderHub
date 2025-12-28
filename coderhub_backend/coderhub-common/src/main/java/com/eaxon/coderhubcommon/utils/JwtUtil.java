package com.eaxon.coderhubcommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 适配 jjwt 0.12.x 新版本 API
 */
public class JwtUtil {
    
    /**
     * 生成jwt
     * 使用HS256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥（至少32个字符）
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return JWT token
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 确保密钥至少32字节（256位）
        String paddedKey = padKey(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(paddedKey.getBytes(StandardCharsets.UTF_8));
        
        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 构建JWT
        return Jwts.builder()
                .claims(claims)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥
     * @param token     加密后的token
     * @return Claims
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 确保密钥至少32字节（256位）
        String paddedKey = padKey(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(paddedKey.getBytes(StandardCharsets.UTF_8));
        
        // 解析JWT
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 确保密钥至少32字节
     */
    private static String padKey(String key) {
        if (key.length() >= 32) {
            return key;
        }
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < 32) {
            sb.append(key);
        }
        return sb.substring(0, 32);
    }
}
