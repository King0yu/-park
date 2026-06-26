package com.parking.controller;

import com.parking.common.R;
import com.parking.entity.SysUser;
import com.parking.mapper.SysUserMapper;
import com.parking.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 个人中心控制器
 *
 * 提供当前登录用户相关的个人信息查询与修改接口
 * 接口路径：/api/user
 *
 * 权限说明：
 * - 所有登录用户均可操作自己的个人信息
 * - 仅允许修改自己的用户名和手机号，禁止修改角色
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 角色常量
     */
    private static final int ROLE_SUPER_ADMIN = 0;
    private static final int ROLE_ADMIN = 1;
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
     * 根据角色数字获取角色文字描述
     *
     * @param role 角色数字
     * @return 角色文字描述
     */
    private String getRoleText(Integer role) {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case ROLE_SUPER_ADMIN:
                return "超级管理员";
            case ROLE_ADMIN:
                return "管理员";
            case ROLE_USER:
                return "车主";
            default:
                return "未知";
        }
    }

    /**
     * MD5 加密
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前登录用户完整信息
     *
     * GET /api/user/current
     */
    @GetMapping("/current")
    public R<?> getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }

        Integer currentRole = getCurrentUserRole(request);
        SysUser user = sysUserService.getUserById(userId, userId, currentRole);
        if (user == null) {
            return R.error(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("roleText", getRoleText(user.getRole()));
        data.put("status", user.getStatus());
        data.put("createTime", user.getCreateTime());
        data.put("updateTime", user.getUpdateTime());

        return R.success(data);
    }

    /**
     * 更新当前登录用户自己的信息
     * 仅允许修改 username 和 phone，禁止修改 role
     *
     * PUT /api/user/updateInfo
     */
    @PutMapping("/updateInfo")
    public R<?> updateInfo(@RequestBody SysUser sysUser, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }

        Integer currentRole = getCurrentUserRole(request);
        SysUser existingUser = sysUserService.getUserById(userId, userId, currentRole);
        if (existingUser == null) {
            return R.error(404, "用户不存在");
        }

        // 校验用户名
        if (sysUser.getUsername() == null || sysUser.getUsername().trim().isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        String newUsername = sysUser.getUsername().trim();
        if (!Objects.equals(newUsername, existingUser.getUsername()) && sysUserService.existsByUsername(newUsername)) {
            return R.error(400, "用户名已存在");
        }

        // 校验手机号
        String newPhone = sysUser.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            if (!newPhone.matches("^1[3-9]\\d{9}$")) {
                return R.error(400, "请输入正确的手机号");
            }
            if (!Objects.equals(newPhone, existingUser.getPhone()) && sysUserService.existsByPhone(newPhone)) {
                return R.error(400, "手机号已存在");
            }
        } else {
            newPhone = existingUser.getPhone();
        }

        // 仅更新 username 和 phone，忽略 role 等其他字段
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setUsername(newUsername);
        updateUser.setPhone(newPhone);
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);

        return R.success("个人信息更新成功");
    }

    /**
     * 修改当前登录用户密码
     *
     * PUT /api/user/updatePwd
     */
    @PutMapping("/updatePwd")
    public R<?> updatePwd(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");

        // 参数校验
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return R.error(400, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return R.error(400, "新密码不能为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            return R.error(400, "新密码与确认密码不一致");
        }

        // 查询用户并校验旧密码
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return R.error(404, "用户不存在");
        }
        String encryptedOldPassword = md5(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            return R.error(400, "旧密码不正确");
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(md5(newPassword));
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);

        return R.success("密码修改成功");
    }
}
