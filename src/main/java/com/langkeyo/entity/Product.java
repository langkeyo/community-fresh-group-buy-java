package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId("id")
    private Long id;

    @TableField("name")
    private String name;

    @TableField("category")
    private String category;

    @TableField("price")
    private BigDecimal price;

    @TableField("group_price_2")
    private BigDecimal groupPrice2;

    @TableField("group_price_3")
    private BigDecimal groupPrice3;

    @TableField("stock")
    private Integer stock;

    @TableField("images")
    private String images;

    @TableField("status")
    private Integer status;

    @TableField("group_open")
    private Integer groupOpen;

    @TableField("group_start_time")
    private LocalDateTime groupStartTime;

    @TableField("group_end_time")
    private LocalDateTime groupEndTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;
}
