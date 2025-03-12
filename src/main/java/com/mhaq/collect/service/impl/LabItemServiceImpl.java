package com.mhaq.collect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhaq.collect.entity.LabItem;
import com.mhaq.collect.mapper.LabItemMapper;
import com.mhaq.collect.service.ILabItemService;
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
public class LabItemServiceImpl extends ServiceImpl<LabItemMapper, LabItem> implements ILabItemService {


    @Override
    public LabItem getItem(String testFlag) {
        QueryWrapper queryWrapper =  new QueryWrapper();
        queryWrapper.eq("test_flag",testFlag);
        queryWrapper.last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void saveItem(LabItem item) {
        baseMapper.insert(item);
    }

    @Override
    public Long getLastPoint(String testFlag) {
        return baseMapper.selectLastPoint(testFlag);
    }

    @Override
    public String getLastVersion() {
        return baseMapper.getLastVersion();
    }
}
