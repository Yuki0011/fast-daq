package com.mhaq.collect.entity.enums;



import java.io.Serializable;

/**
 * @author xutao
 * @date 2020-08-03 17:17
 * @description 枚举类
 */
public enum FileTypeEnum implements IBaseEnum {



    LAB_ITEM("LabItem.lab", "实验信息"),
    LAB_ITEM_SENSOR_CONFIG("LabItemSensorConfig.lab", "通道信息"),
    LAB_ITEM_DATA("LabItemData", "实验数据");




    private String value;
    private String display;

    FileTypeEnum(final String value, String display) {
        this.value = value;
        this.display = display;
    }


    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getDisplay() {
        return display;
    }
}
