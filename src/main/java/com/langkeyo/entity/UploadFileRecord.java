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
@TableName("upload_file_record")
public class UploadFileRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("biz_type")
    private String bizType;

    @TableField("origin_name")
    private String originName;

    @TableField("stored_name")
    private String storedName;

    @TableField("content_type")
    private String contentType;

    @TableField("size_bytes")
    private Long sizeBytes;

    @TableField("url")
    private String url;

    @TableField("uploaded_by")
    private String uploadedBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
