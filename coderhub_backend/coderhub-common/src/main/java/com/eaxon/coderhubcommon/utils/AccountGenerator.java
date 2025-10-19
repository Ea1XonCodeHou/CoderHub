package com.eaxon.coderhubcommon.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 账户编号生成器
 * 生成8位随机数字作为账户编号
 */
public class AccountGenerator {

    /**
     * 账户编号起始值
     */
    private static final int MIN_ACCOUNT = 10000000;
    
    /**
     * 账户编号最大值
     */
    private static final int MAX_ACCOUNT = 99999999;

    /**
     * 生成8位随机账户编号
     * 范围：10000000 ~ 99999999
     *
     * @return 8位账户编号字符串
     */
    public static String generate() {
        // 使用ThreadLocalRandom提高并发性能
        int accountNumber = ThreadLocalRandom.current().nextInt(MIN_ACCOUNT, MAX_ACCOUNT + 1);
        return String.valueOf(accountNumber);
    }

    /**
     * 生成指定前缀的账户编号
     * 例如：前缀1，生成10000000-19999999范围内的账户
     *
     * @param prefix 前缀（1-9）
     * @return 8位账户编号字符串
     */
    public static String generateWithPrefix(int prefix) {
        if (prefix < 1 || prefix > 9) {
            throw new IllegalArgumentException("前缀必须在1-9之间");
        }
        
        int min = prefix * 10000000;
        int max = (prefix + 1) * 10000000 - 1;
        int accountNumber = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.valueOf(accountNumber);
    }

    /**
     * 生成指定数量的账户编号（用于批量生成）
     *
     * @param count 生成数量
     * @return 账户编号数组
     */
    public static String[] generateBatch(int count) {
        if (count <= 0 || count > 1000) {
            throw new IllegalArgumentException("生成数量必须在1-1000之间");
        }
        
        String[] accounts = new String[count];
        for (int i = 0; i < count; i++) {
            accounts[i] = generate();
        }
        return accounts;
    }
}

