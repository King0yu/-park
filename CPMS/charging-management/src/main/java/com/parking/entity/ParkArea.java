package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 停车区域实体类
 *
 * 对应数据库表：park_area
 *
 * 表结构说明：
 * - 存储停车区域的基本信息
 * - area_code: 区域编码，唯一标识
 * - status: 区域状态（0-停用，1-运营中，2-维护中）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("park_area")
public class ParkArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域编码（唯一标识，如：AREA001）
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 区域详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区县
     */
    @TableField("district")
    private String district;

    /**
     * 区域停车位总数
     */
    @TableField("total_spaces")
    private Integer totalSpaces;

    /**
     * 状态：0-停用，1-运营中，2-维护中
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}