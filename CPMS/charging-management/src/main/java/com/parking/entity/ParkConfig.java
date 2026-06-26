package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置实体类
 *
 * 对应数据库表：park_config
 *
 * 表结构说明：
 * - 存储系统级别的配置信息
 * - system_name: 系统名称
 * - copyright: 版权标语
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("park_config")
public class ParkConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统名称
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 版权标语
     */
    @TableField("copyright")
    private String copyright;

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
