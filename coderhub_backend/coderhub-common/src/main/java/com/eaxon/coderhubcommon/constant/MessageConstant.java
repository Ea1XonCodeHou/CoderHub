package com.eaxon.coderhubcommon.constant;

public class MessageConstant {
    
    //1.账号密码相关常量 
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_ALREADY_EXISTS = "账号已存在";    
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String PASSWORD_ERROR = "密码错误";

    public static final String ACCOUNT_OR_PASSWORD_ERROR = "账号或密码错误";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String DEFAULT_USERNAME_PREFIX="user_";
    public static final String DEFAULT_AVATAR_URL="https://eaxon-bucket.oss-cn-wuhan-lr.aliyuncs.com/avatars/8_3bccc85c-79ae-4f81-821b-00aaa2d04719.jpeg";

    public static final Integer USER_DEFAULT=0;
    public static final Integer USER_VIP=1;
    public static final Integer USER_SUPER_VIP=2;

    public static final String ROLE_USER="USER";
    public static final String ROLE_ADMIN="ADMIN";
}
