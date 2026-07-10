package com.parking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.common.R;
import com.parking.entity.ParkSpace;
import com.parking.service.ParkSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 停车位控制器
 *
 * 提供 RESTful 风格的停车位管理接口
 * 接口路径：/api/space
 *
 * 权限说明：
 * - super_admin (role=0): 完全控制
 * - admin (role=1): 完全控制
 * - user (role=2): 无权限访问管理，可查看列表
 */
@RestController
@RequestMapping("/api/space")
@CrossOrigin(origins = "*")
public class ParkSpaceController {

    @Autowired
    private ParkSpaceService parkSpaceService;

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
     */
    private R<?> checkPermission(HttpServletRequest request) {
        Integer currentRole = getCurrentUserRole(request);
        if (currentRole != null && currentRole == ROLE_USER) {
            return R.error(403, "您暂无权限访问此页面");
        }
        return null;
    }

    /**
     * 新增停车位
     *
     * POST /api/space/add
     */
    @PostMapping("/add")
    public R<?> addSpace(@RequestBody ParkSpace parkSpace, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkSpaceService.saveSpace(parkSpace, currentRole);
        if (success) {
            return R.success("停车位添加成功");
        }
        return R.error("停车位添加失败");
    }

    /**
     * 删除停车位
     *
     * DELETE /api/space/{id}
     */
    @DeleteMapping("/{id}")
    public R<?> deleteSpace(@PathVariable Long id, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkSpaceService.deleteSpaceById(id, currentRole);
        if (success) {
            return R.success("停车位删除成功");
        }
        return R.error("停车位删除失败");
    }

    /**
     * 编辑停车位
     *
     * PUT /api/space/edit
     */
    @PutMapping("/edit")
    public R<?> editSpace(@RequestBody ParkSpace parkSpace, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        boolean success = parkSpaceService.updateSpace(parkSpace, currentRole);
        if (success) {
            return R.success("停车位信息更新成功");
        }
        return R.error("停车位信息更新失败");
    }

    /**
     * 根据ID查询停车位
     *
     * GET /api/space/{id}
     */
    @GetMapping("/{id}")
    public R<?> getSpaceById(@PathVariable Long id, HttpServletRequest request) {
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Integer currentRole = getCurrentUserRole(request);
        ParkSpace space = parkSpaceService.getSpaceById(id, currentRole);
        if (space != null) {
            return R.success(space);
        }
        return R.error(404, "停车位不存在或您没有权限查看此停车位");
    }

    /**
     * 根据区域ID查询所有停车位（关联查询）
     *
     * GET /api/space/area/{areaId}
     * 
     * 权限说明：普通用户也可访问（只读，用于模拟停车登记）
     */
    @GetMapping("/area/{areaId}")
    public R<?> getSpacesByAreaId(@PathVariable Long areaId, HttpServletRequest request) {
        Integer currentRole = getCurrentUserRole(request);
        if (currentRole == null) {
            return R.error(401, "请先登录");
        }
        
        List<ParkSpace> spaces = parkSpaceService.getSpacesByAreaId(areaId, currentRole);

        return R.success(spaces);
    }

    /**
     * 分页查询停车位列表
     *
     * GET /api/space/list
     * 
     * 权限说明：普通用户也可访问（只读，用于查看可用车位）
     */
    @GetMapping("/list")
    public R<?> getSpaceList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String spaceCode,
            @RequestParam(required = false) String spaceName,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        Integer currentRole = getCurrentUserRole(request);
        IPage<ParkSpace> page = parkSpaceService.getSpaceListPage(pageNum, pageSize, areaId, spaceCode, spaceName, status, currentRole);

        return R.success(page);
    }

    /**
     * 停车位状态统计（用于数据大屏）
     * GET /api/space/statistics
     */
    @GetMapping("/statistics")
    public R<?> getSpaceStatistics(HttpServletRequest request) {
        // 优先校验登录（管理端任何人都得先登录）
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        // 限管理端
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        java.util.Map<Integer, Integer> statusMap = parkSpaceService.countByStatus();
        int total = statusMap.values().stream().mapToInt(Integer::intValue).sum();
        int free = statusMap.getOrDefault(0, 0);
        int occupied = statusMap.getOrDefault(1, 0);
        int fault = statusMap.getOrDefault(2, 0) + statusMap.getOrDefault(3, 0);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("total", total);
        data.put("free", free);
        data.put("occupied", occupied);
        data.put("fault", fault);
        return R.success(data);
    }

    /**
     * 检查登录状态
     */
    private R<?> checkLogin(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }
        return null;
    }

    /**
     * 从请求头获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String idStr = request.getHeader("X-User-Id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}