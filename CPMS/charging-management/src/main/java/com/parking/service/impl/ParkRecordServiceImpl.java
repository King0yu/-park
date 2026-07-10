package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.common.BusinessException;
import com.parking.entity.ParkRecord;
import com.parking.entity.ParkSpace;
import com.parking.mapper.ParkRecordMapper;
import com.parking.mapper.ParkSpaceMapper;
import com.parking.service.ParkRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 停车记录服务实现类
 *
 * 实现停车记录管理相关的业务逻辑，包含权限控制
 * 
 * 权限规则：
 * - 普通用户(车主，role=2)：只能查看和操作自己的停车记录
 * - 管理员(物业，role=1)：可以查看和管理所有停车记录
 * - 超级管理员(总管理，role=0)：可以查看和管理所有停车记录
 */
@Service
public class ParkRecordServiceImpl implements ParkRecordService {

    @Autowired
    private ParkRecordMapper parkRecordMapper;

    @Autowired
    private ParkSpaceMapper parkSpaceMapper;

    /**
     * 角色常量
     */
    private static final int ROLE_SUPER_ADMIN = 0;
    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_USER = 2;

    /**
     * 记录状态常量
     */
    private static final int RECORD_STATUS_PENDING = 0;    // 待停车
    private static final int RECORD_STATUS_PARKING = 1;    // 停车中
    private static final int RECORD_STATUS_COMPLETED = 2;  // 已完成
    private static final int RECORD_STATUS_CANCELLED = 3;  // 已取消
    private static final int RECORD_STATUS_ABNORMAL = 4;   // 异常结束

    @Override
    public boolean addRecord(ParkRecord parkRecord, Long currentUserId, Integer currentUserRole) {
        // 验证必要字段
        if (parkRecord.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (parkRecord.getAreaId() == null) {
            throw new BusinessException("区域ID不能为空");
        }
        if (parkRecord.getSpaceId() == null) {
            throw new BusinessException("停车位ID不能为空");
        }

        // 权限控制：普通用户(车主)只能创建自己的记录
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(parkRecord.getUserId())) {
                throw new BusinessException(403, "您只能创建自己的停车记录");
            }
        }

        // 生成记录编号
        parkRecord.setRecordNo(generateRecordNo());

        // 设置默认状态为待停车
        if (parkRecord.getStatus() == null) {
            parkRecord.setStatus(RECORD_STATUS_PENDING);
        }

        // 设置默认支付状态为未支付
        if (parkRecord.getPayStatus() == null) {
            parkRecord.setPayStatus(0);
        }

        // 设置创建时间
        if (parkRecord.getCreateTime() == null) {
            parkRecord.setCreateTime(LocalDateTime.now());
        }

        // 设置更新时间
        parkRecord.setUpdateTime(LocalDateTime.now());

        // 执行插入
        return parkRecordMapper.insert(parkRecord) > 0;
    }

    @Override
    public boolean endRecord(Long recordId, LocalDateTime endTime, BigDecimal parkingDuration,
                             BigDecimal totalCost, Long currentUserId, Integer currentUserRole) {
        // 检查记录是否存在
        ParkRecord record = parkRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("停车记录不存在");
        }

        // 权限控制：普通用户(车主)只能操作自己的记录
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(record.getUserId())) {
                throw new BusinessException(403, "您只能操作自己的停车记录");
            }
        }

        // 检查记录状态：只能结束待停车或停车中的记录
        if (record.getStatus() != RECORD_STATUS_PENDING && record.getStatus() != RECORD_STATUS_PARKING) {
            throw new BusinessException("只能结束待停车或停车中的记录");
        }

        // 更新记录信息
        record.setEndTime(endTime);
        record.setParkingDuration(parkingDuration != null ? parkingDuration : BigDecimal.ZERO);
        record.setTotalCost(totalCost != null ? totalCost : BigDecimal.ZERO);
        record.setStatus(RECORD_STATUS_COMPLETED);
        record.setPayStatus(1); // 完成停车时设置为已支付
        record.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int result = parkRecordMapper.updateById(record);

        // 释放停车位：将状态恢复为空闲，清除车牌号
        if (result > 0 && record.getSpaceId() != null) {
            ParkSpace parkSpace = parkSpaceMapper.selectById(record.getSpaceId());
            if (parkSpace != null && parkSpace.getStatus() != null && parkSpace.getStatus() == 1) {
                parkSpace.setStatus(0);
                parkSpace.setCarNumber(null);
                parkSpace.setUpdateTime(LocalDateTime.now());
                parkSpaceMapper.updateById(parkSpace);
            }
        }

        return result > 0;
    }

    @Override
    public boolean cancelRecord(Long recordId, Long currentUserId, Integer currentUserRole) {
        // 检查记录是否存在
        ParkRecord record = parkRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("停车记录不存在");
        }

        // 权限控制：普通用户(车主)只能取消自己的记录
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(record.getUserId())) {
                throw new BusinessException(403, "您只能取消自己的停车记录");
            }
        }

        // 检查记录状态：只能取消待停车的记录
        if (record.getStatus() != RECORD_STATUS_PENDING) {
            throw new BusinessException("只能取消待停车的记录");
        }

        // 更新记录状态为已取消
        record.setStatus(RECORD_STATUS_CANCELLED);
        record.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int result = parkRecordMapper.updateById(record);

        // 释放停车位：取消记录时同步释放车位
        if (result > 0 && record.getSpaceId() != null) {
            ParkSpace parkSpace = parkSpaceMapper.selectById(record.getSpaceId());
            if (parkSpace != null && parkSpace.getStatus() != null && parkSpace.getStatus() == 1) {
                parkSpace.setStatus(0);
                parkSpace.setCarNumber(null);
                parkSpace.setUpdateTime(LocalDateTime.now());
                parkSpaceMapper.updateById(parkSpace);
            }
        }

        return result > 0;
    }

    @Override
    public boolean deleteRecord(Long recordId, Long currentUserId, Integer currentUserRole) {
        // 检查记录是否存在
        ParkRecord record = parkRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("停车记录不存在");
        }

        // 权限控制：普通用户(车主)只能删除自己的记录
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(record.getUserId())) {
                throw new BusinessException(403, "您只能删除自己的停车记录");
            }
        }

        record.setIsDeleted(1);
        record.setUpdateTime(LocalDateTime.now());
        return parkRecordMapper.updateById(record) > 0;
    }

    @Override
    public ParkRecord getRecordById(Long recordId, Long currentUserId, Integer currentUserRole) {
        // 查询记录详情（包含关联信息）
        ParkRecord record = parkRecordMapper.selectRecordDetail(recordId);
        if (record == null) {
            return null;
        }

        boolean isAdmin = (currentUserRole != null && (currentUserRole == ROLE_SUPER_ADMIN || currentUserRole == ROLE_ADMIN));

        if (!isAdmin && record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            return null;
        }

        if (!isAdmin) {
            if (!currentUserId.equals(record.getUserId())) {
                return null;
            }
        }

        return record;
    }

    @Override
    public IPage<ParkRecord> getRecordListPage(int pageNum, int pageSize, Long userId, Long spaceId,
                                               Integer status, LocalDateTime startTime, LocalDateTime endTime,
                                               String recordNo, String username, String spaceCode,
                                               Long currentUserId, Integer currentUserRole) {
        // 创建分页对象
        Page<ParkRecord> page = new Page<>(pageNum, pageSize);

        // 权限控制：普通用户(车主)只能查看自己的记录
        Long finalUserId = userId;
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            finalUserId = currentUserId;
        }

        // 管理员(物业)可查看已删除记录，普通用户(车主)只看到未删除的
        boolean includeDeleted = (currentUserRole != null && (currentUserRole == ROLE_SUPER_ADMIN || currentUserRole == ROLE_ADMIN));

        return parkRecordMapper.selectRecordListWithRelationsPage(
                page,
                finalUserId,
                spaceId,
                status,
                startTime,
                endTime,
                recordNo,
                username,
                spaceCode,
                includeDeleted
        );
    }

    @Override
    public List<ParkRecord> getUserRecords(Long userId) {
        LambdaQueryWrapper<ParkRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkRecord::getUserId, userId);
        queryWrapper.orderByDesc(ParkRecord::getCreateTime);
        return parkRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<ParkRecord> getUserActiveRecords(Long userId) {
        return parkRecordMapper.selectUserActiveRecords(userId);
    }

    @Override
    public List<ParkRecord> getUserActiveRecordsWithSpace(Long userId) {
        return parkRecordMapper.selectUserActiveRecordsWithSpace(userId);
    }

    @Override
    public java.util.Map<String, Object> getUserRecordsPageWithSpace(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<ParkRecord> records = parkRecordMapper.selectUserRecordsPageWithSpace(userId, offset, pageSize);
        Long total = parkRecordMapper.countUserRecordsNotDeleted(userId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    @Override
    public Long countUserRecordsNotDeleted(Long userId) {
        return parkRecordMapper.countUserRecordsNotDeleted(userId);
    }

    @Override
    public Long countUserRecords(Long userId) {
        return parkRecordMapper.countByUserId(userId);
    }

    @Override
    public BigDecimal sumUserParkingDuration(Long userId) {
        return parkRecordMapper.sumParkingDurationByUserId(userId);
    }

    @Override
    public BigDecimal sumUserAmount(Long userId) {
        return parkRecordMapper.sumAmountByUserId(userId);
    }

    @Override
    public String generateRecordNo() {
        // 格式：PRK + 年月日(yyyyMMdd) + 4位序号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PRK" + dateStr;

        // 查询当天最大序号
        LambdaQueryWrapper<ParkRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(ParkRecord::getRecordNo, prefix);
        queryWrapper.orderByDesc(ParkRecord::getRecordNo);
        
        List<ParkRecord> records = parkRecordMapper.selectList(queryWrapper);
        
        int maxSeq = 0;
        if (!records.isEmpty()) {
            String maxRecordNo = records.get(0).getRecordNo();
            try {
                String seqStr = maxRecordNo.substring(prefix.length());
                maxSeq = Integer.parseInt(seqStr);
            } catch (Exception e) {
                maxSeq = 0;
            }
        }

        // 生成新序号（4位，不足补0）
        int newSeq = maxSeq + 1;
        return prefix + String.format("%04d", newSeq);
    }

    @Override
    public Long countTodayRecords() {
        return parkRecordMapper.countTodayRecords();
    }

    @Override
    public BigDecimal sumTodayAmount() {
        BigDecimal result = parkRecordMapper.sumTodayAmount();
        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public ParkRecord simulateCreateRecord(Long userId, Long spaceId, String carNumber) {
        // 验证必要参数
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (spaceId == null) {
            throw new BusinessException("停车位ID不能为空");
        }

        // 验证停车位是否存在
        ParkSpace parkSpace = parkSpaceMapper.selectById(spaceId);
        if (parkSpace == null) {
            throw new BusinessException("停车位不存在");
        }

        // 验证停车位是否已被占用
        if (parkSpace.getStatus() != null && parkSpace.getStatus() == 1) {
            throw new BusinessException("该停车位已被占用，请选择其他停车位");
        }

        Long areaId = parkSpace.getAreaId();
        if (areaId == null) {
            throw new BusinessException("停车位所属区域信息缺失");
        }

        // 创建停车记录对象
        ParkRecord parkRecord = new ParkRecord();

        // 设置记录编号
        parkRecord.setRecordNo(generateRecordNo());

        // 设置用户ID
        parkRecord.setUserId(userId);

        // 设置区域ID和停车位ID
        parkRecord.setAreaId(areaId);
        parkRecord.setSpaceId(spaceId);

        // 设置车牌号
        parkRecord.setCarNumber(carNumber);

        // 设置状态为停车中（1-停车中）
        parkRecord.setStatus(RECORD_STATUS_PARKING);

        // 自动记录开始时间
        LocalDateTime now = LocalDateTime.now();
        parkRecord.setStartTime(now);
        parkRecord.setCreateTime(now);
        parkRecord.setUpdateTime(now);

        // 设置默认支付状态为未支付
        parkRecord.setPayStatus(0);

        // 设置软删除标记为0（未删除）
        parkRecord.setIsDeleted(0);

        // 设置单价（5元/小时）
        parkRecord.setUnitPrice(new BigDecimal("5"));

        // 设置初始停车时长和总费用为0
        parkRecord.setParkingDuration(BigDecimal.ZERO);
        parkRecord.setTotalCost(BigDecimal.ZERO);

        // 执行插入
        int result = parkRecordMapper.insert(parkRecord);
        if (result > 0) {
            // 更新停车位状态为已占用（1-已占用），并同步车牌号
            parkSpace.setStatus(1);
            parkSpace.setCarNumber(carNumber);
            parkSpace.setUpdateTime(LocalDateTime.now());
            parkSpaceMapper.updateById(parkSpace);

            // 返回创建的记录
            return parkRecordMapper.selectById(parkRecord.getId());
        }

        throw new BusinessException("停车记录创建失败");
    }

    @Override
    public boolean autoEndRecord(Long recordId, Long currentUserId, Integer currentUserRole) {
        // 检查记录是否存在
        ParkRecord record = parkRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("停车记录不存在");
        }

        // 权限控制：普通用户(车主)只能操作自己的记录
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(record.getUserId())) {
                throw new BusinessException(403, "您只能操作自己的停车记录");
            }
        }

        // 检查记录状态：只能结束待停车或停车中的记录
        if (record.getStatus() != RECORD_STATUS_PENDING && record.getStatus() != RECORD_STATUS_PARKING) {
            throw new BusinessException("只能结束待停车或停车中的记录");
        }

        // 获取当前时间作为结束时间
        LocalDateTime endTime = LocalDateTime.now();
        
        // 计算停车时长（小时）
        LocalDateTime startTime = record.getStartTime();
        if (startTime == null) {
            startTime = record.getCreateTime();
        }
        if (startTime == null) {
            startTime = endTime.minusMinutes(1);
        }
        
        // 计算时间差（秒）
        long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
        double hours = seconds / 3600.0;
        
        // 不足1小时按1小时计算
        double parkingHours = Math.ceil(hours);
        if (parkingHours < 1) {
            parkingHours = 1;
        }
        
        // 单价：5元/小时
        BigDecimal unitPrice = new BigDecimal("5");
        
        // 计算总费用 = 停车时长 × 单价
        BigDecimal totalCost = unitPrice.multiply(new BigDecimal(String.valueOf(parkingHours)));

        // 更新记录信息
        record.setEndTime(endTime);
        record.setParkingDuration(new BigDecimal(String.valueOf(parkingHours)));
        record.setTotalCost(totalCost);
        record.setStatus(RECORD_STATUS_COMPLETED);
        record.setPayStatus(1);
        record.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int result = parkRecordMapper.updateById(record);

        // 释放停车位：结算完成后将车位恢复为空闲
        if (result > 0 && record.getSpaceId() != null) {
            ParkSpace parkSpace = parkSpaceMapper.selectById(record.getSpaceId());
            if (parkSpace != null && parkSpace.getStatus() != null && parkSpace.getStatus() == 1) {
                parkSpace.setStatus(0);
                parkSpace.setCarNumber(null);
                parkSpace.setUpdateTime(LocalDateTime.now());
                parkSpaceMapper.updateById(parkSpace);
            }
        }

        return result > 0;
    }
}