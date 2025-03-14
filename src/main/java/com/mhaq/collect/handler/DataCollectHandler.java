package com.mhaq.collect.handler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mhaq.collect.entity.LabItem;
import com.mhaq.collect.entity.LabItemColumnConfig;
import com.mhaq.collect.entity.enums.FileTypeEnum;
import com.mhaq.collect.service.ILabItemColumnConfigService;
import com.mhaq.collect.service.ILabItemDataService;
import com.mhaq.collect.service.ILabItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import static com.mhaq.collect.common.FileUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.*;


/**
 * @author xutao
 * @date 2021/4/7 10:46 上午
 * @description 数据采集 处理器
 */
@Service
@PropertySource("classpath:application.yml")
public class DataCollectHandler {

    private Logger logger = LoggerFactory.getLogger(DataCollectHandler.class);

    @Autowired
    private ILabItemService labItemService;
    @Autowired
    private ILabItemColumnConfigService labItemColumnConfigService;
    @Autowired
    private ILabItemDataService labItemDataService;
    @Value("${lab}")
    private String labAddress;
    @Value("${labname}")
    private String labname;

    public void handleData() {
        if (null == labAddress) {
            logger.info("无法获取连接地址");
            return;
        }
        // 获取所有文件
        File orignFile = new File(labAddress);
        if (null == orignFile) {
            logger.info("目录不存在！");
            return;
        }
        List<File> allFiles = new ArrayList<>(16);
        // 递归获取说有文件
        listAllFile(orignFile, allFiles);
        if (CollectionUtils.isEmpty(allFiles)) {
            logger.info(labname + "文件下还没有生成数据!");
            return;
        }
        // 文件筛选获取要更新的文件
        Map<FileTypeEnum, File> fileMap = new HashMap<>(3);
        List<File> dataFiles = new ArrayList<>();
        for (File f : allFiles) {
            if (Objects.equals(FileTypeEnum.LAB_ITEM.getValue(), f.getName())) {
                fileMap.put(FileTypeEnum.LAB_ITEM, f);
            } else if (Objects.equals(FileTypeEnum.LAB_ITEM_SENSOR_CONFIG.getValue(), f.getName())) {
                fileMap.put(FileTypeEnum.LAB_ITEM_SENSOR_CONFIG, f);
            } else if (f.isFile()) {
                dataFiles.add(f);
            }
        }
        if (CollectionUtils.isNotEmpty(dataFiles)) {
            File file = getLastFile(dataFiles);
            fileMap.put(FileTypeEnum.LAB_ITEM_DATA, file);
        }
        File itemFile = fileMap.get(FileTypeEnum.LAB_ITEM);
        LabItem labItem = new LabItem();
        if (null != itemFile) {
            List<String> contentList = getContentList(itemFile);
            try {
                saveItemInformation(contentList, labItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File config = fileMap.get(FileTypeEnum.LAB_ITEM_SENSOR_CONFIG);
        if (null != config) {
            List<String> contentList = getContentList(config);
            try {
                saveLabItemSensorConfig(contentList, labItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File data = fileMap.get(FileTypeEnum.LAB_ITEM_DATA);
        if (null != data) {
            // 从缓存中拿到指针
            Long pointer = pointMap.get(labname + data.getName());
            if (null == pointer) {
                // 数据库查询最后的指针位置
                Long dbPoint = labItemService.getLastPoint(labItem.getTestFlag());
                pointer = null == dbPoint ? 0L : dbPoint;
            }
            // 从指针位置向下读取
            Map<String, Object> dataMap = readByPoint(data, pointer);
            List<String> contentList = (List<String>) dataMap.get("data");
            if (CollectionUtils.isNotEmpty(contentList)) {
                Long nPoint = (Long) dataMap.get("point");
                // 保存实验数据
                if (saveBatchData(contentList, labItem)) {
                    // 缓存指针
                    pointMap.put(labname + data.getName(), nPoint);
                    // UPDATE ZZ
                    labItem.setPoint(nPoint);
                    labItemService.updateById(labItem);
                }
                ;
            }
        }
    }


    /**
     * 保存实验信息
     *
     * @return
     */
    public void saveItemInformation(List<String> contentList, LabItem labItem) {
        // 信息固定,直接读固定的行即可
        for (String str : contentList) {
            if (str.startsWith("测试标识")) {
                labItem.setTestFlag(cleanBlank(str, "测试标识"));
            } else if (str.startsWith("台位标识")) {
                labItem.setLabRoomGuid(cleanBlank(str, "台位标识"));
            } else if (str.startsWith("测试状态")) {
                labItem.setStatus(cleanBlank(str, "测试状态"));
            } else if (str.startsWith("开启时间")) {
                labItem.setStartTime(cleanBlank(str, "开启时间"));
            } else if (str.startsWith("结束时间")) {
                labItem.setEndTime(cleanBlank(str, "结束时间"));
            } else if (str.startsWith("测试编号")) {
                labItem.setTestNum(cleanBlank(str, "测试编号"));
            } else if (str.startsWith("测试项目")) {
                labItem.setItem(cleanBlank(str, "测试项目"));
            } else if (str.contains("试验名称")) {
                labItem.setItem(cleanBlank(str, "试验名称"));
            } else if (str.startsWith("样品编号")) {
                labItem.setSampleNum(cleanBlank(str, "样品编号"));
            } else if (str.startsWith("被测机编号")) {
                labItem.setSampleNum(cleanBlank(str, "被测机编号"));
            } else if (str.contains("样机型号")) {
                labItem.setSampleSpec(cleanBlank(str, "样机型号"));
            } else if (str.startsWith("被测机型号")) {
                labItem.setSampleSpec(cleanBlank(str, "被测机型号"));
            } else if (str.startsWith("样机名称")) {
                labItem.setSample(cleanBlank(str, "样机名称"));
            } else if (str.startsWith("被测机类型")) {
                labItem.setSample(cleanBlank(str, "被测机类型"));
            } else if (str.startsWith("操作员")) {
                labItem.setDeveloper(cleanBlank(str, "操作员"));
            } else if (str.startsWith("备注")) {
                labItem.setRemark(cleanBlank(str, "备注"));
            } else if (str.startsWith("试验目的")) {
                labItem.setPurpose(cleanBlank(str, "试验目的"));
            } else if (str.startsWith("测试时间")) {
                String testTime = cleanBlank(str, "测试时间");
                if (StringUtils.isNotBlank(testTime)) {
                    labItem.setTestTime(dateStringtoDate(testTime));
                }
            } else if (str.startsWith("试验时间")) {
                String testTime = cleanBlank(str, "试验时间");
                if (StringUtils.isNotBlank(testTime)) {
                    labItem.setTestTime(dateStringtoDate(testTime));
                }
            }
        }
        // 判断是否存储过,存储过直接跳过
        LabItem oItem = labItemService.getItem(labItem.getTestFlag());
        if (null != oItem) {
            labItem.setId(oItem.getId());
            return;
        }
        
        labItemService.saveItem(labItem);
    }


    public String cleanBlank(String str, String clean) {
        return str.replaceAll(clean, "").replaceAll("\t", "").replaceAll("\r", "").trim();
    }

    /**
     * 保存通道信息
     *
     * @param contentList
     * @return
     */
    public void saveLabItemSensorConfig(List<String> contentList, LabItem labItem) {
        if (0 < labItemColumnConfigService.countByQuery(labItem.getTestFlag())) {
            return;
        }
        List<LabItemColumnConfig> columnConfigList = new ArrayList<>(contentList.size());
        for (String s : contentList) {
            // 一条信息是一个通道
            LabItemColumnConfig labItemColumnConfig = new LabItemColumnConfig();
            String[] v = s.split("\\t");
            labItemColumnConfig.setColName(v[2]);
            labItemColumnConfig.setColCaption(v[4]);
            labItemColumnConfig.setTestFlag(labItem.getTestFlag());
            labItemColumnConfig.setLabRoomGuid(labItem.getLabRoomGuid());
            columnConfigList.add(labItemColumnConfig);
        }
        labItemColumnConfigService.saveColumns(columnConfigList);
    }


    /**
     * 保存实验数据
     *
     * @param contentList
     * @param labItem
     * @return
     */
    public boolean saveBatchData(List<String> contentList, LabItem labItem) {
        // 通道数量
        int count = labItemColumnConfigService.countByQuery(labItem.getTestFlag());
        if (count <= 200) {
            for (String s : contentList) {
                // 一行数据是一个时刻点的
                String[] v = s.split("\\t");
                String[] v1 = Arrays.copyOfRange(v, 2,count+2);
                saveDatas(1,count,"lab_item_data",Arrays.asList(v1),v[0], dateStringtoDate(v[1]));
            }
        } else if (200 < count && count <= 400) {
            for (String s : contentList) {
                String[] v = s.split("\\t");
                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, count+2);
                saveDatas(201,count,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));
            }
        }else if(400 < count && count <= 600){
            for (String s : contentList) {
                String[] v = s.split("\\t");
                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, count+2);
                saveDatas(401,count,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));
            }
        }else if(600 < count && count <= 800){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, count+2);
                saveDatas(601,count,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));
            }
        }else if(800 < count && count <= 1000){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, count+2);
                saveDatas(801,count,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));
            }
        }else if(1000 < count && count <= 1200){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, count+2);
                saveDatas(1001,count,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));
            }
        }else if(1200 < count && count <= 1400){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, 1202);
                saveDatas(1001,1200,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));

                String[] v7 = Arrays.copyOfRange(v, 1202, count+2);
                saveDatas(1201,count,"lab_item_data_twelve",Arrays.asList(v7),v[0],dateStringtoDate(v[1]));
            }
        }else if(1400 < count && count <= 1600){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, 1202);
                saveDatas(1001,1200,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));

                String[] v7 = Arrays.copyOfRange(v, 1202, 1402);
                saveDatas(1201,1400,"lab_item_data_twelve",Arrays.asList(v7),v[0],dateStringtoDate(v[1]));

                String[] v8 = Arrays.copyOfRange(v, 1402, count+2);
                saveDatas(1401,count,"lab_item_data_fortheen",Arrays.asList(v8),v[0],dateStringtoDate(v[1]));
            }
        }else if(1600 < count && count <= 1800){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, 1202);
                saveDatas(1001,1200,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));

                String[] v7 = Arrays.copyOfRange(v, 1202, 1402);
                saveDatas(1201,1400,"lab_item_data_twelve",Arrays.asList(v7),v[0],dateStringtoDate(v[1]));

                String[] v8 = Arrays.copyOfRange(v, 1402, 1602);
                saveDatas(1401,1600,"lab_item_data_fortheen",Arrays.asList(v8),v[0],dateStringtoDate(v[1]));

                String[] v9 = Arrays.copyOfRange(v, 1602, count+2);
                saveDatas(1601,count,"lab_item_data_sixtheen",Arrays.asList(v9),v[0],dateStringtoDate(v[1]));
            }
        }else if(1800 < count && count <= 2000){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, 1202);
                saveDatas(1001,1200,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));

                String[] v7 = Arrays.copyOfRange(v, 1202, 1402);
                saveDatas(1201,1400,"lab_item_data_twelve",Arrays.asList(v7),v[0],dateStringtoDate(v[1]));

                String[] v8 = Arrays.copyOfRange(v, 1402, 1602);
                saveDatas(1401,1600,"lab_item_data_fortheen",Arrays.asList(v8),v[0],dateStringtoDate(v[1]));

                String[] v9 = Arrays.copyOfRange(v, 1602, 1802);
                saveDatas(1601,1800,"lab_item_data_sixtheen",Arrays.asList(v9),v[0],dateStringtoDate(v[1]));

                String[] v10 = Arrays.copyOfRange(v, 1802, count+2);
                saveDatas(1801,count,"lab_item_data_eighttheen",Arrays.asList(v10),v[0],dateStringtoDate(v[1]));
            }
        }else if(2000 < count && count <= 2200){
            for (String s : contentList) {
                String[] v = s.split("\\t");

                String[] v1 = Arrays.copyOfRange(v, 2, 202);
                saveDatas(1,200,"lab_item_data",Arrays.asList(v1),v[0],dateStringtoDate(v[1]));

                String[] v2 = Arrays.copyOfRange(v, 202, 402);
                saveDatas(201,400,"lab_item_data_two",Arrays.asList(v2),v[0],dateStringtoDate(v[1]));

                String[] v3 = Arrays.copyOfRange(v, 402, 602);
                saveDatas(401,600,"lab_item_data_four",Arrays.asList(v3),v[0],dateStringtoDate(v[1]));

                String[] v4 = Arrays.copyOfRange(v, 602, 802);
                saveDatas(601,800,"lab_item_data_six",Arrays.asList(v4),v[0],dateStringtoDate(v[1]));

                String[] v5 = Arrays.copyOfRange(v, 802, 1002);
                saveDatas(801,1000,"lab_item_data_eight",Arrays.asList(v5),v[0],dateStringtoDate(v[1]));

                String[] v6 = Arrays.copyOfRange(v, 1002, 1202);
                saveDatas(1001,1200,"lab_item_data_ten",Arrays.asList(v6),v[0],dateStringtoDate(v[1]));

                String[] v7 = Arrays.copyOfRange(v, 1202, 1402);
                saveDatas(1201,1400,"lab_item_data_twelve",Arrays.asList(v7),v[0],dateStringtoDate(v[1]));

                String[] v8 = Arrays.copyOfRange(v, 1402, 1602);
                saveDatas(1401,1600,"lab_item_data_fortheen",Arrays.asList(v8),v[0],dateStringtoDate(v[1]));

                String[] v9 = Arrays.copyOfRange(v, 1602, 1802);
                saveDatas(1601,1800,"lab_item_data_sixtheen",Arrays.asList(v9),v[0],dateStringtoDate(v[1]));

                String[] v10 = Arrays.copyOfRange(v, 1802, 2002);
                saveDatas(1801,2000,"lab_item_data_eighttheen",Arrays.asList(v10),v[0],dateStringtoDate(v[1]));

                String[] v11 = Arrays.copyOfRange(v, 2002, count+2);
                saveDatas(2001,count,"lab_item_data_twenty",Arrays.asList(v11),v[0],dateStringtoDate(v[1]));
            }
        }
        return true;
    }


    private void saveDatas(int begin, int end, String table, List<String> datas, String testFlag, Date timePoint) {
        List<String> columns = new ArrayList<>();
        for (int i = begin; i <= end; i++) {
            columns.add("t" + i);
        }
        labItemDataService.saveBySql(columns, datas, table, testFlag, timePoint);
    }

    public static Date dateStringtoDate(String date) {
        if (org.apache.commons.lang3.StringUtils.isBlank(date)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                return sdf.parse(date);
            } catch (Exception ee) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    return sdf.parse(date);
                } catch (Exception eee) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        return sdf.parse(date);
                    } catch (Exception eeee) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                            return sdf.parse(date);
                        } catch (Exception eeeee) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                                return sdf.parse(date);
                            } catch (Exception eeeeee) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    return sdf.parse(date);
                                } catch (Exception eeeeeee) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                        return sdf.parse(date);
                                    } catch (Exception eeeeeeee) {
                                        try {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                                            return sdf.parse(date);
                                        } catch (Exception eeeeeeeee) {
                                            return null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }






    public void insertData(){
        File file = new File("D:\\CLab\\100HP焓差\\100HP焓差C室\\20210413124226\\测试数据\\LabItemData_202104131651.lab");
//            for(int i=0;i<1000;i++){
//                insertByPoint(file);
//            }

    }








}


