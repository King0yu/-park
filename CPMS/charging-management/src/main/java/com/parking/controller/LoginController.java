package com.parking.controller;

import com.parking.common.R;
import com.parking.entity.SysUser;
import com.parking.mapper.SysUserMapper;
import com.parking.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * 提供小区停车场管理系统用户登录相关接口
 * 
 * 角色说明：
 * - 0: 超级管理员(总管理) - 具有系统最高权限
 * - 1: 管理员(物业) - 负责停车场日常管理工作
 * - 2: 普通用户(车主) - 可使用停车场服务
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 用户登录
     *
     * POST /api/user/login
     * Content-Type: application/json
     * Body: {"username": "xxx", "password": "xxx"}
     *
     * 或者 GET /api/user/login?username=xxx&password=xxx
     */
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public R<?> login(
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password) {

        // 兼容GET和POST两种请求方式
        if (body != null && body.containsKey("username")) {
            username = body.get("username");
            password = body.get("password");
        }

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return R.error(400, "密码不能为空");
        }

        // 查询用户
        SysUser user = sysUserService.getUserByUsername(username);

        // 用户不存在
        if (user == null) {
            return R.error(401, "用户名或密码错误");
        }

        // 密码校验（这里简单对比，实际项目应加密后对比）
        // 注意：后端存储的是 MD5 加密后的密码
        String encryptedPassword = md5(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            return R.error(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            return R.error(401, "账号已被禁用，请联系管理员(物业)");
        }

        // 生成 Token（这里使用简单的用户ID+时间戳作为Token，实际项目应使用JWT）
        String token = generateToken(user);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        // 用户信息（不包含密码，但包含ID和角色）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        data.put("userInfo", userInfo);

        return R.success("登录成功", data);
    }

    /**
     * MD5 加密
     */
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
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
     * 用户注册
     *
     * POST /api/user/register
     * Content-Type: application/json
     * Body: {"username": "xxx", "password": "xxx", "phone": "xxx"}
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public R<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return R.error(400, "密码不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return R.error(400, "手机号不能为空");
        }

        // 手机号格式校验
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return R.error(400, "请输入正确的手机号");
        }

        // 创建用户对象
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setName(username); // 默认用户名为显示名称
        // 其他字段使用默认值

        try {
            boolean success = sysUserService.saveUser(user);
            if (success) {
                return R.success("注册成功");
            }
            return R.error("注册失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 忘记密码 - 身份验证后重置密码
     *
     * POST /api/user/forgotPassword
     * Content-Type: application/json
     *
     * 模式1 - 身份验证（仅校验用户名+手机号是否匹配）：
     * Body: {"username": "xxx", "phone": "13800138000", "mode": "verify"}
     *
     * 模式2 - 重置密码（验证通过后设置新密码）：
     * Body: {"username": "xxx", "phone": "13800138000", "newPassword": "xxx"}
     *
     * 流程：
     * 1. 验证用户名是否存在
     * 2. 验证手机号是否与注册时一致
     * 3. 检查账号状态
     * 4. 若 mode != "verify"，则重置密码
     */
    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public R<?> forgotPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String phone = params.get("phone");
        String newPassword = params.get("newPassword");
        String mode = params.get("mode");

        boolean verifyOnly = "verify".equals(mode);

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return R.error(400, "手机号不能为空");
        }
        if (!verifyOnly && (newPassword == null || newPassword.trim().isEmpty())) {
            return R.error(400, "新密码不能为空");
        }
        if (!verifyOnly && (newPassword.length() < 6 || newPassword.length() > 20)) {
            return R.error(400, "密码长度需为6-20个字符");
        }

        // 查询用户
        SysUser user = sysUserService.getUserByUsername(username.trim());
        if (user == null) {
            return R.error(404, "该用户名不存在");
        }

        // 验证手机号是否匹配
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return R.error(400, "该账户未绑定手机号，无法通过手机号重置密码，请联系管理员(物业)");
        }
        if (!user.getPhone().equals(phone.trim())) {
            return R.error(400, "手机号与注册时不一致，身份验证失败");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            return R.error(401, "账号已被禁用，请联系管理员(物业)");
        }

        // 仅验证模式：不更新密码
        if (verifyOnly) {
            return R.success("身份验证通过");
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(md5(newPassword));
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);

        return R.success("密码重置成功，请返回登录");
    }

    /**
     * 生成 Token（简化版）
     * 实际项目建议使用 JWT
     */
    private String generateToken(SysUser user) {
        return user.getId() + "-" + System.currentTimeMillis();
    }
}
