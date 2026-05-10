package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("group_buy_campaign")
public class GroupBuyCampaign implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("product_id")
    private Long productId;

    @TableField("pick_point_id")
    private Long pickPointId;

    @TableField("group_target")
    private Integer groupTarget;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
