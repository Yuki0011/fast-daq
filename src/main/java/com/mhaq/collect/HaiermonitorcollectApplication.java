package com.mhaq.collect;

import com.mhaq.collect.common.DesktopApp;
import com.mhaq.collect.common.LocalCacheUtil;
import com.mhaq.collect.entity.LabItem;
import com.mhaq.collect.service.ILabItemService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.mhaq.collect.mapper")
public class HaiermonitorcollectApplication {

    public static void main(String[] args) {
        if (isPortUsing("localhost", 8080) || isPortUsing("localhost", 8081)) {
            return;
        }
        LocalCacheUtil.app = new DesktopApp();
        SpringApplication.run(HaiermonitorcollectApplication.class, args);
    }


    public static boolean isPortUsing(String host, int port) {
        boolean flag = false;
        try {
            InetAddress theAddress = InetAddress.getByName(host);
            Socket socket = new Socket(theAddress, port);
            flag = true;
        } catch (Exception e) {

        }
        return flag;
    }

}
