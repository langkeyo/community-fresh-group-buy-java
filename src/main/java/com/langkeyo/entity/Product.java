package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("products")
public class Product {
    @TableId("id")
    private Long id;

    @TableField("name")
    private String name;
}
