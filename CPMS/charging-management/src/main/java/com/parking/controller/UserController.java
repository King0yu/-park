package com.parking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.common.R;
import com.parking.entity.SysUser;
import com.parking.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户管理控制器
 *
 * 提供 RESTful 风格的用户管理接口
 * 接口路径：/api/user
 *
 * 权限说明：
 * - super_admin (role=0): 超级管理员(总管理) - 完全控制
 * - admin (role=1): 管理员(物业) - 只能管理普通用户(role=2)
 * - user (role=2): 普通用户(车主) - 无权限访问
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 角色常量
     */
    private static final int ROLE_USER = 2;

    /**
     * 从请求头获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

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
     * 普通用户(车主，role=2)无权访问用户管理
     */
    private R<?> checkPermission(HttpServletRequest request) {
        Integer currentRole = getCurrentUserRole(request);
        if (currentRole != null && currentRole == ROLE_USER) {
            return R.error(403, "您暂无权限访问此页面");
        }
        return null;
    }

    /**
     * 新增用户
     *
     * POST /api/user/add
     */
    @PostMapping("/add")
    public R<?> addUser(@RequestBody SysUser sysUser, HttpServletRequest request) {
        // 权限检查
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        boolean success = sysUserService.saveUser(sysUser);
        if (success) {
            return R.success("用户添加成功");
        }
        return R.error("用户添加失败");
    }

    /**
     * 删除用户
     *
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public R<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        // 权限检查
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);
        boolean success = sysUserService.deleteUserById(id, currentUserId, currentRole);
        if (success) {
            return R.success("用户删除成功");
        }
        return R.error("用户删除失败");
    }

    /**
     * 编辑用户
     *
     * PUT /api/user/edit
     */
    @PutMapping("/edit")
    public R<?> editUser(@RequestBody SysUser sysUser, HttpServletRequest request) {
        // 权限检查
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);
        boolean success = sysUserService.updateUser(sysUser, currentUserId, currentRole);
        if (success) {
            return R.success("用户信息更新成功");
        }
        return R.error("用户信息更新失败");
    }

    /**
     * 根据ID查询用户
     *
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public R<?> getUserById(@PathVariable Long id, HttpServletRequest request) {
        // 权限检查
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);
        SysUser user = sysUserService.getUserById(id, currentUserId, currentRole);
        if (user != null) {
            user.setPassword(null);
            return R.success(user);
        }
        return R.error(404, "用户不存在或您没有权限查看此用户");
    }

    /**
     * 分页查询用户列表
     *
     * GET /api/user/list
     */
    @GetMapping("/list")
    public R<?> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        // 权限检查
        R<?> permissionCheck = checkPermission(request);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        IPage<SysUser> page = sysUserService.getUserListPage(pageNum, pageSize, username, name, role, status, currentUserId, currentRole);

        // 隐藏密码字段
        page.getRecords().forEach(user -> user.setPassword(null));

        return R.success(page);
    }

    /**
     * 获取当前登录用户信息
     *
     * GET /api/user/info
     */
    @GetMapping("/info")
    public R<?> getUserInfo(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }

        Integer currentRole = getCurrentUserRole(request);
        SysUser user = sysUserService.getUserById(userId, userId, currentRole);
        if (user != null) {
            user.setPassword(null);
            return R.success(user);
        }
        return R.error(404, "用户不存在");
    }
}
