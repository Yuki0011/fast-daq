package com.mhaq.collect.shedule;


import com.mhaq.collect.entity.enums.LabTypeEnum;
import com.mhaq.collect.handler.DataCollectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author xutao
 * @date 2021/3/9 1:42 下午
 * @description 海尔数据采集定时任务
 */
@Component
public class HaierDataInsertCollectShedule {

    private Logger logger = LoggerFactory.getLogger(HaierDataInsertCollectShedule.class);

    @Autowired
    private DataCollectHandler dataCollectHandler;


    /**
     * 每5秒钟插入一条
     */
    //@Scheduled(cron = "*/5 * * * * ?")
    public void excute() {
        logger.info(LabTypeEnum.D_HC_HP_10.getDisplay() + "开始插入数据...");
        dataCollectHandler.insertData();
    }
}
