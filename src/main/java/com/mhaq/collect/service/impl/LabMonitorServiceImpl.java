package com.mhaq.collect.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhaq.collect.entity.LabMonitor;
import com.mhaq.collect.mapper.LabMonitorMapper;
import com.mhaq.collect.service.ILabMonitorService;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 实验信息 服务实现类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
@Service
@DS("item")
public class LabMonitorServiceImpl extends ServiceImpl<LabMonitorMapper, LabMonitor> implements ILabMonitorService {

    @Override
    public LabMonitor getMonitor(AbstractWrapper query) {
        return super.getOne(query);
    }
}
