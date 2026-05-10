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
@TableName("support_message")
public class SupportMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("ticket_id")
    private Long ticketId;

    @TableField("sender_type")
    private String senderType;

    @TableField("sender_id")
    private Long senderId;

    @TableField("sender_name")
    private String senderName;

    @TableField("content")
    private String content;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
