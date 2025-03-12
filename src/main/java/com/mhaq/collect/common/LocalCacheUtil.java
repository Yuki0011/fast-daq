package com.mhaq.collect.common;







import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.SimpleFormatter;


/**
 * @author xutao
 * @date 2021/4/7 10:48 上午
 * @description 本地缓存工具类
 */
public class LocalCacheUtil {

    public static JTextArea  txt ;

    public static DesktopApp  app ;

    public static Map<String,byte[]> modbus = new HashMap<>();

    // 监控和实验室对应关系
    public static Map<String,String> monitorMap = new HashMap<>();

    // 实验室和检测项目对应关系
    public static Map<String,String> labMap = new HashMap<>();

    public static Map<String,Long> pointMap = new HashMap<>();

    public static Map<String,Long> itemIdMap = new HashMap<>();


    public static List<String> answerList = new ArrayList<>();

    public static boolean addFlag = false;
    public static int pass = 0;
    public static int back = 0;


    public static String toYearMonthDayHMS(Date date){
        String formate = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }
}
