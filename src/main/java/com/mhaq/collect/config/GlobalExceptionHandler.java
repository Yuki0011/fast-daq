package com.mhaq.collect.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

import static com.mhaq.collect.common.LocalCacheUtil.toYearMonthDayHMS;
import static com.mhaq.collect.common.LocalCacheUtil.txt;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e){
        txt.append(toYearMonthDayHMS(new Date()));
        txt.append(" 异常："+e.getMessage()+"\n");
        txt.setCaretPosition(txt.getDocument().getLength());
    }
}
