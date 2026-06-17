package com.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.entity.ParkSpace;

import java.util.List;

/**
 * 停车位服务接口
 *
 * 定义停车位管理相关的业务方法
 */
public interface ParkSpaceService {

    /**
     * 新增停车位
     *
     * @param parkSpace 停车位实体
     * @param currentUserRole 当前登录用户角色
     * @return 是否添加成功
     */
    boolean saveSpace(ParkSpace parkSpace, Integer currentUserRole);

    /**
     * 删除停车位
     *
     * @param id 停车位ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否删除成功
     */
    boolean deleteSpaceById(Long id, Integer currentUserRole);

    /**
     * 更新停车位
     *
     * @param parkSpace 停车位实体（包含ID）
     * @param currentUserRole 当前登录用户角色
     * @return 是否更新成功
     */
    boolean updateSpace(ParkSpace parkSpace, Integer currentUserRole);

    /**
     * 根据ID查询停车位
     *
     * @param id 停车位ID
     * @param currentUserRole 当前登录用户角色
     * @return 停车位实体
     */
    ParkSpace getSpaceById(Long id, Integer currentUserRole);

    /**
     * 根据区域ID查询所有停车位
     *
     * @param areaId 区域ID
     * @param currentUserRole 当前登录用户角色
     * @return 该区域的所有停车位列表
     */
    List<ParkSpace> getSpacesByAreaId(Long areaId, Integer currentUserRole);

    /**
     * 分页查询停车位列表（带权限控制）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param areaId 区域ID（可选）
     * @param spaceCode 停车位编码（可选，用于模糊查询）
     * @param spaceName 停车位名称（可选，用于模糊查询）
     * @param status 状态（可选）
     * @param currentUserRole 当前登录用户角色
     * @return 分页停车位列表
     */
    IPage<ParkSpace> getSpaceListPage(int pageNum, int pageSize, Long areaId, String spaceCode, String spaceName, Integer status, Integer currentUserRole);

    /**
     * 检查停车位编码是否已存在
     *
     * @param spaceCode 停车位编码
     * @return 是否存在
     */
    boolean existsBySpaceCode(String spaceCode);

    /**
     * 检查停车位编码是否已存在（排除指定ID）
     *
     * @param spaceCode 停车位编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean existsBySpaceCodeExcludeId(String spaceCode, Long excludeId);
}