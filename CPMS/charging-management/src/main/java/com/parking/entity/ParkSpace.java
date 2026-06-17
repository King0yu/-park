package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车位实体类
 *
 * 对应数据库表：park_space
 *
 * 表结构说明：
 * - 存储停车位的详细信息
 * - area_id: 关联的停车区域ID
 * - status: 停车位状态（0-空闲，1-已占用，2-故障，3-维护）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("park_space")
public class ParkSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 停车位ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属区域ID（关联park_area表）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 停车位编码（唯一标识，如：P001）
     */
    @TableField("space_code")
    private String spaceCode;

    /**
     * 停车位名称/位置描述
     */
    @TableField("space_name")
    private String spaceName;

    /**
     * 停车位类型（地面/地下/机械/无障碍）
     */
    @TableField("space_type")
    private String spaceType;

    /**
     * 停车位面积（单位：平方米）
     */
    @TableField("area_size")
    private BigDecimal areaSize;

    /**
     * 绑定车牌号（业主车位专用）
     */
    @TableField("car_number")
    private String carNumber;

    /**
     * 状态：0-空闲，1-已占用，2-故障，3-维护
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
