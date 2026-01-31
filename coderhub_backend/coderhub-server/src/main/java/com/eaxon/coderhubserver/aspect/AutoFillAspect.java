package com.eaxon.coderhubserver.aspect;

import com.eaxon.coderhubcommon.constant.AutoFillConstant;
import com.eaxon.coderhubcommon.enumeration.OperationType;
import com.eaxon.coderhubserver.annotation.AutoFill;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //指定切入点：哪些类/哪些方法
    //下面这句指定了mapper类中加入autofill注解的会被拦截
    @Pointcut("execution(* com.eaxon.coderhubserver.mapper.*.*(..)) && @annotation(com.eaxon.coderhubserver.annotation.AutoFill)")
    public void autoFillPointCut(){
        //下面选取通知方法。这里适合前置通知，在更新/插入操作之前进行注解生效
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充");
        //实现逻辑：获取当前被拦截方法的参数：先获取数据库操作类型
        MethodSignature methodSignature=(MethodSignature) joinPoint.getSignature();
        AutoFill autoFill=methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType=autoFill.value();
        //获取当前被拦截方法的参数，实体类型
        Object args[]=joinPoint.getArgs();
        if(args==null||args.length==0){
            return;
        }

        Object entityArg=args[0];
        LocalDateTime currentTime=LocalDateTime.now();

        //根据当前方法类型，进行不同的赋值
        if(operationType==OperationType.INSERT){
            //为2个公共字段赋值
            Method setCreateTimeMethod=entityArg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setUpdateTimeMethod=entityArg.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

            setCreateTimeMethod.invoke(entityArg,currentTime);
            setUpdateTimeMethod.invoke(entityArg,currentTime);
        }else if(operationType==OperationType.UPDATE){
            //为1个公共字段赋值
            Method setUpdateTimeMethod=entityArg.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setUpdateTimeMethod.invoke(entityArg,currentTime);
        }
    }
}
