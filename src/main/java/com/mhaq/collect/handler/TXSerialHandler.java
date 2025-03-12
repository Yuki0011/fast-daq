package com.mhaq.collect.handler;

import com.mhaq.collect.entity.LabMonitorData;
import com.mhaq.collect.service.ILabMonitorDataService;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static com.mhaq.collect.common.BiteUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.*;

/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 串口监听 TX(小)
 */
@PropertySource("classpath:application.yml")
@Service
public class TXSerialHandler implements SerialPortEventListener {

    private static final int BIT_RATE = 9600;
    public static final int DATA_BITS = SerialPort.DATABITS_8;
    public static final int STOP_BIT = SerialPort.STOPBITS_1;
    public static final int PARITY_BIT = SerialPort.PARITY_NONE;

    private static SerialPort serialPort;
    private static InputStream in;
    private static OutputStream out;


    @Autowired
    private ILabMonitorDataService labMonitorDataService;

    @Resource(name = "defaultThreadPool")
    private ThreadPoolTaskExecutor executor;

    private TXSerialHandler() {
    }


    public boolean init(String port) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
            if (portIdentifier.isCurrentlyOwned()) {
                txt.append(toYearMonthDayHMS(new Date()));
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
                    String[] orign = ByteArrayToStringArray(buffer);
                    // 校验和
                    if (Objects.equals(orign[orign.length - 1], makeChecksumByArr(orign, 1))) {
                        handleResponseData(orign, now);
                    }
                });
            }
            // 休眠
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleResponseData(String[] orign, Date now) {
        String[] result = Arrays.copyOfRange(orign, 7, orign.length);
        // 逻辑处理
        LabMonitorData monitorData = new LabMonitorData();
        monitorData.setTimePoint(now);
        // 实验室从缓存中拿 没有的话通过ip地址取
        monitorData.setLabRoomGuid(labMap.get("TX(小)"));
        monitorData.setType("TX(小)");
        monitorData.setItem(monitorMap.get(monitorData.getLabRoomGuid()));
        monitorData.setT2201(BigDecimal.valueOf(hexStringToInt(result[3])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2202(BigDecimal.valueOf(hexStringToInt(bin2int(getStrByHex(result[9], 0, 3)) + result[5])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2203(BigDecimal.valueOf(hexStringToInt(bin2int(getStrByHex(result[11], 0, 3)) + result[6])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2204(BigDecimal.valueOf(hexStringToInt(getStrByHex(result[9], 7) + result[10])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2205(BigDecimal.valueOf(hexStringToInt(getStrByHex(result[11], 7) + result[12])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2206(BigDecimal.valueOf(0.02 * hexStringToInt(result[29])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2207(BigDecimal.valueOf(0.5 * hexStringToInt(result[35]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2208(BigDecimal.valueOf(0.01 * hexStringToInt(result[31])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2209(BigDecimal.valueOf(0.5 * hexStringToInt(result[36]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2210(BigDecimal.valueOf(hexStringToInt(result[17]) - 55).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2211(BigDecimal.valueOf(0.5 * hexStringToInt(result[19]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2212(BigDecimal.valueOf(0.5 * hexStringToInt(result[15]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2213(BigDecimal.valueOf(0.5 * hexStringToInt(result[21]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2214(BigDecimal.valueOf(0.5 * hexStringToInt(result[23]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2215(BigDecimal.valueOf(0.5 * hexStringToInt(result[33]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2216(BigDecimal.valueOf(0.5 * hexStringToInt(result[25]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2217(BigDecimal.valueOf(0.5 * hexStringToInt(result[27]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2218(BigDecimal.valueOf(0.2 * hexStringToInt(result[39])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2219(BigDecimal.valueOf(0.2 * hexStringToInt(result[59])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2220(BigDecimal.valueOf(hexStringToInt(result[38]) - 55).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2221(BigDecimal.valueOf(hexStringToInt(getStrByHex(result[7], 0))).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2222(BigDecimal.valueOf(hexStringToInt(result[40])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2223(result[43].equals("00") ? null : BigDecimal.valueOf(0.5 * hexStringToInt(result[43]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2224(result[44].equals("00") ? null : BigDecimal.valueOf(0.5 * hexStringToInt(result[44]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2225(result[45].equals("00") ? null : BigDecimal.valueOf(0.5 * hexStringToInt(result[45]) - 30).setScale(2, RoundingMode.HALF_UP));
        monitorData.setT2226(result[43].equals("00") ? null : BigDecimal.valueOf(hexStringToInt(getStrByHex(result[41], 2) + result[42])).setScale(2, RoundingMode.HALF_UP));
        monitorData.setItemId(itemIdMap.get("itemId"));
        labMonitorDataService.save(monitorData);
        txt.append(toYearMonthDayHMS(new Date())+": "+monitorData.toSmallTx()+"\n");
        txt.setCaretPosition(txt.getDocument().getLength());
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
