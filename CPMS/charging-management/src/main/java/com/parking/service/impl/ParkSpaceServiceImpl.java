package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.common.BusinessException;
import com.parking.entity.ParkArea;
import com.parking.entity.ParkSpace;
import com.parking.mapper.ParkAreaMapper;
import com.parking.mapper.ParkSpaceMapper;
import com.parking.service.ParkSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 停车位服务实现类
 *
 * 实现停车位管理相关的业务逻辑，包含权限控制
 */
@Service
public class ParkSpaceServiceImpl implements ParkSpaceService {

    @Autowired
    private ParkSpaceMapper parkSpaceMapper;

    @Autowired
    private ParkAreaMapper parkAreaMapper;

    /**
     * 角色常量
     */
    private static final int ROLE_SUPER_ADMIN = 0;
    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_USER = 2;

    /**
     * 检查权限（普通用户无权限访问设备管理）
     * 超级管理员(role=0)和管理员(role=1)有权限访问
     * 普通用户(role=2)无权访问
     */
    private void checkPermission(Integer currentUserRole) {
        if (currentUserRole == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (currentUserRole == ROLE_USER) {
            throw new BusinessException(403, "您没有权限执行此操作");
        }
    }

    @Override
    public boolean saveSpace(ParkSpace parkSpace, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 验证所属区域是否存在
        ParkArea area = parkAreaMapper.selectById(parkSpace.getAreaId());
        if (area == null) {
            throw new BusinessException("所属区域不存在");
        }

        // 验证停车位编码是否已存在
        if (existsBySpaceCode(parkSpace.getSpaceCode())) {
            throw new BusinessException("停车位编码已存在");
        }

        // 设置默认状态为空闲
        if (parkSpace.getStatus() == null) {
            parkSpace.setStatus(0);
        }

        // 执行插入
        boolean success = parkSpaceMapper.insert(parkSpace) > 0;

        // 更新区域的停车位总数
        if (success) {
            Integer currentTotal = area.getTotalSpaces();
            if (currentTotal == null) {
                currentTotal = 0;
            }
            area.setTotalSpaces(currentTotal + 1);
            parkAreaMapper.updateById(area);
        }

        return success;
    }

    @Override
    public boolean deleteSpaceById(Long id, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 检查停车位是否存在
        ParkSpace space = parkSpaceMapper.selectById(id);
        if (space == null) {
            throw new BusinessException("停车位不存在");
        }

        // 执行删除
        boolean success = parkSpaceMapper.deleteById(id) > 0;

        // 更新区域的停车位总数
        if (success) {
            ParkArea area = parkAreaMapper.selectById(space.getAreaId());
            if (area != null) {
                Integer currentTotal = area.getTotalSpaces();
                if (currentTotal != null && currentTotal > 0) {
                    area.setTotalSpaces(currentTotal - 1);
                    parkAreaMapper.updateById(area);
                }
            }
        }

        return success;
    }

    @Override
    public boolean updateSpace(ParkSpace parkSpace, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 检查停车位是否存在
        ParkSpace existingSpace = parkSpaceMapper.selectById(parkSpace.getId());
        if (existingSpace == null) {
            throw new BusinessException("停车位不存在");
        }

        // 如果修改了所属区域，验证新区域是否存在
        if (parkSpace.getAreaId() != null
                && !parkSpace.getAreaId().equals(existingSpace.getAreaId())) {
            ParkArea newArea = parkAreaMapper.selectById(parkSpace.getAreaId());
            if (newArea == null) {
                throw new BusinessException("新所属区域不存在");
            }
        }

        // 如果修改了停车位编码，检查新编码是否已存在
        if (StringUtils.hasText(parkSpace.getSpaceCode())
                && !parkSpace.getSpaceCode().equals(existingSpace.getSpaceCode())
                && existsBySpaceCode(parkSpace.getSpaceCode())) {
            throw new BusinessException("停车位编码已存在");
        }

        // 执行更新
        return parkSpaceMapper.updateById(parkSpace) > 0;
    }

    @Override
    public ParkSpace getSpaceById(Long id, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        return parkSpaceMapper.selectById(id);
    }

    @Override
    public List<ParkSpace> getSpacesByAreaId(Long areaId, Integer currentUserRole) {
        // 只检查是否登录，不检查角色权限
        // 普通用户(role=2)可以查看停车位列表（用于模拟停车登记）
        if (currentUserRole == null) {
            throw new BusinessException(401, "请先登录");
        }

        return parkSpaceMapper.selectByAreaId(areaId);
    }

    @Override
    public IPage<ParkSpace> getSpaceListPage(int pageNum, int pageSize, Long areaId, String spaceCode, String spaceName, Integer status, Integer currentUserRole) {
        // 创建分页对象
        Page<ParkSpace> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<ParkSpace> queryWrapper = new LambdaQueryWrapper<>();

        // 区域ID筛选
        if (areaId != null) {
            queryWrapper.eq(ParkSpace::getAreaId, areaId);
        }

        // 停车位编码模糊查询
        if (StringUtils.hasText(spaceCode)) {
            queryWrapper.like(ParkSpace::getSpaceCode, spaceCode);
        }

        // 停车位名称模糊查询
        if (StringUtils.hasText(spaceName)) {
            queryWrapper.like(ParkSpace::getSpaceName, spaceName);
        }

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(ParkSpace::getStatus, status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ParkSpace::getCreateTime);

        // 执行分页查询
        return parkSpaceMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean existsBySpaceCode(String spaceCode) {
        LambdaQueryWrapper<ParkSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkSpace::getSpaceCode, spaceCode);
        return parkSpaceMapper.exists(queryWrapper);
    }

    @Override
    public boolean existsBySpaceCodeExcludeId(String spaceCode, Long excludeId) {
        LambdaQueryWrapper<ParkSpace> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkSpace::getSpaceCode, spaceCode)
                    .ne(ParkSpace::getId, excludeId);
        return parkSpaceMapper.exists(queryWrapper);
    }

    @Override
    public java.util.Map<Integer, Integer> countByStatus() {
        List<java.util.Map<String, Object>> raw = parkSpaceMapper.countByStatus();
        java.util.Map<Integer, Integer> result = new java.util.HashMap<>();
        if (raw != null) {
            for (java.util.Map<String, Object> row : raw) {
                Object statusObj = row.get("status");
                Object countObj = row.get("count");
                if (statusObj != null && countObj != null) {
                    result.put(((Number) statusObj).intValue(), ((Number) countObj).intValue());
                }
            }
        }
        return result;
    }
}