package com.eaxon.coderhubcommon.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * 用于密码加密
 */
public class MD5Util {

    /**
     * 对字符串进行MD5加密
     *
     * @param source 原始字符串
     * @return MD5加密后的32位小写字符串
     */
    public static String encrypt(String source) {
        if (source == null || source.length() == 0) {
            return null;
        }
        
        try {
            // 获取MD5加密对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // 进行加密
            byte[] bytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
            
            // 将字节数组转换为16进制字符串
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    result.append("0");
                }
                result.append(hex);
            }
            
            return result.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 验证密码
     *
     * @param rawPassword  原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encrypt(rawPassword).equals(encodedPassword);
    }
}

