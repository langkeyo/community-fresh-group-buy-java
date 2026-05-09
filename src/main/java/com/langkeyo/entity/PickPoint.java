package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pick_points")
public class PickPoint {
    @TableId("id")
    private Long id;

    @TableField("name")
    private String name;

    @TableField("address")
    private String address;

    @TableField("leader_name")
    private String leaderName;

    @TableField("phone")
    private String phone;

    @TableField("latitude")
    private Double latitude;

    @TableField("longitude")
    private Double longitude;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
