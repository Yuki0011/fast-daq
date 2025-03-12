package com.mhaq.collect.shedule;


import com.mhaq.collect.entity.enums.LabTypeEnum;
import com.mhaq.collect.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author xutao
 * @date 2021/3/9 1:42 下午
 * @description 海尔数据采集定时任务
 */
@Component
public class HaierDataCollectShedule {

    private Logger logger = LoggerFactory.getLogger(HaierDataCollectShedule.class);


    @Autowired
    private DataCollectHandler dataCollectHandler;


    /**
     * 每2秒钟采集一次
     */
//    @Scheduled(cron = "*/2 * * * * ?")
//    @Async
    public void excute() {
        logger.error("自动结束程序");
        dataCollectHandler.handleData();
    }
}
