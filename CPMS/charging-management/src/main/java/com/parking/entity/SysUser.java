package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 小区停车场系统用户实体类
 * 
 * 对应数据库表：sys_user
 * 使用 MyBatis-Plus 注解配置表映射
 * 使用 Lombok @Data 自动生成 getter/setter/toString 等方法
 * 
 * 角色说明：
 * - 0: 超级管理员(总管理) - 具有系统最高权限
 * - 1: 管理员(物业工作人员) - 负责停车场日常管理工作
 * - 2: 普通用户(小区车主) - 可使用停车场服务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键，自增）
     * 使用 @TableId 注解指定为主键，type = IdType.AUTO 表示自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号，唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（MD5/SHA256加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 用户姓名
     */
    @TableField("name")
    private String name;

    /**
     * 手机号码（唯一）
     */
@TableField(value = "phone", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    /**
     * 邮箱地址
     */
@TableField(value = "email", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String email;

    /**
     * 角色：0-超级管理员(总管理)，1-管理员(物业工作人员)，2-普通用户(小区车主)
     */
    @TableField("role")
    private Integer role;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
