package com.mhaq.collect.handler;

import com.mhaq.collect.entity.LabMonitorData;
import com.mhaq.collect.service.ILabMonitorDataService;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;


import static com.mhaq.collect.common.BiteUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.*;

/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 串口监听 单元机
 */
@PropertySource("classpath:application.yml")
@Component
public class UnitSerialHandler implements SerialPortEventListener {

    private static final int BIT_RATE = 1200;
    public static final int DATA_BITS = SerialPort.DATABITS_8;
    public static final int STOP_BIT = SerialPort.STOPBITS_1;
    public static final int PARITY_BIT = SerialPort.PARITY_EVEN;
    private static SerialPort serialPort;
    private static InputStream in;
    private static OutputStream out;

    @Autowired
    private ILabMonitorDataService labMonitorDataService;
    @Resource(name = "defaultThreadPool")
    private ThreadPoolTaskExecutor executor;

    public boolean init(String port) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");                txt.append(toYearMonthDayHMS(new Date()));
                txt.append(" 异常：Port is currently in use"+"\n");
                txt.setCaretPosition(txt.getDocument().getLength());
            } else if (portIdentifier.getPortType() == 1) {
                serialPort = (SerialPort) portIdentifier.open(port, 1000);
                serialPort.setSerialPortParams(BIT_RATE, DATA_BITS, STOP_BIT, PARITY_BIT);
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
            } else {
                txt.append(toYearMonthDayHMS(new Date()));
                txt.append(" 异常：Only serial ports are handled by this example."+"\n");
                txt.setCaretPosition(txt.getDocument().getLength());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void send(String message) {
        try {
            byte[] bytes = hexStrToByteArray(message);
            out.write(bytes);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                receive();
                break;
        }
    }

    public void receive() {
        try {
            int size = in.available();
            if (0 < size) {
                byte[] buffer = new byte[size];
                in.read(buffer);
                Date now = new Date();
                executor.execute(() -> {
                    System.out.println(now+" "+byteArrayToString(buffer));
                    if (checkDate(buffer)) {
                        handleResponseData(buffer, now);
                    }
                });
            }
            // 休眠
            if(size==8){
                Thread.sleep(905);
            }else{
                Thread.sleep(680);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean checkDate(byte[] copyValue) {
        if (copyValue.length == 8) {
            // 判断是不是03
            if (!"03".equals(returnHex(copyValue[1]))) {
                return false;
            }
            // CRC校验
            byte[] jy = Arrays.copyOfRange(copyValue, 0, 6);
            byte[] last = Arrays.copyOfRange(copyValue, 6, 8);
            if (!byteArrayToString(last).equals(getCRC(jy))) {
                return false;
            }
        } else {
            // CRC校验(包含发送接受)
            byte[] jy = Arrays.copyOfRange(copyValue, 0, copyValue.length - 2);
            byte[] last = Arrays.copyOfRange(copyValue, copyValue.length - 2, copyValue.length);
            if (!byteArrayToString(last).equals(getCRC(jy))) {
                return false;
            }
        }
        return true;
    }


    public void handleResponseData(byte[] copyValue, Date now) {
        // 先处理问
        //   if (copyValue.length == 8) {
        // 判断是不是03
//            if (!"03".equals(returnHex(copyValue[1]))) {
//                return;
//            }
//            // CRC校验
//            byte[] jy = Arrays.copyOfRange(copyValue, 0, 6);
//            byte[] last = Arrays.copyOfRange(copyValue, 6, 8);
//            if (!byteArrayToString(last).equals(getCRC(jy))) {
//                return;
//            }
        //         modbus.put("pre", copyValue);
        // } else {
//        byte[] order = Arrays.copyOfRange(copyValue, 0, 8);
//        byte[] answer = Arrays.copyOfRange(copyValue, 8, copyValue.length);
//        modbus.put("pre", order);
//
        if (copyValue.length == 8) {
            modbus.put("pre", copyValue);
            return;
        }
        byte[] order = modbus.get("pre");
        if (null == order) {
            return;
        }
//        // CRC校验
//        byte[] jy = Arrays.copyOfRange(copyValue, 0, copyValue.length - 2);
//        byte[] last = Arrays.copyOfRange(copyValue, copyValue.length - 2, copyValue.length);
//        if (!byteArrayToString(last).equals(getCRC(jy))) {
//            return;
//        }
        // 起始值
        LabMonitorData monitorData = new LabMonitorData();
       int begin = hexStringToInt(byteArrayToString(Arrays.copyOfRange(order, 2, 4)))+1;
       // int begin = 2000;
        int end = begin + hexStringToInt(byteArrayToString(Arrays.copyOfRange(order, 4, 6)))+1;
        String result = byteArrayToString(copyValue);
        monitorData.setTimePoint(now);
        monitorData.setLabRoomGuid(monitorMap.get("单元机"));
        monitorData.setItem(labMap.get(monitorData.getLabRoomGuid()));
        monitorData.setType("单元机");
        monitorData.setT2201(getValFromBegin(result, begin, end, 2004));
        monitorData.setT2202(getValFromBegin(result, begin, end, 2005));
        monitorData.setT2203(getValFromBegin(result, begin, end, 2008));

        monitorData.setT2204(wd(getValFromBegin(result, begin, end, 2022)));
        monitorData.setT2205(wd(getValFromBegin(result, begin, end, 2023)));
        monitorData.setT2206(wd(getValFromBegin(result, begin, end, 2020)));
        monitorData.setT2207(wd(getValFromBegin(result, begin, end, 2021)));
        monitorData.setT2208(dl(getValFromBegin(result, begin, end, 2003)));
        monitorData.setT2209(dl(getValFromBegin(result, begin, end, 2001)));
        monitorData.setT2210(wd(getValFromBegin(result, begin, end, 2024)));
        monitorData.setT2211(wd(getValFromBegin(result, begin, end, 2019)));
        monitorData.setItemId(itemIdMap.get("itemId"));
        labMonitorDataService.save(monitorData);
        txt.append(toYearMonthDayHMS(new Date())+": "+monitorData.toUnit()+"\n");
        txt.setCaretPosition(txt.getDocument().getLength());
        modbus.put("pre", null);
    }

    private BigDecimal wd(BigDecimal w){
        BigDecimal subtract = w.subtract(BigDecimal.valueOf(200));
        return subtract.divide(BigDecimal.valueOf(10),1,BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal dl(BigDecimal w){
        return w.divide(BigDecimal.valueOf(10),1,BigDecimal.ROUND_HALF_UP);
    }




    public BigDecimal getValFromBegin(String val, int begin, int end, int aim) {
        if (begin > aim || aim > end) {
            return null;
        }
        return BigDecimal.valueOf(hexStringToInt(val.substring(6 + (aim - begin) * 4, 6 + (aim - begin + 1) * 4)));
    }

    public static BigDecimal getValFromBegin2(String val, int begin, int end, int aim) {
        if (begin > aim || aim > end) {
            return null;
        }
        return BigDecimal.valueOf(hexStringToInt(val.substring(6 + (aim - begin) * 4, 6 + (aim - begin + 1) * 4)));
    }


    public boolean close() {
        if (null == in || null == out || null == serialPort) {
            return true;
        }
        try {
            in.close();
            out.close();
            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            serialPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


}
