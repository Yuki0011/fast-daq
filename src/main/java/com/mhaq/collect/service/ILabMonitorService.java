package com.mhaq.collect.service;



import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mhaq.collect.entity.LabMonitor;


/**
 * <p>
 * 实验信息 服务类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
@DS("item")
public interface ILabMonitorService extends IService<LabMonitor> {



    LabMonitor getMonitor(AbstractWrapper query);
}
