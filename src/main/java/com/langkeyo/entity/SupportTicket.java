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
@TableName("support_ticket")
public class SupportTicket implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("contact")
    private String contact;

    @TableField("status")
    private Integer status;

    @TableField("reply_content")
    private String replyContent;

    @TableField("reply_by")
    private String replyBy;

    @TableField("reply_time")
    private LocalDateTime replyTime;

    @TableField("assigned_to")
    private String assignedTo;

    @TableField("closed_by")
    private String closedBy;

    @TableField("closed_time")
    private LocalDateTime closedTime;

    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    @TableField("unread_admin_count")
    private Integer unreadAdminCount;

    @TableField("unread_user_count")
    private Integer unreadUserCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
