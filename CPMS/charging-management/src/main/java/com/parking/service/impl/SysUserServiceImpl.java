package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.common.BusinessException;
import com.parking.entity.SysUser;
import com.parking.mapper.SysUserMapper;
import com.parking.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 小区停车场管理系统用户服务实现类
 *
 * 实现用户管理相关的业务逻辑，包含权限控制
 * 
 * 角色说明：
 * - 0: 超级管理员(总管理) - 具有系统最高权限
 * - 1: 管理员(物业) - 负责停车场日常管理工作
 * - 2: 普通用户(车主) - 可使用停车场服务
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 角色常量
     * - ROLE_SUPER_ADMIN(0): 超级管理员(总管理) - 具有系统最高权限
     * - ROLE_ADMIN(1): 管理员(物业) - 负责停车场日常管理工作
     * - ROLE_USER(2): 普通用户(车主) - 可使用停车场服务
     */
    private static final int ROLE_SUPER_ADMIN = 0;
    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_USER = 2;

    /**
     * MD5加密工具方法
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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean saveUser(SysUser sysUser) {
        // 验证用户名是否已存在
        if (existsByUsername(sysUser.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 空字符串转为 null，避免 MySQL UNIQUE 约束冲突
        if (sysUser.getPhone() != null && sysUser.getPhone().trim().isEmpty()) {
            sysUser.setPhone(null);
        }
        if (sysUser.getEmail() != null && sysUser.getEmail().trim().isEmpty()) {
            sysUser.setEmail(null);
        }

        // 验证手机号是否已存在
        if (StringUtils.hasText(sysUser.getPhone()) && existsByPhone(sysUser.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 验证邮箱是否已存在
        if (StringUtils.hasText(sysUser.getEmail()) && existsByEmail(sysUser.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 密码加密（MD5）
        if (StringUtils.hasText(sysUser.getPassword())) {
            sysUser.setPassword(md5(sysUser.getPassword()));
        }

        // 设置默认角色为普通用户
        if (sysUser.getRole() == null) {
            sysUser.setRole(ROLE_USER);
        }

        // 设置默认状态为启用
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(1);
        }

        // 执行插入
        return sysUserMapper.insert(sysUser) > 0;
    }

    @Override
    public boolean deleteUserById(Long id, Long currentUserId, Integer currentUserRole) {
        // 检查目标用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 权限控制：普通用户(车主)不能删除任何用户
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            throw new BusinessException(403, "您没有权限执行此操作");
        }

        // 权限控制：管理员(物业)只能删除普通用户(车主)
        if (currentUserRole != null && currentUserRole == ROLE_ADMIN) {
            if (user.getRole() == ROLE_SUPER_ADMIN) {
                throw new BusinessException(403, "您没有权限删除超级管理员(总管理)");
            }
            if (user.getRole() == ROLE_ADMIN) {
                throw new BusinessException(403, "您没有权限删除其他管理员(物业)");
            }
        }

        // 不能删除自己
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException("不能删除自己");
        }

        // 执行删除
        return sysUserMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateUser(SysUser sysUser, Long currentUserId, Integer currentUserRole) {
        // 检查用户是否存在
        SysUser existingUser = sysUserMapper.selectById(sysUser.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 权限控制：普通用户(车主)不能修改任何用户
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            throw new BusinessException(403, "您没有权限执行此操作");
        }

        // 权限控制：管理员(物业)只能修改普通用户(车主)
        if (currentUserRole != null && currentUserRole == ROLE_ADMIN) {
            if (existingUser.getRole() == ROLE_SUPER_ADMIN) {
                throw new BusinessException(403, "您没有权限修改超级管理员(总管理)信息");
            }
            if (existingUser.getRole() == ROLE_ADMIN && !existingUser.getId().equals(currentUserId)) {
                throw new BusinessException(403, "您没有权限修改其他管理员(物业)信息");
            }
            // 管理员(物业)不能创建超级管理员(总管理)或管理员(物业)
            if (sysUser.getRole() != null && sysUser.getRole() != ROLE_USER) {
                throw new BusinessException(403, "您没有权限创建管理员(物业)或超级管理员(总管理)");
            }
        }

        // 如果修改了用户名，检查新用户名是否已存在
        if (StringUtils.hasText(sysUser.getUsername())
                && !Objects.equals(sysUser.getUsername(), existingUser.getUsername())
                && existsByUsername(sysUser.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 如果修改了手机号，检查新手机号是否已存在
        if (StringUtils.hasText(sysUser.getPhone())
                && !Objects.equals(sysUser.getPhone(), existingUser.getPhone())
                && existsByPhone(sysUser.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 如果修改了邮箱，检查新邮箱是否已存在
        if (StringUtils.hasText(sysUser.getEmail())
                && !Objects.equals(sysUser.getEmail(), existingUser.getEmail())
                && existsByEmail(sysUser.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 如果修改了密码，进行加密
        if (StringUtils.hasText(sysUser.getPassword())) {
            sysUser.setPassword(md5(sysUser.getPassword()));
        } else {
            // 如果密码为空，保持原密码不变
            sysUser.setPassword(existingUser.getPassword());
        }

        // 执行更新
        return sysUserMapper.updateById(sysUser) > 0;
    }

    @Override
    public SysUser getUserById(Long id, Long currentUserId, Integer currentUserRole) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return null;
        }

        // 权限控制：普通用户(车主)只能看自己
        if (currentUserRole != null && currentUserRole == ROLE_USER) {
            if (!currentUserId.equals(id)) {
                return null;
            }
        }

        // 权限控制：管理员(物业)只能看普通用户(车主)和自己
        if (currentUserRole != null && currentUserRole == ROLE_ADMIN) {
            if (user.getRole() == ROLE_SUPER_ADMIN) {
                return null;
            }
        }

        return user;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser getUserByPhone(String phone) {
        return sysUserMapper.selectByPhone(phone);
    }

    @Override
    public IPage<SysUser> getUserListPage(int pageNum, int pageSize, String username, String name, Integer role, Integer status, Long currentUserId, Integer currentUserRole) {
        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysUser::getUsername, username);
        }

        // 用户姓名模糊查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(SysUser::getName, name);
        }

        // 角色筛选
        if (role != null) {
            queryWrapper.eq(SysUser::getRole, role);
        }

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        // 【权限控制】根据当前用户角色过滤数据
        if (currentUserRole != null) {
            if (currentUserRole == ROLE_USER) {
                // 普通用户(车主)：只能看到自己
                queryWrapper.eq(SysUser::getId, currentUserId);
            } else if (currentUserRole == ROLE_ADMIN) {
                // 管理员(物业)：只能看到普通用户(车主，role=2)
                queryWrapper.eq(SysUser::getRole, ROLE_USER);
            }
            // 超级管理员(总管理)：可以看到所有用户，不做额外过滤
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        // 执行分页查询
        return sysUserMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.exists(queryWrapper);
    }

    @Override
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, phone);
        return sysUserMapper.exists(queryWrapper);
    }

    @Override
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, email);
        return sysUserMapper.exists(queryWrapper);
    }
}
