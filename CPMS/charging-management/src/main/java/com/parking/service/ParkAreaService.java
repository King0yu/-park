package com.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.entity.ParkArea;

/**
 * 停车区域服务接口
 *
 * 定义停车区域管理相关的业务方法
 */
public interface ParkAreaService {

    /**
     * 新增停车区域
     *
     * @param parkArea 区域实体
     * @param currentUserRole 当前登录用户角色
     * @return 是否添加成功
     */
    boolean saveArea(ParkArea parkArea, Integer currentUserRole);

    /**
     * 删除停车区域
     *
     * @param id 区域ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否删除成功
     */
    boolean deleteAreaById(Long id, Integer currentUserRole);

    /**
     * 更新停车区域
     *
     * @param parkArea 区域实体（包含ID）
     * @param currentUserRole 当前登录用户角色
     * @return 是否更新成功
     */
    boolean updateArea(ParkArea parkArea, Integer currentUserRole);

    /**
     * 根据ID查询停车区域
     *
     * @param id 区域ID
     * @param currentUserRole 当前登录用户角色
     * @return 区域实体
     */
    ParkArea getAreaById(Long id, Integer currentUserRole);

    /**
     * 分页查询停车区域列表（带权限控制）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param areaCode 区域编码（可选，用于模糊查询）
     * @param areaName 区域名称（可选，用于模糊查询）
     * @param status 状态（可选）
     * @param currentUserRole 当前登录用户角色
     * @return 分页区域列表
     */
    IPage<ParkArea> getAreaListPage(int pageNum, int pageSize, String areaCode, String areaName, Integer status, Integer currentUserRole);

    /**
     * 检查区域编码是否已存在
     *
     * @param areaCode 区域编码
     * @return 是否存在
     */
    boolean existsByAreaCode(String areaCode);

    /**
     * 检查区域编码是否已存在（排除指定ID）
     *
     * @param areaCode 区域编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean existsByAreaCodeExcludeId(String areaCode, Long excludeId);
}