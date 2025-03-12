package com.mhaq.collect.entity.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * <p>
 * JSON Enum 基础类
 * </p>
 *
 * @author hubin
 * @since 2017-10-17
 */
public interface IBaseEnum extends IEnum {

    /**
     * 操作名称
     */
    String getName();

    /**
     * 显示描述
     */
    String getDisplay();
}
