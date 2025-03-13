package com.mhaq.collect.handler;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.mhaq.collect.service.ILabMonitorDataService;

/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 串口监听 单元机
 */
@PropertySource("classpath:application.yml")
@Component
public class UnitSerialHandler {

    private static final int BIT_RATE = 1200;
    private static InputStream in;
    private static OutputStream out;

    @Autowired
    private ILabMonitorDataService labMonitorDataService;
    @Resource(name = "defaultThreadPool")
    private ThreadPoolTaskExecutor executor;

   


}
