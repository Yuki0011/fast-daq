package com.mhaq.collect.common;


import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author xutao
 * @date 2021/4/7 11:05 上午
 * @description 文件操作工具
 */
public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);


    /**
     * 根据名称排序得到最后最新日期的文件夹
     *
     * @param
     * @return
     */
    public static File getLastFileDirectory(File[] files) {
        List<File> directories = new ArrayList<File>();
        if (ArrayUtils.isEmpty(files)) {
            logger.info("未生成实验相关数据");
            return null;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                directories.add(f);
            }
        }
        Collections.sort(directories, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return directories.get(directories.size()-1);
    }

    public static File getLastFile(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return fileList.get(fileList.size()-1);
    }


    public static List<String> getContentList(File f) {
        FileInputStream workInfoIs = null;
        try {
            workInfoIs = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Reader reader = new InputStreamReader(workInfoIs, Charset.forName("GBK"));
        List<String> contentList = new ArrayList<>();
        // 循环每行
        while (true) {
            String line = readLine(reader);
            if (StringUtils.isEmpty(line)) {
                break;
            }
            contentList.add(line);
        }
        return contentList;
    }


    /**
     * 从字节流 {@link Reader} 中读取一行
     *
     * @param reader 字节流
     * @return 字节流中的一行内容
     */
    public static String readLine(Reader reader) {
        StringBuffer line = new StringBuffer();
        try {
            while (true) {
                int read = reader.read();
                if (read == '\n' || read == -1) {
                    return line.toString();
                }
                line.append((char) read);
            }
        } catch (IOException e) {
            logger.error("读取一行时出现异常", e);
        }
        return line.toString();
    }


    /**
     * 通过指针向下读取
     *
     * @param file
     * @param pointer
     * @return
     */
    public static Map<String,Object> readByPoint(File file, long pointer) {
        Map<String,Object> dataMap = new HashMap<>(2);
        List<String> result = new ArrayList<String>();
        RandomAccessFile randomAccessFileRead = null;
        try {
            randomAccessFileRead = new RandomAccessFile(file, "r");
            long length = randomAccessFileRead.length();
            if (0L == length) {
                return dataMap;
            } else {
                // 第一行
                if (pointer == 0) {
                    result.add(randomAccessFileRead.readLine());
                }
                while (pointer < length - 1) {
                    randomAccessFileRead.seek(pointer);
                    if ('\n' == randomAccessFileRead.readByte()) {
                        String line = randomAccessFileRead.readLine();
                        result.add(line);
                    }
                    pointer++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                randomAccessFileRead.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataMap.put("data",result);
        dataMap.put("point",pointer);
        return dataMap;
    }






    public static void listAllFile(File file,List<File> allFiles){
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isDirectory()){
                listAllFile(f,allFiles);
            }else{
                allFiles.add(f);
            }
        }
    }


    /**
     * 通过指针向下插入
     *
     * @param file
     * @return
     */
    public static long insertByPoint(File file) {
        Long point = returnLastPoint(file);
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file,"rw");
            // 处理数据
            String str = "20210413124226\t2021-04-13 16:54:22\t4118.741\t3780\t108.9614\t12623.37\t14000\t90.1669\t3.064861\t3.703704\t82.75124\t0\t100\t3832670\t3236207\t700778.9\t20169.06\t7.63\t12.265\t13.411\t11.102\t0.00608\t0.00506\t0.00427\t0.00514\t0.002\t0.002\t0.001\t0.006\t0.0522\t0.0316\t0.0251\t0.0351\t4644.3\t145.2\t133.18\t131.52\t409.9\t267033\t7.57\t13.58\t14.22\t11.79\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t4.0483\t0.0099005\t9.371701E-03\t4.0676\t266898\t221.33\t221.83\t304.91\t247.73\t0.01663\t0.01354\t0.01528\t0.01515\t0.58\t0.09\t-0.62\t0.79\t0.1566\t0.0084\t0.0083\t0.0667\t50.019\t0.013575\t8.4986E-04\t9.3934E-04\t0.015364\t475443\t219.19\t219.74\t219.52\t219.48\t0.0168\t0.0169\t1.0406\t0.3581\t0.66\t0.16\t161.48\t162.3\t0.1793\t0.0419\t0.707\t0.6882\t0\t0.08618\t0.022353\t29.136\t29.245\t475444\t218.88\t219.48\t4.77\t147.71\t9.8\t7.51\t7.24\t8.19\t1918\t1278\t-3\t3194\t0.8939\t0.7751\t-0.0759\t0.834\t0\t290.09\t254.49\t-0.45385\t544.13\t475443\t2.999\t7.243\t7.034\t217.63\t219.29\t0\t0\t0\t18.439\t1.0053\t0\t0\t0.001\t3998.9\t119.84\t0\t0\t0\t0.9965\t0.5436\t197510\t8.07683E-07\t-6.08619E-08\t6.06821E-06\t0\t26.624\t0\t18.99\t12.36\t18.97\t11.87\t20.08175\t20.10733\t21.12127\t-61.0827\t-62.1875\t-0.80935\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t20.4621\t14.26291\t22.43683\t-0.4955\t0.3966999\t-0.6104499\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t19.83\t14.34\t19.85\t14.25\t22.84\t22.8\t20.84\t0\t4.714\t1.0964\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t24.2\t15.83\t24.80226\t-3.4136\t4.6132\t130.7822\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t20.29\t14.76\t19.92\t14.81\t25.61\t25.64\t19.53\t0.1033001\t0.1961\t0.8966999\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t27.82\t17.6\t27.21\t0.2006\t1.7994\t754.2932\t35.39773\t79.50672\t41.2433\t49.30476\t-8.061462\t12623.37\t12623.37\t100\t0\t0\t34.66125\t172.7709\t0\t0\t16.63\t13.68\t16.54\t72.05992\t7.39\t6.03\t7.39\t82.02897\t1.87\t0.96\t1.8\t84.77382\t19.1\t20.5\t21.7\t24.5\t24.7\t27.2\t102.21\t101.63\t101.62\t19.74\t19.87\t19.78\t19.72\t0\t0.02\t0\t0\t0\t2.1\t19.23\t0.001\t0\t0\t-0.002\t-0.001\t0.005\t0\t0.001\t0.001\t-0.001\t0.003\t-0.002\t-0.002\t-0.002\t0\t-0.002\t0.002\t0.001\t-0.001\t0.002\t0.004\t0.003\t0.001\t0.002\t0.002\t-0.002\t0.002\t0.001\t-0.004\t0.002\t0.001\t-0.002\t-0.004\t0.001\t0\t-0.003\t0.001\t-0.004\t-0.002\t0.001\t-1.372\t-1.375\t-1.375\t-1.375\t-1.385\t-1.372\t-1.375\t-1.375\t-1.375\t-1.375\t22\t22\t22\t21.9\t22\t21.9\t21.9\t22\t21.9\t21.9\t22\t22.1\t22.1\t22.1\t22\t21.999\t22\t21.9\t22\t21.9\t-1.375\t-1.375\t-1.372\t-1.351\t-1.375\t-1.375\t-1.375\t-1.375\t-1.378\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.399\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.365\t-1.375\t-1.375\t-1.375\t-1.375\t-0.003\t0.006\t0.001\t-0.004\t0.003\t-1.375\t-1.375\t-1.375\t-1.372\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.365\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-0.004\t0.003\t-0.001\t-0.001\t0.003\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.381\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.368\t-1.375\t-1.369\t-1.383\t-1.375\t-1.38\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t-1.375\t0\t0.004\t-0.002\t0.001\t-0.004\t0.003\t0.004\t0\t-0.001\t-0.3\t-0.2\t-0.5\t-0.2\t-0.3\t-0.4\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t17.8\t17.8\t17.7\t17.8\t17.9\t17.9\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t19.5\t19.5\t17.4\t19.5\t16.3\t16\t17.5\t17.4\t0\t0\t19.5\t16.9\t15.8\t16.3\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t18.7\t18.7\t21\t15.4\t14.7\t11.7\t11.9\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t24.9\t26.9\t28.3\t34.8\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t29.7\t33.1\t38\t33.3\t30.5\t0\t0\t0\t0\t0\t34.6\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t77\t-3.1\t35.6\t41.7\t9.2\t58.3\t7.4\t6.6\t7.1\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t7.6\t7.5\t7.6\t7.6\t7.6\t7.5\t0\t0\t7.6\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t7.6\t0\t0\t0\t0\t0\t0\t0\t0\t0\t10.8\t7.7\t10.6\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t26.6\t27.3\t0\t0\t0\t0\t0\t54.7\t27.6\t45.8\t28.3\t56\t28.1\t53.9\t29.5\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t29.3\t27.2\t28.1\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t60.7\t-6.6\t56.4\t27.6\t58.6\t2.3\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t100\t0\t100\t0\t58.1\t19\t8.4\t6\t0\t11.9\t0\t39.6\t7.1\t0\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t79.50672\t0.47\t0\t1.0024\t1.0023\t-7.5\t0\n";
            raf.seek(point);
            raf.write(str.getBytes());
            return raf.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  0;

    }




    public static Long returnLastPoint(File file) {
        Long pointer = 0L;
        List<String> result = new ArrayList<String>();
        RandomAccessFile randomAccessFileRead = null;
        try {
            randomAccessFileRead = new RandomAccessFile(file, "r");
            long length = randomAccessFileRead.length();
            if (0L == length) {
                pointer = 0L;
            } else {
                // 第一行
                if (pointer == 0) {
                    result.add(randomAccessFileRead.readLine());
                }
                while (pointer < length - 1) {
                    randomAccessFileRead.seek(pointer);
                    if ('\n' == randomAccessFileRead.readByte()) {
                        String line = randomAccessFileRead.readLine();
                        result.add(line);
                    }
                    pointer++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                randomAccessFileRead.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pointer;
    }

    public static Long read(File file,Long pointer) {
        List<String> result = new ArrayList<String>();
        RandomAccessFile randomAccessFileRead = null;
        try {
            randomAccessFileRead = new RandomAccessFile(file, "r");
            long length = randomAccessFileRead.length();
            if (0L == length) {
                pointer = 0L;
            } else {
                // 第一行
                if (pointer == 0) {
                    result.add(randomAccessFileRead.readLine());
                }
                while (pointer < length - 1) {
                    randomAccessFileRead.seek(pointer);
                    if ('\n' == randomAccessFileRead.readByte()) {
                        String line = randomAccessFileRead.readLine();
                        result.add(line);
                        System.out.println(line);
                    }
                    pointer++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                randomAccessFileRead.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pointer;
    }


}
