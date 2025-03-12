package com.mhaq.collect.shedule;


import com.mhaq.collect.handler.TXGreatSerialHandler;
import com.mhaq.collect.handler.TXSerialHandler;
import com.mhaq.collect.handler.TXZSerialHandler;
import com.mhaq.collect.handler.UnitSerialHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author xutao
 * @date 2021/3/9 1:42 下午
 * @description 海尔监控查询定时任务
 */
@Component
public class HaierMonitorShedule {

    private Logger logger = LoggerFactory.getLogger(HaierMonitorShedule.class);

    @Autowired
    private TXGreatSerialHandler txGreatSerialHandler;
    @Autowired
    private TXSerialHandler txSerialHandler;
    @Autowired
    private TXZSerialHandler txzSerialHandler;
    @Autowired
    private UnitSerialHandler unitSerialHandler;


    /**
     * 每2秒钟采集一次
     */
//    @Scheduled(cron = "*/2 * * * * ?")
//    @Async
//    public void excute() {
////        txGreatSerialHandler.getInstance();
////        txSerialHandler.getInstance();
////        txzSerialHandler.getInstance();
////        unitSerialHandler.getInstance();
//    }
}
