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
 * 停车记录实体类
 *
 * 对应数据库表：park_record
 *
 * 表结构说明：
 * - 存储停车记录的详细信息
 * - user_id: 关联用户ID（sys_user表）
 * - area_id: 关联区域ID（park_area表）
 * - space_id: 关联停车位ID（park_space表）
 * - status: 记录状态（0-待停车，1-停车中，2-已完成，3-已取消，4-异常）
 * - pay_status: 支付状态（0-未支付，1-已支付）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("park_record")
public class ParkRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 记录编号（唯一标识，如：PRK202401010001）
     */
    @TableField("record_no")
    private String recordNo;

    /**
     * 用户ID（关联sys_user表）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 区域ID（关联park_area表）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 停车位ID（关联park_space表）
     */
    @TableField("space_id")
    private Long spaceId;

    /**
     * 车牌号
     */
    @TableField("car_number")
    private String carNumber;

    /**
     * 停车开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 停车结束时间（NULL表示正在停车）
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 停车时长（单位：小时）
     */
    @TableField("parking_duration")
    private BigDecimal parkingDuration;

    /**
     * 单价（元/小时）
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 停车总费用（单位：元）
     */
    @TableField("total_cost")
    private BigDecimal totalCost;

    /**
     * 记录状态：0-待停车，1-停车中，2-已完成，3-已取消，4-异常
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付状态：0-未支付，1-已支付
     */
    @TableField("pay_status")
    private Integer payStatus;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间（记录创建时间）
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 关联查询字段（非数据库字段） ====================

    /**
     * 关联用户名（用于查询展示）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 关联区域名称（用于查询展示）
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 关联停车位编码（用于查询展示）
     */
    @TableField(exist = false)
    private String spaceCode;

    /**
     * 关联停车位名称（用于查询展示）
     */
    @TableField(exist = false)
    private String spaceName;
}