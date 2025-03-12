package com.mhaq.collect.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.mhaq.collect.entity.LabMonitor;
import com.mhaq.collect.handler.TXGreatSerialHandler;
import com.mhaq.collect.handler.TXSerialHandler;
import com.mhaq.collect.handler.TXZSerialHandler;
import com.mhaq.collect.handler.UnitSerialHandler;
import com.mhaq.collect.service.ILabMonitorDataService;
import com.mhaq.collect.service.ILabMonitorService;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mhaq.collect.common.LocalCacheUtil.*;


/**
 * @author xutao
 * @date 2021/4/11 9:41 下午
 * @description haier
 */
@RestController
@RequestMapping("/monitor")
public class HaierMonitorController {

    @Autowired
    private TXGreatSerialHandler txGreatSerialHandler;
    @Autowired
    private TXSerialHandler txSerialHandler;
    @Autowired
    private TXZSerialHandler txzSerialHandler;
    @Autowired
    private UnitSerialHandler unitSerialHandler;
    @Autowired
    private ILabMonitorService labMonitorService;
    @Autowired
    private ILabMonitorDataService labMonitorDataService;





    @GetMapping("/test")
    public void test(String type, String port) {
            if ("TX(大)".equals(type)) {
                 txGreatSerialHandler.init(port);
            }
            if ("TX(小)".equals(type)) {
                 txSerialHandler.init(port);
            }
            if ("TXZ".equals(type)) {
                 txzSerialHandler.init(port);
            }
            if ("单元机".equals(type)) {
               unitSerialHandler.init(port);
            }
    }












    @GetMapping("/ip")
    public void success(HttpServletRequest request,
                        HttpServletResponse response) {
        AbstractWrapper query = new QueryWrapper();
        query.eq("ip", getHostIP());
        query.last("limit 4");
        List<LabMonitor> list = labMonitorService.list(query);
        // 将数据条数返回
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(e -> {
                Date date = new Date();
                int count = labMonitorDataService.countByQuery(e, date);
                e.setStatus(formatDate(e.getBeginDate()) + " - " + formatDate(date) + " 共采集" + count + "条数据" + " " + e.getStatus());
            });
        }
        JSONObject aapJson = new JSONObject();
        aapJson.put("list", list);
        String jj = "successCallback(" + aapJson + ")";
        try {
            InputStream is = new ByteArrayInputStream(
                    jj.toString().getBytes("UTF-8"));
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @GetMapping("/control")
    public boolean control(String id,String lab, String name, String type, String port, String status) {
        String ip = getHostIP();
        AbstractWrapper query = new QueryWrapper();
        query.eq("lab", lab);
        query.eq("ip", ip);
        query.last("limit 1");
        LabMonitor preMonitor = labMonitorService.getMonitor(query);
        if (null == preMonitor) {
            preMonitor = new LabMonitor();
            preMonitor.setLab(lab);
            preMonitor.setIp(ip);
            preMonitor.setBeginDate(new Date());
        }
        preMonitor.setItemId(Long.parseLong(id));
        preMonitor.setType(type);
        preMonitor.setPort(port);
        if (!name.equals(preMonitor.getItem()) || !lab.equals(preMonitor.getLab()) || !type.equals(preMonitor.getType())) {
            // 重新做实验 重新开始
            preMonitor.setBeginDate(new Date());
        }
        preMonitor.setItem(name);
        boolean result = false;
        if ("开始".equals(status)) {
            if ("TX(大)".equals(type)) {
                result = txGreatSerialHandler.init(port);
            }
            if ("TX(小)".equals(type)) {
                result = txSerialHandler.init(port);
            }
            if ("txz".equals(type)||"TXZ".equals(type)) {
                result = txzSerialHandler.init(port);
            }
            if ("单元机".equals(type)) {
                result = unitSerialHandler.init(port);
            }
            preMonitor.setStatus("监控中");
        }
        if ("结束".equals(status)) {
            if ("TX(大)".equals(type)) {
                result = txGreatSerialHandler.close();
            }
            if ("TX(小)".equals(type)) {
                result = txSerialHandler.close();
            }
            if ("TXZ".equals(type)) {
                result = txzSerialHandler.close();
            }
            if ("单元机".equals(type)) {
                result = unitSerialHandler.close();
            }
            preMonitor.setStatus("停止");
        }
        if (result) {
            labMonitorService.saveOrUpdate(preMonitor);
            labMap.put(type, lab);
            monitorMap.put(lab, name);
            itemIdMap.put("itemId",Long.parseLong(id));
            return true;
        }
        return false;
    }


    private String getHostIP() {
        String tempIP = "127.0.0.1";
        try {
            if (isIpv4(InetAddress.getLocalHost().getHostAddress()))
                tempIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            Enumeration<InetAddress> addrs;
            while (networks.hasMoreElements()) {
                addrs = networks.nextElement().getInetAddresses();
                while (addrs.hasMoreElements()) {
                    ip = addrs.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && ip.isSiteLocalAddress()
                            && !ip.getHostAddress().equals(tempIP)) {
                        if (isIpv4(ip.getHostAddress()))
                            return ip.getHostAddress();
                    }
                }
            }
            return tempIP;
        } catch (Exception e) {
            System.out.println("获取IP地址抛出异常");
            throw new RuntimeException(e);

        }
    }

    public boolean isIpv4(String ipAddress) {

        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();

    }


    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

}
