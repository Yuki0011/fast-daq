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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.mhaq.collect.common.BiteUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.txt;

/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 串口监听 TXZ    负数特殊处理
 */
@PropertySource("classpath:application.yml")
@Component
public class TXZSerialHandler implements SerialPortEventListener {

    private static final int BIT_RATE = 19200;
    public static final int DATA_BITS = SerialPort.DATABITS_8;
    public static final int STOP_BIT = SerialPort.STOPBITS_1;
    public static final int PARITY_BIT = SerialPort.PARITY_EVEN;
    private static SerialPort serialPort;
    private static InputStream in;
    private static OutputStream out;

    //private static ThreadLocal<StringBuilder>  dataCache = new ThreadLocal<>();

    private int maxLength = 100000;

    private StringBuilder dataCache = new StringBuilder();


    @Resource(name = "singgleThreadPool")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private ILabMonitorDataService labMonitorDataService;


    public boolean init(String port) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
            if (portIdentifier.isCurrentlyOwned()) {
                txt.append(toYearMonthDayHMS(new Date()));
                txt.append(" 异常：Port is currently in use" + "\n");
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
                txt.append(" 异常：Only serial ports are handled by this example." + "\n");
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
            byte[] buffer = new byte[size];
            in.read(buffer);
            executor.execute(() -> {
                parseDatePro(buffer);
            });
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseDate(byte[] dataArr) {
        // String originalTxt = "34 C7 32 80 52C7 32 80 00 00 00 01 00 00 00 00 00 00 00 01 00 00 80 0434 C7 32 81 51C7 32 81 00 00 00 01 01 00 00 00 00 00 00 01 00 00 80 0234 C7 33 80 51C7 33 80 01 D6 01 D6 00 6D 00 00 01 D6 01 D6 00 6D 00 00 00 0C 00 0C 03 83 03 84 00 3F 00 00 0E C4 80 00 0B 6D 80 00 D0 FC FF FF 18 39 80 00 00 02 80 00 80 00 80 00 80 00 80 00 80 00 80 00 8434 C7 33 81 50C7 33 81 01 D6 01 D6 01 D6 00 00 01 D6 01 D6 01 D6 00 00 00 0C 00 0C 03 84 03 85 00 3F 00 3F 0E C4 0E C4 0C E4 0C E4 D0 FC D0 FC 1D 69 1B A9 82 02 80 00 80 00 80 00 80 00 80 00 80 00 80 00 6B5678".replace(" ","");
        // long start = System.currentTimeMillis();
        // System.out.println("o:"+byteArrayToString(dataArr));
        dataCache.append(byteArrayToString(dataArr));
        //StringBuilder dataCache = new StringBuilder(originalTxt);
        // StringBuilder dataCache = new StringBuilder(buffer);
        //System.out.println(dataCache.toString());
        loopRemoveAsk();

        List<String> c731 = Arrays.asList(
                "C73180",
                "C73181",
                "C73182",
                "C73183"
        );

        boolean jx = true;
        while (jx) {
            int i = 0;
            int a = 80;
            for (String s : c731) {
                int b1 = dataCache.indexOf(s);
                if (b1 != -1 && dataCache.length() > b1 + 240) {
                    String result = dataCache.substring(b1, b1 + 240);
                   // System.out.println("S: " + result);
                    if (checkSum(result)) {
                        LabMonitorData data = c731(result);
                        data.setT2299(BigDecimal.valueOf(a));
                        data.setItemId(itemIdMap.get("itemId"));
                        labMonitorDataService.save(data);
                        txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                        txt.setCaretPosition(txt.getDocument().getLength());
                        dataCache.delete(b1, b1 + 240);
                    }
                } else {
                    i++;
                }
                a++;
            }
            if (i == 4) {
                jx = false;
            }
        }


        List<String> c732 = Arrays.asList(
                "C73280",
                "C73281",
                "C73282",
                "C73283"
        );

        boolean jx2 = true;
        while (jx2) {
            int a2 = 80;
            int i2 = 0;
            for (String s : c732) {
                int b2 = dataCache.indexOf(s);
                if (b2 != -1 && dataCache.length() > b2 + 38) {
                    String result = dataCache.substring(b2, b2 + 38);
                    if (checkSum(result)) {
                        LabMonitorData data = c732(result);
                        data.setT2299(BigDecimal.valueOf(a2));
                        data.setItemId(itemIdMap.get("itemId"));
                        labMonitorDataService.save(data);
                        txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                        txt.setCaretPosition(txt.getDocument().getLength());
                    }
                    dataCache.delete(b2, b2 + 38);
                } else {
                    i2++;
                }
                a2++;
            }
            if (i2 == 4) {
                jx2 = false;
            }
        }

        List<String> c733 = Arrays.asList(
                "C73380",
                "C73381",
                "C73382",
                "C73383"
        );

        boolean jx3 = true;
        while (jx3) {
            int a3 = 80;
            int i3 = 0;
            for (String s : c733) {
                int b3 = dataCache.indexOf(s);
                if (b3 != -1 && dataCache.length() > b3 + 128) {
                    String result = dataCache.substring(b3, b3 + 128);
                    if (checkSum(result)) {
                        LabMonitorData data = c733(result);
                        data.setT2299(BigDecimal.valueOf(a3));
                        data.setItemId(itemIdMap.get("itemId"));
                        labMonitorDataService.save(data);
                        txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                        txt.setCaretPosition(txt.getDocument().getLength());
                    }
                    dataCache.delete(b3, b3 + 128);
                } else {
                    i3++;
                }
                a3++;
            }
            if (i3 == 4) {
                jx3 = false;
            }
        }

        List<String> c739 = Arrays.asList(
                "C73980",
                "C73981",
                "C73982",
                "C73983"
        );
        boolean jx4 = true;
        while (jx4) {
            int i4 = 0;
            int a4 = 80;
            for (String s : c739) {
                int b9 = dataCache.indexOf(s);
                // 先获得数量
                if (b9 != -1 && dataCache.length() > b9 + 10) {
                    int sl = Integer.parseInt(dataCache.substring(b9 + 8, b9 + 10));
                    int length = (5 + sl * 21 + 1) * 2;
                    if (dataCache.length() > b9 + length) {
                        String result = dataCache.substring(b9, b9 + length);
                        if (checkSum(result)) {
                            List<LabMonitorData> data = c739(result);
                            for (LabMonitorData datum : data) {
                                datum.setItemId(itemIdMap.get("itemId"));
                                labMonitorDataService.save(datum);
                                datum.setT2299(BigDecimal.valueOf(a4));
                                txt.append(toYearMonthDayHMS(new Date()) + ": " + datum.toTxz() + "\n");
                                txt.setCaretPosition(txt.getDocument().getLength());
                            }
                        }
                        dataCache.delete(b9, b9 + length);
                    }
                } else {
                    i4++;
                }
                a4++;
            }
            if (i4 == 4) {
                jx4 = false;
            }
        }

        if (dataCache.length() > maxLength) {
            dataCache.setLength(0);
        }
    }

    private void parseDatePro(byte[] dataArr) {
        StringBuilder dataBuilder = new StringBuilder(byteArrayToString(dataArr));
        System.out.println(dataBuilder.toString());
        List<String> ask = Arrays.asList(
                "34C73180", "34C73280", "34C73380", "36C73980",
                "34C73181", "34C73281", "34C73381", "36C73981",
                "34C73182", "34C73282", "34C73382", "36C73982",
                "34C73183", "34C73283", "34C73383", "36C73983"
        );
        for (String s : ask) {
            int begin = dataBuilder.indexOf(s);
            if(s.contains("36C739")){
                if (begin != -1 && dataBuilder.length() >= begin + 14) {
                    dataBuilder.delete(begin, begin + 14);
                }
            }else {
                if (begin != -1 && dataBuilder.length() >= begin + 10) {
                    dataBuilder.delete(begin, begin + 10);
                }
            }
        }

        List<String> c731 = Arrays.asList(
                "C73180",
                "C73181",
                "C73182",
                "C73183"
        );
        for (String s : c731) {
            int b1 = dataBuilder.indexOf(s);
            if (b1 != -1 && dataBuilder.length() >= b1 + 240) {
                String result = dataBuilder.substring(b1, b1 + 240);
                if (checkSum(result)) {
                    LabMonitorData data = c731(result);
                    data.setT2299(BigDecimal.valueOf(Integer.parseInt(s.substring(4))));
                    data.setItemId(itemIdMap.get("itemId"));
                    labMonitorDataService.save(data);
                    System.out.println("C731解析成功！");
                    txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                    txt.setCaretPosition(txt.getDocument().getLength());
                }
                dataBuilder.delete(b1, b1 + 240);
            }
        }

        List<String> c732 = Arrays.asList(
                "C73280",
                "C73281",
                "C73282",
                "C73283"
        );
        for (String s : c732) {
            int b2 = dataBuilder.indexOf(s);
            if (b2 != -1 && dataBuilder.length() >= b2 + 38) {
                String result = dataBuilder.substring(b2, b2 + 38);
                if (checkSum(result)) {
                    LabMonitorData data = c732(result);
                    data.setT2299(BigDecimal.valueOf(Integer.parseInt(s.substring(4))));
                    data.setItemId(itemIdMap.get("itemId"));
                    labMonitorDataService.save(data);
                    System.out.println("C732解析成功！");
                    txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                    txt.setCaretPosition(txt.getDocument().getLength());
                }
                dataBuilder.delete(b2, b2 + 38);
            }
        }


        List<String> c733 = Arrays.asList(
                "C73380",
                "C73381",
                "C73382",
                "C73383"
        );

        for (String s : c733) {
            int b3 = dataBuilder.indexOf(s);
            if (b3 != -1 && dataBuilder.length() >= b3 + 128) {
                String result = dataBuilder.substring(b3, b3 + 128);
                if (checkSum(result)) {
                    LabMonitorData data = c733(result);
                    data.setT2299(BigDecimal.valueOf(Integer.parseInt(s.substring(4))));
                    data.setItemId(itemIdMap.get("itemId"));
                    labMonitorDataService.save(data);
                    System.out.println("C733解析成功！");
                    txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                    txt.setCaretPosition(txt.getDocument().getLength());
                }
                dataBuilder.delete(b3, b3 + 128);
            }
        }


        List<String> c739 = Arrays.asList(
                "C73980",
                "C73981",
                "C73982",
                "C73983"
        );
        for (String s : c739) {
            int b9 = dataBuilder.indexOf(s);
            // 先获得数量
            if (b9 != -1 && dataBuilder.length() >= b9 + 10) {
                int sl = Integer.parseInt(dataBuilder.substring(b9 + 8, b9 + 10));
                int length = (5 + sl * 21 + 1) * 2;
                if (dataBuilder.length() > b9 + length) {
                    String result = dataBuilder.substring(b9, b9 + length);
                    if (checkSum(result)) {
                        List<LabMonitorData> data = c739(result);
                        for (LabMonitorData datum : data) {
                            datum.setItemId(itemIdMap.get("itemId"));
                            labMonitorDataService.save(datum);
                            System.out.println("C739解析成功！");
                            datum.setT2299(BigDecimal.valueOf(Integer.parseInt(s.substring(4))));
                            txt.append(toYearMonthDayHMS(new Date()) + ": " + datum.toTxz() + "\n");
                            txt.setCaretPosition(txt.getDocument().getLength());
                        }
                    }
                    dataBuilder.delete(b9, b9 + length);
                }
            }
        }
    }


    private void loopRemoveAsk() {
        List<String> ask = Arrays.asList(
                "34C73180", "34C73280", "34C73380", "36C73980",
                "34C73181", "34C73281", "34C73381", "36C73981",
                "34C73182", "34C73282", "34C73382", "36C73982",
                "34C73183", "34C73283", "34C73383", "36C73983"
        );
        boolean clean = true;

        while (clean) {
            int i = 0;
            for (String s : ask) {
                int begin = dataCache.indexOf(s);
                if (begin != -1 && dataCache.length() >= begin + 10) {
                    dataCache.delete(begin, begin + 10);
                } else {
                    i++;
                }
            }
            if (i == 16) {
                clean = false;
            }
        }
    }


//
//    private void parseDate(byte[] dataArr) {
//        StringBuilder builder = dataCache.get();
//        builder.append(byteArrayToString(dataArr));
//        dataCache.set();
//        System.out.println(dataCache);
//        int m1 = dataCache.indexOf("34C731");
//        if (m1 != -1) {
//            dataCache.delete(m1, m1 + 10);
//        }
//        int b1 = dataCache.indexOf("C731");
//        if (b1 != -1 && dataCache.length() >= b1 + 240) {
//            String result = dataCache.substring(b1, b1 + 240);
//            if (checkSum(result)) {
//                LabMonitorData data = c731(result);
//                data.setItemId(itemIdMap.get("itemId"));
//                labMonitorDataService.save(data);
//                txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
//                txt.setCaretPosition(txt.getDocument().getLength());
//                dataCache.delete(b1, b1 + 240);
//            }
//        }
//        int m2 = dataCache.indexOf("34C732");
//        if (m2 != -1) {
//            dataCache.delete(m2, m2 + 10);
//        }
//        int b2 = dataCache.indexOf("C732");
//        if (b2 != -1 && dataCache.length() >= b2 + 38) {
//            String result = dataCache.substring(b2, b2 + 38);
//            if (checkSum(result)) {
//                LabMonitorData data = c732(result);
//                data.setItemId(itemIdMap.get("itemId"));
//                labMonitorDataService.save(data);
//                txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
//                txt.setCaretPosition(txt.getDocument().getLength());
//                dataCache.delete(b2, b2 + 38);
//            }
//        }
//        int m3 = dataCache.indexOf("34C733");
//        if (m3 != -1) {
//            dataCache.delete(m3, m3 + 10);
//        }
//        int b3 = dataCache.indexOf("C733");
//        if (b3 != -1 && dataCache.length() >= b3 + 128) {
//            String result = dataCache.substring(b3, b3 + 128);
//            if (checkSum(result)) {
//                LabMonitorData data = c733(result);
//                data.setItemId(itemIdMap.get("itemId"));
//                labMonitorDataService.save(data);
//                txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
//                txt.setCaretPosition(txt.getDocument().getLength());
//                dataCache.delete(b3, b3 + 128);
//            }
//        }
//        int m9 = dataCache.indexOf("36C739");
//        if (m9 != -1) {
//            dataCache.delete(m9, m9 + 14);
//        }
//        int b9 = dataCache.indexOf("C739");
//        // 先获得数量
//        if (b9 != -1 && dataCache.length() > b9 + 10) {
//            int sl = Integer.parseInt(dataCache.substring(b9 + 8, b9 + 10));
//            int length = (5 + sl * 21 + 1) * 2;
//            if (dataCache.length() >= b9 + length) {
//                String result = dataCache.substring(b9, b9 + length);
//                if (checkSum(result)) {
//                    List<LabMonitorData> data = c739(result);
//                    for (LabMonitorData datum : data) {
//                        datum.setItemId(itemIdMap.get("itemId"));
//                        labMonitorDataService.saveOrUpdate(datum);
//                        txt.append(toYearMonthDayHMS(new Date()) + ": " + datum.toTxz() + "\n");
//                        txt.setCaretPosition(txt.getDocument().getLength());
//                    }
//
//                    dataCache.delete(b9, b9 + length);
//                }
//            }
//        }
//        if (dataCache.length() > maxLength) {
//            dataCache.setLength(0);
//        }
//    }


    public static void main(String[] args) {
        Integer.parseInt("C73180".substring(4));
//        StringBuilder dataCache = new StringBuilder();
//        String str = "C7 39 80 20 08 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 07";
//        str = str.replace(" ", "");
//        dataCache.append(str);
//        List<String> c739 = Arrays.asList(
//                "C73980",
//                "C73981",
//                "C73982",
//                "C73983"
//        );
//        for (String s : c739) {
//            int b9 = dataCache.indexOf(s);
//            // 先获得数量
//            if (b9 != -1 && dataCache.length() > b9 + 10) {
//                int sl = Integer.parseInt(dataCache.substring(b9 + 8, b9 + 10));
//                int length = (5 + sl * 21 + 1) * 2;
//                if (dataCache.length() >= b9 + length) {
//                    String result = dataCache.substring(b9, b9 + length);
//                    if (checkSum(result)) {
//
//                    }
//                    dataCache.delete(b9, b9 + length);
//                }
//            }
//        }

    }


    public void testTxz() {
        List<String> dataCache = new ArrayList<>();
        String origion = "34 C7 31 80 53 C7 31 80 80 00 0E 6B 80 00 0D A6 80 00 1A 2C 80 00 15 1F 0D 93 09 D2 0D EA 00 18 0D FE 25 28 1D 83 26 24 1E E4 80 00 0A 06 80 00 0D F5 11 D7 0E A9 80 00 08 52 80 00 FD 62 02 1A 0E 74 80 00 05 B1 00 00 11 D7 0E A9 08 0B 80 00 17 B0 10 70 16 B4 0F 0F 0C A4 80 00 00 09 80 00 00 8A 00 00 01 5D 00 00 1C CA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ED\n" +
                "34 C7 32 80 52\n" +
                "C7 32 80 00 00 00 01 00 00 00 01 00 00 00 01 00 00 80 03\n" +
                "34 C7 33 80 51\n" +
                "C7 33 80 01 D6 01 D6 00 00 00 00 01 D6 01 D6 01 90 00 00 00 0A 00 0A 03 21 03 1E 00 14 00 00 04 B0 00 00 05 B1 00 00 CA EE CD 0A 11 D7 0E A9 D0 50 80 00 80 00 80 00 80 00 80 00 80 00 80 00 F3\n" +
                "34 C7 34 80 50\n" +
                "C7 34 80 00 00 00 00 00 01 00 00 08 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 01 00 00 00 04 09 00 00 00 01 00 00 01 01 00 00 00 00 01 01 67\n" +
                "34 C7 36 80 4E\n" +
                "C7 36 80 00 00 01 2C 02 EE 0A F0 00 21 00 23 00 5A 00 78 00 F0 FF FF FF FF 01 2C 3C\n" +
                "34 C7 37 80 4D\n" +
                "C7 37 80 00 01 01 01 00 01 32 90 00 00 19 8C 00 00 19 8C 03 E8 00 53 01 0A 01 01 03 14 00 1C 00 00 00 14 00 A0 00 A0 01 18 0A F0 00 4B 02 EE 04 20 03 04 06 04 00 05 00 00 00 14 00 14 00 14 00 00 01 B4 26\n" +
                "36 C7 39 80 40 08 01\n" +
                "C7 39 80 40 08 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 E4 12 E7 00 E7 00 E7 00 00 00 00 00 E7\n" +
                "38 C7 42 80 00 00 00 07 37\n" +
                "C7 42 80 00 00 00 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3C 19 FE 00 00 00 B4 00 00 00 00 00 00 00 00 00 00 00 4F 00 00 00 01 00 18\n" +
                "34 C7 43 80 41\n" +
                "C7 43 80 02 58 01 2C 06 9C 01 A7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 86\n" +
                "34 C7 44 80 40\n" +
                "C7 44 80 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 78\n" +
                "34 C7 45 80 3F\n" +
                "C7 45 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 73\n" +
                "30 C8 39 80 00 00 3F 00 00 C0 00 40 00 00 BF 00 41 00 00 BE 00 42 00 00 BD 00 43 00 00 BC 00 44 00 00 BB 00 45 00 00 BA 00 46 00 00 B9 56\n" +
                "C8 39 80 00 3F 00 00 C0 00 40 00 60 5F 00 41 00 0A B4 00 42 00 1E 9F 00 43 00 C8 F4 00 44 00 1E 9D 00 45 00 C8 F2 00 46 00 08 B1 86\n" +
                "34 C7 30 80 54\n" +
                "C7 30 80 32 36 30 30 00 25 00 27 00 27 00 02 00 00 00 00 4B";


        char[] dataStr = origion.replace(" ", "").replace("\n", "").toCharArray();

        for (char s : dataStr) {
            dataCache.add(s + "");
        }
        String content = dataCache.stream().collect(Collectors.joining(""));
        int m1 = content.indexOf("34C731");
        if (m1 != -1) {
            dataCache.subList(m1, m1 + 10).clear();
        }
        content = listToString(dataCache);
        int b1 = content.indexOf("C731");
        if (b1 != -1 && content.length() >= b1 + 240) {
            String result = content.substring(b1, 240);
            if (checkSum(result)) {
                LabMonitorData data = c731(result);
                data.setItemId(itemIdMap.get("itemId"));
                //labMonitorDataService.saveOrUpdate(data);
                //txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                //txt.setCaretPosition(txt.getDocument().getLength());
            }
            dataCache.subList(b1, b1 + 240).clear();
        }
        content = listToString(dataCache);
        int m2 = content.indexOf("34C732");
        if (m2 != -1) {
            dataCache.subList(m2, m2 + 10).clear();
        }
        content = listToString(dataCache);

        int b2 = content.indexOf("C732");
        if (b2 != -1 && content.length() >= b2 + 38) {
            String result = content.substring(b2, 38);
            if (checkSum(result)) {
                LabMonitorData data = c732(result);
                data.setItemId(itemIdMap.get("itemId"));
                //labMonitorDataService.saveOrUpdate(data);
                //txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                //txt.setCaretPosition(txt.getDocument().getLength());
            }
            dataCache.subList(b2, b2 + 38).clear();
        }
        content = listToString(dataCache);
        int m3 = content.indexOf("34C733");
        if (m3 != -1) {
            dataCache.subList(m3, m3 + 10).clear();
        }
        content = listToString(dataCache);
        int b3 = content.indexOf("C733");
        if (b3 != -1 && content.length() >= b3 + 128) {
            String result = content.substring(b2, 128);
            if (checkSum(result)) {
                LabMonitorData data = c733(result);
                data.setItemId(itemIdMap.get("itemId"));
                // labMonitorDataService.saveOrUpdate(data);
                // txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                // txt.setCaretPosition(txt.getDocument().getLength());
            }
            dataCache.subList(b3, b3 + 128).clear();
        }
        content = listToString(dataCache);
        int m9 = content.indexOf("36C739");
        if (m9 != -1) {
            dataCache.subList(m9, m9 + 14).clear();
        }
        content = listToString(dataCache);
        int b9 = content.indexOf("C739");
        // 先获得数量
        if (b9 != -1 && content.length() > b9 + 10) {
            int sl = Integer.parseInt(content.substring(b9 + 8, b9 + 10));
            int length = (5 + sl * 21 + 1) * 2;
            if (content.length() >= b9 + length) {
                String result = content.substring(b9, b9 + length);
                if (checkSum(result)) {
                    List<LabMonitorData> data = c739(result);
                    for (LabMonitorData datum : data) {
                        datum.setItemId(itemIdMap.get("itemId"));
                    }
                    //labMonitorDataService.saveOrUpdate(data);
                    //txt.append(toYearMonthDayHMS(new Date()) + ": " + data.toTxz() + "\n");
                    // txt.setCaretPosition(txt.getDocument().getLength());
                }
                dataCache.subList(b9, b9 + length).clear();
            }
        }
        dataCache.clear();
    }

    private String listToString(List<String> data) {
        StringBuilder str = new StringBuilder();
        for (String datum : data) {
            str.append(datum);
        }
        return str.toString();
    }


    private LabMonitorData txz() {
        LabMonitorData data = new LabMonitorData();
        data.setLabRoomGuid(labMap.get("TXZ"));
        data.setType("TXZ");
        data.setItem(monitorMap.get(data.getLabRoomGuid()));
        data.setTimePoint(new Date());
        return data;

    }


    private LabMonitorData c731(String result) {
        LabMonitorData data = txz();
        data.setT2208(getValueWithNegative(result, 24 * 4 + 6, 25 * 4 + 6, BigDecimal.valueOf(0.001)));
        data.setT2209(getValueWithNegative(result, 28 * 4 + 6, 29 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2210(getValueWithNegative(result, 27 * 4 + 6, 28 * 4 + 6, BigDecimal.valueOf(0.001)));
        data.setT2211(getValueWithNegative(result, 26 * 4 + 6, 27 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2212(getValueWithNegative(result, 15 * 4 + 6, 16 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2213(getValueWithNegative(result, 16 * 4 + 6, 17 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2214(getValueWithNegative(result, 18 * 4 + 6, 19 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2215(getValueWithNegative(result, 8 * 4 + 6, 9 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2216(getValueWithNegative(result, 1 * 4 + 6, 2 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2217(getValueWithNegative(result, 3 * 4 + 6, 4 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2218(getValueWithNegative(result, 5 * 4 + 6, 6 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2219(getValueWithNegative(result, 7 * 4 + 6, 8 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2220(getValueWithNegative(result, 10 * 4 + 6, 11 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2221(getValueWithNegative(result, 12 * 4 + 6, 13 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2222(getValueWithNegative(result, 20 * 4 + 6, 21 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2223(getValueWithNegative(result, 13 * 4 + 6, 14 * 4 + 6, BigDecimal.valueOf(0.001)));
        data.setT2224(getValueWithNegative(result, 14 * 4 + 6, 15 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2225(getValueWithNegative(result, 30 * 4 + 6, 31 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2226(getValueWithNegative(result, 31 * 4 + 6, 32 * 4 + 6, BigDecimal.valueOf(0.01)));
        return data;
    }

    private LabMonitorData c732(String result) {
        LabMonitorData data = txz();
        data.setT2229(getValueWithNegative(result, 1 * 2 + 6, 2 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2230(getValueWithNegative(result, 2 * 2 + 6, 3 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2231(getValueWithNegative(result, 7 * 2 + 6, 8 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2232(getValueWithNegative(result, 8 * 2 + 6, 9 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2233(getValueWithNegative(result, 9 * 2 + 6, 10 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2234(getValueWithNegative(result, 3 * 2 + 6, 4 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2235(getValueWithNegative(result, 4 * 2 + 6, 5 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2236(getValueWithNegative(result, 11 * 2 + 6, 12 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2237(getValueWithNegative(result, 6 * 2 + 6, 7 * 2 + 6, BigDecimal.valueOf(1)));
        data.setT2238(getValueWithNegative(result, 6, 1 * 2 + 6, BigDecimal.valueOf(1)));
        return data;
    }

    private LabMonitorData c733(String result) {
        LabMonitorData data = txz();
        data.setT2201(getValueWithNegative(result, 14 * 4 + 6, 15 * 4 + 6, BigDecimal.valueOf(1).divide(new BigDecimal(60), BigDecimal.ROUND_HALF_UP)));
        data.setT2202(getValueWithNegative(result, 15 * 4 + 6, 16 * 4 + 6, BigDecimal.valueOf(1).divide(new BigDecimal(60), BigDecimal.ROUND_HALF_UP)));
        data.setT2203(getValueWithNegative(result, 10 * 4 + 6, 11 * 4 + 6, BigDecimal.valueOf(1)));
        data.setT2204(getValueWithNegative(result, 11 * 4 + 6, 12 * 4 + 6, BigDecimal.valueOf(1)));
        data.setT2205(getValueWithNegative(result, 6, 4 + 6, BigDecimal.valueOf(1)));
        data.setT2206(getValueWithNegative(result, 4 + 6, 2 * 4 + 6, BigDecimal.valueOf(1)));
        data.setT2207(getValueWithNegative(result, 2 * 4 + 6, 3 * 4 + 6, BigDecimal.valueOf(1)));
        data.setT2227(getValueWithNegative(result, 20 * 4 + 6, 21 * 4 + 6, BigDecimal.valueOf(0.01)));
        data.setT2228(getValueWithNegative(result, 21 * 4 + 6, 22 * 4 + 6, BigDecimal.valueOf(0.01)));
        return data;
    }

    private List<LabMonitorData> c739(String result) {
        List<LabMonitorData> dataList = new ArrayList<>();
        int sl = Integer.parseInt(result.substring(8, 10));
        Date date = new Date();
        for (int i = 0; i < sl; i++) {
            LabMonitorData data = txz();
            data.setTimePoint(date);
            int jg = 10 + (i) * 21 * 2;
            String address = result.substring(jg, jg + 2);
            if ("FF".equals(address)) {
                continue;
            }
            data.setT2239(BigDecimal.valueOf(Integer.parseInt(address) - 40));
            data.setT2240(getValueWithNegative(result, jg + (11 * 2), jg + (13 * 2), BigDecimal.valueOf(0.01)));
            data.setT2241(getValueWithNegative(result, jg + (13 * 2), jg + (15 * 2), BigDecimal.valueOf(0.01)));
            data.setT2242(getValueWithNegative(result, jg + (15 * 2), jg + (17 * 2), BigDecimal.valueOf(0.01)));
            data.setT2243(getValueWithNegative(result, jg + (2 * 2), jg + (4 * 2), BigDecimal.valueOf(1)));
            dataList.add(data);
        }
        return dataList;
    }


    public String checkDate(String result) {
        // 判断是否是发送命令
        String begin = result.substring(0, 2);
        if (!begin.equals("47") && result.substring(2, 4).equals("47")) {
            // 包含发送指令
            // 长度
            Integer length = Integer.parseInt(hexToAscii(begin));
            if (length == (result.length() / 2) - 1) {
                // 只包含发送指令
                return null;
            }
            // 包含发送和接收
            result = result.substring(length * 2 + 2);
            // 校验和
        }
        // 只包含接收
        // 校验和
        //return checkSum(result);
        return null;
    }


    public static boolean checkSum(String result) {
        String sum = makeChecksum(result.substring(0, result.length() - 2));
        String rSum = reverseHex(sum);
        // 校验和 转 2进制 取反码 -> 16进制
        String last = result.substring(result.length() - 2);
        if (rSum.equals(last)) {
            return true;
        }
        return false;
    }


    private int min(int start, String origion, String flag) {
        int s = origion.indexOf(flag);
        if (-1 != s) {
            if (start != -1) {
                start = start < s ? start : s;
            } else {
                start = s;
            }
        }
        return start;
    }


    // 截取指定位置的字符串转换成十进制
    public BigDecimal getValueByLocation(String result, int begin, int end, BigDecimal jd) {
        String val = result.substring(begin, end);
        int dec = hexStringToInt(val);
        return BigDecimal.valueOf(dec).multiply(jd);
    }

    // 截取指定位置的字符串转换成十进制
    // 处理负数的情况，如果算出来的数值＞7FFF，则用这个数减去FFFF
    public BigDecimal getValueWithNegative(String result, int begin, int end, BigDecimal jd) {
        String val = result.substring(begin, end);
        int dec = hexStringToInt(val);
        if (dec > 127255) {
            dec = dec - 255255;
        }
        return BigDecimal.valueOf(dec).multiply(jd);
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


}
