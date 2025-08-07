package com.xxl.job.core.util;

/**
 * @description: 加密异常处理类
 * @Author: copyed
 * @date: 2019-12-16 16:17
 **/
public class CryptException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public CryptException(String message){
        super(message);
    }
    
    
    public CryptException(String message, Throwable e){
        super(message,e);
    }

}
