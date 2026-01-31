package com.eaxon.coderhubcommon.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code;//1表示成功
    private String msg;
    private T data;//具体数据

    //第一个T表示类型参数声明，告诉编译器定义泛型方法
    //第二个T表示类型参数使用 <T> Result<T> success()表示定义一个泛型方法，返回一个Result<T>类型的对象
    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setCode(1);
        return result;
    }

    public static <T> Result<T> success(T object){
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setData(object);
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }
}
