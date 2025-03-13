package com.mhaq.collect.handler;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.mhaq.collect.service.ILabMonitorDataService;


;

/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 串口监听 TX(大)
 */

@Service
@PropertySource("classpath:application.yml")
public class TXGreatSerialHandler  {

    @Autowired
    private ILabMonitorDataService labMonitorDataService;

    private Logger logger = LoggerFactory.getLogger(TXGreatSerialHandler.class);
   

    @Resource(name = "defaultThreadPool")
    private ThreadPoolTaskExecutor executor;

   

}
