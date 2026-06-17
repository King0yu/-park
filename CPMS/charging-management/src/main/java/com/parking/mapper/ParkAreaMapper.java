package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 停车区域Mapper接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<ParkArea> 接口，提供基础的 CRUD 操作：
 * - insert(ParkArea entity) - 插入记录
 * - deleteById(Serializable id) - 根据ID删除
 * - updateById(ParkArea entity) - 根据ID更新
 * - selectById(Serializable id) - 根据ID查询
 * - selectList(Wrapper<ParkArea> queryWrapper) - 条件查询列表
 * - selectPage(IPage<ParkArea> page, Wrapper<ParkArea> queryWrapper) - 分页查询
 *
 * 如需自定义查询方法，可在此接口中添加方法定义，并在对应的 XML 文件中编写 SQL
 */
@Mapper
public interface ParkAreaMapper extends BaseMapper<ParkArea> {

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    @Select("SELECT * FROM park_area WHERE area_code = #{areaCode}")
    ParkArea selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 查询所有运营中的区域
     *
     * @return 运营中的区域列表
     */
    @Select("SELECT * FROM park_area WHERE status = 1 ORDER BY create_time DESC")
    List<ParkArea> selectActiveAreas();

    /**
     * 根据区域名称模糊查询
     *
     * @param areaName 区域名称
     * @return 匹配的区域列表
     */
    @Select("SELECT * FROM park_area WHERE area_name LIKE CONCAT('%', #{areaName}, '%')")
    List<ParkArea> selectByAreaNameLike(@Param("areaName") String areaName);
}