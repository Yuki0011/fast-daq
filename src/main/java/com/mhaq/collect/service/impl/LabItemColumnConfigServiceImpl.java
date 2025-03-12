package com.mhaq.collect.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhaq.collect.entity.LabItemColumnConfig;
import com.mhaq.collect.mapper.LabItemColumnConfigMapper;
import com.mhaq.collect.service.ILabItemColumnConfigService;
import org.springframework.stereotype.Service;

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
public class LabItemColumnConfigServiceImpl extends ServiceImpl<LabItemColumnConfigMapper, LabItemColumnConfig> implements ILabItemColumnConfigService {


    @Override
    public int countByQuery(String testFlag) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("test_flag",testFlag);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public void saveColumns(List<LabItemColumnConfig> list) {
         saveBatch(list,list.size());
    }
}
