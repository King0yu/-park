package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.entity.ParkRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车记录Mapper接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<ParkRecord> 接口，提供基础的 CRUD 操作：
 * - insert(ParkRecord entity) - 插入记录
 * - deleteById(Serializable id) - 根据ID删除
 * - updateById(ParkRecord entity) - 根据ID更新
 * - selectById(Serializable id) - 根据ID查询
 * - selectList(Wrapper<ParkRecord> queryWrapper) - 条件查询列表
 * - selectPage(IPage<ParkRecord> page, Wrapper<ParkRecord> queryWrapper) - 分页查询
 *
 * 如需自定义查询方法，可在此接口中添加方法定义，并在对应的 XML 文件中编写 SQL
 */
@Mapper
public interface ParkRecordMapper extends BaseMapper<ParkRecord> {

    /**
     * 根据用户ID查询停车记录列表
     *
     * @param userId 用户ID
     * @return 该用户的所有停车记录列表
     */
    @Select("SELECT * FROM park_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ParkRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据停车位ID查询停车记录列表
     *
     * @param spaceId 停车位ID
     * @return 该停车位的所有停车记录列表
     */
    @Select("SELECT * FROM park_record WHERE space_id = #{spaceId} ORDER BY create_time DESC")
    List<ParkRecord> selectBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 根据区域ID查询停车记录列表
     *
     * @param areaId 区域ID
     * @return 该区域的所有停车记录列表
     */
    @Select("SELECT * FROM park_record WHERE area_id = #{areaId} ORDER BY create_time DESC")
    List<ParkRecord> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询指定状态的停车记录列表
     *
     * @param status 记录状态
     * @return 指定状态的记录列表
     */
    @Select("SELECT * FROM park_record WHERE status = #{status} ORDER BY create_time DESC")
    List<ParkRecord> selectByStatus(@Param("status") Integer status);

    /**
     * 查询用户的进行中记录（待停车、停车中）
     *
     * @param userId 用户ID
     * @return 用户的进行中记录列表
     */
    @Select("SELECT * FROM park_record WHERE user_id = #{userId} AND status IN (0, 1) ORDER BY create_time DESC")
    List<ParkRecord> selectUserActiveRecords(@Param("userId") Long userId);

    /**
     * 查询用户的进行中记录（待停车、停车中）- 关联停车位信息
     *
     * @param userId 用户ID
     * @return 用户的进行中记录列表（包含 spaceCode / spaceName）
     */
    @Select("SELECT pr.*, p.space_code AS spaceCode, p.space_name AS spaceName " +
            "FROM park_record pr " +
            "LEFT JOIN park_space p ON pr.space_id = p.id " +
            "WHERE pr.user_id = #{userId} AND pr.status IN (0, 1) " +
            "ORDER BY pr.create_time DESC")
    List<ParkRecord> selectUserActiveRecordsWithSpace(@Param("userId") Long userId);

    /**
     * 分页查询用户的记录 - 关联停车位信息（用于用户端 Dashboard）
     */
    @Select("SELECT pr.*, p.space_code AS spaceCode, p.space_name AS spaceName " +
            "FROM park_record pr " +
            "LEFT JOIN park_space p ON pr.space_id = p.id " +
            "WHERE pr.user_id = #{userId} AND pr.is_deleted = 0 " +
            "ORDER BY pr.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ParkRecord> selectUserRecordsPageWithSpace(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size);

    /**
     * 统计用户记录数（未删除）
     */
    @Select("SELECT COUNT(*) FROM park_record WHERE user_id = #{userId} AND is_deleted = 0")
    Long countUserRecordsNotDeleted(@Param("userId") Long userId);

    /**
     * 查询停车中的记录
     *
     * @return 停车中的记录列表
     */
    @Select("SELECT * FROM park_record WHERE status = 1 ORDER BY start_time DESC")
    List<ParkRecord> selectParkingRecords();

    /**
     * 查询今日完成的记录
     *
     * @return 今日完成的记录列表
     */
    @Select("SELECT * FROM park_record WHERE status = 2 AND DATE(create_time) = CURDATE() ORDER BY create_time DESC")
    List<ParkRecord> selectTodayCompletedRecords();

    /**
     * 统计用户记录总数
     *
     * @param userId 用户ID
     * @return 记录总数
     */
    @Select("SELECT COUNT(*) FROM park_record WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户累计停车时长
     *
     * @param userId 用户ID
     * @return 累计停车时长（小时）
     */
    @Select("SELECT COALESCE(SUM(parking_duration), 0) FROM park_record WHERE user_id = #{userId} AND status = 2")
    BigDecimal sumParkingDurationByUserId(@Param("userId") Long userId);

    /**
     * 统计用户累计消费金额
     *
     * @param userId 用户ID
     * @return 累计消费金额（元）
     */
    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM park_record WHERE user_id = #{userId} AND status = 2")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * 统计今日结算订单数（按结束时间，用于数据大屏）
     */
    @Select("SELECT COUNT(*) FROM park_record WHERE DATE(end_time) = CURDATE() AND status = 2")
    Long countTodayRecords();

    /**
     * 统计今日营收（按结束时间，即今日结算的订单总额）
     */
    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM park_record WHERE DATE(end_time) = CURDATE() AND status = 2")
    BigDecimal sumTodayAmount();

    /**
     * 查询停车记录详情（关联用户、区域、停车位信息）
     *
     * @param recordId 记录ID
     * @return 记录详情（包含关联信息）
     */
    ParkRecord selectRecordDetail(@Param("recordId") Long recordId);

    /**
     * 查询停车记录列表（关联用户、区域、停车位信息）
     *
     * @param userId 用户ID（可选）
     * @param spaceId 停车位ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 记录列表（包含关联信息）
     */
    List<ParkRecord> selectRecordListWithRelations(
            @Param("userId") Long userId,
            @Param("spaceId") Long spaceId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 分页查询停车记录列表（关联用户、区域、停车位信息）
     *
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param spaceId 停车位ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param recordNo 记录编号（可选）
     * @param username 用户名（可选）
     * @param spaceCode 停车位编码（可选）
     * @return 记录列表（包含关联信息）
     */
    IPage<ParkRecord> selectRecordListWithRelationsPage(
            IPage<ParkRecord> page,
            @Param("userId") Long userId,
            @Param("spaceId") Long spaceId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("recordNo") String recordNo,
            @Param("username") String username,
            @Param("spaceCode") String spaceCode,
            @Param("includeDeleted") Boolean includeDeleted
    );
}