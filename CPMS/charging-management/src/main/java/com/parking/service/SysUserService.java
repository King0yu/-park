package com.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.entity.SysUser;

/**
 * 小区停车场管理系统用户服务接口
 *
 * 定义用户管理相关的业务方法
 * 
 * 角色说明：
 * - 0: 超级管理员(总管理) - 具有系统最高权限
 * - 1: 管理员(物业) - 负责停车场日常管理工作
 * - 2: 普通用户(车主) - 可使用停车场服务
 */
public interface SysUserService {

    /**
     * 新增用户
     *
     * @param sysUser 用户实体
     * @return 是否添加成功
     */
    boolean saveUser(SysUser sysUser);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否删除成功
     */
    boolean deleteUserById(Long id, Long currentUserId, Integer currentUserRole);

    /**
     * 更新用户信息
     *
     * @param sysUser 用户实体（包含ID）
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 是否更新成功
     */
    boolean updateUser(SysUser sysUser, Long currentUserId, Integer currentUserRole);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 用户实体
     */
    SysUser getUserById(Long id, Long currentUserId, Integer currentUserRole);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    SysUser getUserByPhone(String phone);

    /**
     * 分页查询用户列表（带权限控制）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param username 用户名（可选，用于模糊查询）
     * @param name 用户姓名（可选，用于模糊查询）
     * @param role 角色（可选）
     * @param status 状态（可选）
     * @param currentUserId 当前登录用户ID
     * @param currentUserRole 当前登录用户角色
     * @return 分页用户列表
     */
    IPage<SysUser> getUserListPage(int pageNum, int pageSize, String username, String name, Integer role, Integer status, Long currentUserId, Integer currentUserRole);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
