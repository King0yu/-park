package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 停车位Mapper接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<ParkSpace> 接口，提供基础的 CRUD 操作：
 * - insert(ParkSpace entity) - 插入记录
 * - deleteById(Serializable id) - 根据ID删除
 * - updateById(ParkSpace entity) - 根据ID更新
 * - selectById(Serializable id) - 根据ID查询
 * - selectList(Wrapper<ParkSpace> queryWrapper) - 条件查询列表
 * - selectPage(IPage<ParkSpace> page, Wrapper<ParkSpace> queryWrapper) - 分页查询
 *
 * 如需自定义查询方法，可在此接口中添加方法定义，并在对应的 XML 文件中编写 SQL
 */
@Mapper
public interface ParkSpaceMapper extends BaseMapper<ParkSpace> {

    /**
     * 根据停车位编码查询
     *
     * @param spaceCode 停车位编码
     * @return 停车位实体
     */
    @Select("SELECT * FROM park_space WHERE space_code = #{spaceCode}")
    ParkSpace selectBySpaceCode(@Param("spaceCode") String spaceCode);

    /**
     * 根据区域ID查询所有停车位
     *
     * @param areaId 区域ID
     * @return 该区域的所有停车位列表
     */
    @Select("SELECT * FROM park_space WHERE area_id = #{areaId} ORDER BY space_code")
    List<ParkSpace> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询区域下可用的停车位（空闲状态）
     *
     * @param areaId 区域ID
     * @return 可用的停车位列表
     */
    @Select("SELECT * FROM park_space WHERE area_id = #{areaId} AND status = 0")
    List<ParkSpace> selectAvailableSpacesByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询所有空闲的停车位
     *
     * @return 空闲的停车位列表
     */
    @Select("SELECT * FROM park_space WHERE status = 0 ORDER BY create_time DESC")
    List<ParkSpace> selectAvailableSpaces();

    /**
     * 统计各状态停车位数量（用于数据大屏）
     *
     * @return key=status(0空闲/1占用/2故障/3维护), value=数量
     */
    @Select("SELECT status, COUNT(*) AS count FROM park_space GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus();
}