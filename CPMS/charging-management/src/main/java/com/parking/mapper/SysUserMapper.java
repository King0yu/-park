package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小区停车场管理系统用户Mapper接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper<T> 接口，提供基础的 CRUD 操作：
 * - insert(T entity) - 插入记录
 * - deleteById(Serializable id) - 根据ID删除
 * - updateById(T entity) - 根据ID更新
 * - selectById(Serializable id) - 根据ID查询
 * - selectList(Wrapper<T> queryWrapper) - 条件查询列表
 * - selectCount(Wrapper<T> queryWrapper) - 条件查询数量
 * 
 * 如需自定义查询方法，可在此接口中添加方法定义，并在对应的 XML 文件中编写 SQL
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体
     */
    SysUser selectByUsername(String username);

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户实体
     */
    SysUser selectByPhone(String phone);

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户实体
     */
    SysUser selectByEmail(String email);
}
