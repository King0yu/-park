package com.parking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.common.R;
import com.parking.entity.ParkArea;
import com.parking.service.ParkAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 停车区域控制器
 *
 * 提供 RESTful 风格的停车区域管理接口
 * 接口路径：/api/area
 *
 * 权限说明：
 * - super_admin (role=0): 完全控制
 * - admin (role=1): 完全控制
 * - user (role=2): 无权限访问
 */
@RestController
@RequestMapping("/api/area")
@CrossOrigin(origins = "*")
public class ParkAreaController {

    @Autowired
    private ParkAreaService parkAreaService;

    /**
     * 角色常量
     */
    private static final int ROLE_USER = 2;

    /**
     * 从请求头获取当前登录用户角色
     */
    private Integer getCurrentUserRole(HttpServletRequest request) {
        String roleStr = request.getHeader("X-User-Role");
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                return Integer.parseInt(roleStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 检查是否有权限访问
     * 普通用户(role=2)无权访问设备管理
     * 超级管理员(role=0)和管理员(role=1)有权限访问
     */
    private R<?> checkPermission(HttpServletRequest request) {
        Integer currentRole = getCurrentUserRole(request);
        // 如果角色为null，说明未正确获取用户信息，拒绝访问
        if (currentRole == null) {
            return R.error(401, "请先登录");
        }
        // 普通用户(role=2)无权访问
        if (currentRole == ROLE_USER) {
            return R.error(403, "您暂无权限访问此页面");
        }
        return null;
    }

    /**
     * 新增停车区域
     *
     * POST /api/area/add
     */
    @PostMapping("/add")
    public R<?> addArea(@RequestBody ParkArea parkArea, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkAreaService.saveArea(parkArea, currentRole);
        if (success) {
            return R.success("区域添加成功");
        }
        return R.error("区域添加失败");
    }

    /**
     * 删除停车区域
     *
     * DELETE /api/area/{id}
     */
    @DeleteMapping("/{id}")
    public R<?> deleteArea(@PathVariable Long id, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkAreaService.deleteAreaById(id, currentRole);
        if (success) {
            return R.success("区域删除成功");
        }
        return R.error("区域删除失败");
    }

    /**
     * 编辑停车区域
     *
     * PUT /api/area/edit
     */
    @PutMapping("/edit")
    public R<?> editArea(@RequestBody ParkArea parkArea, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkAreaService.updateArea(parkArea, currentRole);
        if (success) {
            return R.success("区域信息更新成功");
        }
        return R.error("区域信息更新失败");
    }

    /**
     * 根据ID查询停车区域
     *
     * GET /api/area/{id}
     */
    @GetMapping("/{id}")
    public R<?> getAreaById(@PathVariable Long id, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        ParkArea area = parkAreaService.getAreaById(id, currentRole);
        if (area != null) {
            return R.success(area);
        }
        return R.error(404, "区域不存在或您没有权限查看此区域");
    }

    /**
     * 分页查询停车区域列表
     *
     * GET /api/area/list
     * 
     * 注意：普通用户(role=2)可以查看停车区域列表（用于模拟停车登记），但不能进行增删改操作
     */
    @GetMapping("/list")
    public R<?> getAreaList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        Integer currentRole = getCurrentUserRole(request);
        if (currentRole == null) {
            return R.error(401, "请先登录");
        }

        IPage<ParkArea> page = parkAreaService.getAreaListPage(pageNum, pageSize, areaCode, areaName, status, currentRole);

        return R.success(page);
    }
}