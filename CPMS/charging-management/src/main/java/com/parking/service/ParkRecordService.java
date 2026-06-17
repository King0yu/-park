package com.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.entity.ParkRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车记录服务接口
 *
 * 定义停车记录管理相关的业务方法，包含权限控制
 * 
 * 权限规则：
 * - 普通用户(车主，role=2)：只能查看和操作自己的停车记录
 * - 管理员(物业，role=1)：可以查看和管理所有停车记录
 * - 超级管理员(总管理，role=0)：可以查看和管理所有停车记录
 */
public interface ParkRecordService {

    /**
     * 新增停车记录
     *
     * @param parkRecord 停车记录实体
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否添加成功
     */
    boolean addRecord(ParkRecord parkRecord, Long currentUserId, Integer currentUserRole);

    /**
     * 结束停车记录（完成停车）
     *
     * @param recordId 记录ID
     * @param endTime 结束时间
     * @param parkingDuration 停车时长（小时）
     * @param totalCost 总费用（元）
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否更新成功
     */
    boolean endRecord(Long recordId, LocalDateTime endTime, BigDecimal parkingDuration,
                      BigDecimal totalCost, Long currentUserId, Integer currentUserRole);

    /**
     * 取消记录
     *
     * @param recordId 记录ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否取消成功
     */
    boolean cancelRecord(Long recordId, Long currentUserId, Integer currentUserRole);

    /**
     * 根据ID删除记录
     *
     * @param recordId 记录ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否删除成功
     */
    boolean deleteRecord(Long recordId, Long currentUserId, Integer currentUserRole);

    /**
     * 根据ID查询记录
     *
     * @param recordId 记录ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 记录实体（包含关联信息）
     */
    ParkRecord getRecordById(Long recordId, Long currentUserId, Integer currentUserRole);

    /**
     * 分页查询记录列表（带权限控制）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param userId 用户ID（可选，筛选指定用户的记录）
     * @param spaceId 停车位ID（可选，筛选指定停车位的记录）
     * @param status 记录状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param recordNo 记录编号（可选，模糊查询）
     * @param username 用户名（可选，模糊查询）
     * @param spaceCode 停车位编码（可选，模糊查询）
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 分页记录列表
     */
    IPage<ParkRecord> getRecordListPage(int pageNum, int pageSize, Long userId, Long spaceId,
                                        Integer status, LocalDateTime startTime, LocalDateTime endTime,
                                        String recordNo, String username, String spaceCode,
                                        Long currentUserId, Integer currentUserRole);

    /**
     * 查询用户的记录列表（普通用户查看自己的记录）
     *
     * @param userId 用户ID
     * @return 用户的记录列表
     */
    List<ParkRecord> getUserRecords(Long userId);

    /**
     * 查询用户的进行中记录（待停车、停车中）
     *
     * @param userId 用户ID
     * @return 用户的进行中记录列表
     */
    List<ParkRecord> getUserActiveRecords(Long userId);

    /**
     * 统计用户记录总数
     *
     * @param userId 用户ID
     * @return 记录总数
     */
    Long countUserRecords(Long userId);

    /**
     * 统计用户累计停车时长
     *
     * @param userId 用户ID
     * @return 累计停车时长（小时）
     */
    BigDecimal sumUserParkingDuration(Long userId);

    /**
     * 统计用户累计消费金额
     *
     * @param userId 用户ID
     * @return 累计消费金额（元）
     */
    BigDecimal sumUserAmount(Long userId);

    /**
     * 生成唯一记录编号
     *
     * @return 记录编号（格式：PRK+年月日+4位序号）
     */
    String generateRecordNo();

    /**
     * 自动计算并结束停车记录
     * 根据停车时长自动计算费用
     *
     * @param recordId 记录ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否更新成功
     */
    boolean autoEndRecord(Long recordId, Long currentUserId, Integer currentUserRole);

    /**
     * 模拟停车（创建停车记录）
     * 创建一条停车状态为「已占用」的停车记录，同时更新停车位状态为「已占用」，并同步车牌号
     *
     * @param userId 用户ID（从请求头获取）
     * @param spaceId 停车位ID
     * @param carNumber 车牌号
     * @return 创建的停车记录实体
     */
    ParkRecord simulateCreateRecord(Long userId, Long spaceId, String carNumber);
}