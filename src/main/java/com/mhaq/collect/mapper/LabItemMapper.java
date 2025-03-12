package com.mhaq.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhaq.collect.entity.LabItem;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 实验信息 Mapper 接口
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface LabItemMapper extends BaseMapper<LabItem> {

    Long selectLastPoint(@Param("testFlag") String testFlag);

    String getLastVersion();
}
