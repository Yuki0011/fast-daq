package com.mhaq.collect.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhaq.collect.entity.LabItemData;
import com.mhaq.collect.mapper.LabItemDataMapper;
import com.mhaq.collect.service.ILabItemDataService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 实验数据 服务实现类
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
@Service
public class LabItemDataServiceImpl extends ServiceImpl<LabItemDataMapper, LabItemData> implements ILabItemDataService {


    @Override
    public void saveBySql(List<String> columns, List<String> datas, String table, String testFlag, Date timePoint) {
         baseMapper.saveBySql(columns,datas,table,testFlag,timePoint);
    }


}
