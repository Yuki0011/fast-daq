package com.mhaq.collect.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhaq.collect.entity.LabMonitor;
import com.mhaq.collect.entity.LabMonitorData;
import com.mhaq.collect.mapper.LabMonitorDataMapper;
import com.mhaq.collect.service.ILabMonitorDataService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * <p>
 * 实验信息 服务实现类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
@Service
public class LabMonitorDataServiceImpl extends ServiceImpl<LabMonitorDataMapper, LabMonitorData> implements ILabMonitorDataService {

    @Override
    public LabMonitorData getByCondition(LabMonitorData labMonitor) {
        QueryWrapper wrapper = new QueryWrapper(labMonitor);
        wrapper.last("limit 1");
        return super.getOne(wrapper);
    }

    @Override
    public int countByQuery(LabMonitor labMonitor,Date date) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("lab_room_guid", labMonitor.getLab());
        wrapper.eq("item", labMonitor.getItem());
        wrapper.eq("type", labMonitor.getType());
        wrapper.ge("time_point", labMonitor.getBeginDate());
        wrapper.le("time_point", date);
        return super.count(wrapper);
    }
}
