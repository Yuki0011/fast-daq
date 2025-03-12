package com.mhaq.collect.entity.enums;




import java.io.Serializable;

/**
 * @author xutao
 * @date 2020-08-03 17:17
 * @description 科室类型枚举类
 */

public enum LabTypeEnum implements IBaseEnum {


    D_HC_HP_10("D_HC_HP_10", "10匹焓差D室");





    private String value;
    private String display;

    LabTypeEnum(final String value, String display) {
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
