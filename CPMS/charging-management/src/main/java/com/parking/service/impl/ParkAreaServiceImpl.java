package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.common.BusinessException;
import com.parking.entity.ParkArea;
import com.parking.mapper.ParkAreaMapper;
import com.parking.service.ParkAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 停车区域服务实现类
 *
 * 实现停车区域管理相关的业务逻辑，包含权限控制
 */
@Service
public class ParkAreaServiceImpl implements ParkAreaService {

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
        // 如果角色为null，说明未正确获取用户信息
        if (currentUserRole == null) {
            throw new BusinessException(401, "请先登录");
        }
        // 普通用户(role=2)无权访问设备管理
        if (currentUserRole == ROLE_USER) {
            throw new BusinessException(403, "您没有权限执行此操作");
        }
    }

    @Override
    public boolean saveArea(ParkArea parkArea, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 验证区域编码是否已存在
        if (existsByAreaCode(parkArea.getAreaCode())) {
            throw new BusinessException("区域编码已存在");
        }

        // 设置默认状态为运营中
        if (parkArea.getStatus() == null) {
            parkArea.setStatus(1);
        }

        // 设置默认停车位总数为0
        if (parkArea.getTotalSpaces() == null) {
            parkArea.setTotalSpaces(0);
        }

        // 执行插入
        return parkAreaMapper.insert(parkArea) > 0;
    }

    @Override
    public boolean deleteAreaById(Long id, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 检查区域是否存在
        ParkArea area = parkAreaMapper.selectById(id);
        if (area == null) {
            throw new BusinessException("区域不存在");
        }

        // 执行删除
        return parkAreaMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateArea(ParkArea parkArea, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        // 检查区域是否存在
        ParkArea existingArea = parkAreaMapper.selectById(parkArea.getId());
        if (existingArea == null) {
            throw new BusinessException("区域不存在");
        }

        // 如果修改了区域编码，检查新编码是否已存在
        if (StringUtils.hasText(parkArea.getAreaCode())
                && !parkArea.getAreaCode().equals(existingArea.getAreaCode())
                && existsByAreaCode(parkArea.getAreaCode())) {
            throw new BusinessException("区域编码已存在");
        }

        // 执行更新
        return parkAreaMapper.updateById(parkArea) > 0;
    }

    @Override
    public ParkArea getAreaById(Long id, Integer currentUserRole) {
        // 检查权限
        checkPermission(currentUserRole);

        return parkAreaMapper.selectById(id);
    }

    @Override
    public IPage<ParkArea> getAreaListPage(int pageNum, int pageSize, String areaCode, String areaName, Integer status, Integer currentUserRole) {
        // 只检查是否登录，不检查角色权限
        // 普通用户(role=2)可以查看停车区域列表（用于模拟停车登记）
        if (currentUserRole == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 创建分页对象
        Page<ParkArea> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();

        // 区域编码模糊查询
        if (StringUtils.hasText(areaCode)) {
            queryWrapper.like(ParkArea::getAreaCode, areaCode);
        }

        // 区域名称模糊查询
        if (StringUtils.hasText(areaName)) {
            queryWrapper.like(ParkArea::getAreaName, areaName);
        }

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(ParkArea::getStatus, status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ParkArea::getCreateTime);

        // 执行分页查询
        return parkAreaMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean existsByAreaCode(String areaCode) {
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getAreaCode, areaCode);
        return parkAreaMapper.exists(queryWrapper);
    }

    @Override
    public boolean existsByAreaCodeExcludeId(String areaCode, Long excludeId) {
        LambdaQueryWrapper<ParkArea> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkArea::getAreaCode, areaCode)
                    .ne(ParkArea::getId, excludeId);
        return parkAreaMapper.exists(queryWrapper);
    }
}