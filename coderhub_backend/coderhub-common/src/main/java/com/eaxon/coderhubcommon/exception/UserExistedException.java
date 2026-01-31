package com.eaxon.coderhubcommon.exception;

/**
 * 用户已存在异常
 */
public class UserExistedException extends BaseException {
    
    public UserExistedException() {
        super();
    }
    
    public UserExistedException(String message) {
        super(message);
    }
}
