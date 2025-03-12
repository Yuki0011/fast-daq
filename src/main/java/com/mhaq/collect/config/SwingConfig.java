package com.mhaq.collect.config;

import com.mhaq.collect.service.ILabItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.mhaq.collect.common.LocalCacheUtil.app;


@Configuration
public class SwingConfig {

    @Autowired
    private  ILabItemService labItemService;

    private final static String version = "V1.0";

    @Bean
    public void setTitle(){
        //String lastVersion = labItemService.getLastVersion();
        String title = "Patzn 监控信息采集"+version;
//        if (lastVersion!=null && !lastVersion.equals(version)) {
//            title = "当前版本"+version+"不是最新版本,请前往LIMS系统下载最新版本 "+lastVersion ;
//        }
        app.setTitle(title);
        app.setVisible(true);
    }



}
