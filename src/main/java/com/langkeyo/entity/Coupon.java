package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {
    @TableId("id")
    private Long id;

    @TableField("code")
    private String code;

    @TableField("title")
    private String title;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("minimum_spend")
    private BigDecimal minimumSpend;

    @TableField("status")
    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
