package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<ParkConfig> 接口，提供基础的 CRUD 操作
 */
@Mapper
public interface ParkConfigMapper extends BaseMapper<ParkConfig> {
}
