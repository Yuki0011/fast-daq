package com.mhaq.collect.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.mhaq.collect.entity.LabItem;



/**
 * <p>
 * 实验信息 服务类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface ILabItemService  extends IService<LabItem> {

    LabItem getItem(String testFlag);

    void saveItem(LabItem item);

    Long getLastPoint(String testFlag);

    String getLastVersion();
}
