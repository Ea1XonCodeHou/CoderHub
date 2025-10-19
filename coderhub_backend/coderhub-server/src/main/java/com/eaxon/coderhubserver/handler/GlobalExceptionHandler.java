package com.eaxon.coderhubserver.handler;

import com.eaxon.coderhubcommon.constant.MessageConstant;
import com.eaxon.coderhubcommon.exception.BaseException;
import com.eaxon.coderhubcommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        log.error("SQL异常：{}", message);
        
        if (message.contains("Duplicate entry")) {
            String[] splits = message.split(" ");
            String duplicateValue = splits[2];
            String msg = duplicateValue + MessageConstant.ACCOUNT_ALREADY_EXISTS;
            return Result.error(msg);
        } else {
            return Result.error("未知错误");
        }
    }
}

