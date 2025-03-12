package com.mhaq.collect.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mhaq.collect.entity.LabItemColumnConfig;

import java.util.List;


/**
 * <p>
 * 通道信息 服务类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface ILabItemColumnConfigService extends IService<LabItemColumnConfig> {


    int countByQuery(String testFlag);

    void saveColumns(List<LabItemColumnConfig> list);


}
