package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notice_read")
public class NoticeRead implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("notice_id")
    private Long noticeId;

    @TableField("user_id")
    private Long userId;

    @TableField("read_time")
    private LocalDateTime readTime;
}

