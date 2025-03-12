package com.mhaq.collect.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mhaq.collect.entity.LabItemData;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 实验数据 服务类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface ILabItemDataService  extends IService<LabItemData> {

    void saveBySql(List<String> colums, List<String> datas, String table, String testFlag, Date timePoint);




}
