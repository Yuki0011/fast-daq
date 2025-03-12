package com.mhaq.collect.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mhaq.collect.entity.LabMonitor;
import com.mhaq.collect.entity.LabMonitorData;

import java.util.Date;


/**
 * <p>
 * 实验信息 服务类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface ILabMonitorDataService extends IService<LabMonitorData> {


    LabMonitorData getByCondition(LabMonitorData labMonitorData);

    int countByQuery(LabMonitor labMonitorData, Date date);
}
