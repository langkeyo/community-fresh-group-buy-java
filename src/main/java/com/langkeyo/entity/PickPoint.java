package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pick_points")
public class PickPoint {
    @TableId("id")
    private Long id;

    @TableField("name")
    private String name;

    @TableField("address")
    private String address;
}
