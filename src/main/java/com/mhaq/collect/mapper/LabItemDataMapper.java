package com.mhaq.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhaq.collect.entity.LabItemData;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 实验数据 Mapper 接口
 * </p>
 *
 * @author xutao
 * @since 2020-07-30
 */
public interface LabItemDataMapper extends BaseMapper<LabItemData> {


    void saveBySql(@Param("columns") List<String> columns,@Param("datas") List<String> datas,@Param("table") String table,@Param("testFlag") String testFlag,@Param("timePoint") Date timePoint);





}
