package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * @author langkeyo
 */
@Data
@TableName("users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 微信唯一标识
     */
    @TableField("openid")
    private String openid;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    @TableField("login_name")
    private String loginName;

    @TableField("password_hash")
    private String passwordHash;

    /**
     * 是否为团长 (0:普通用户 1:团长)
     */
    @TableField("is_leader")
    private Boolean isLeader;

    @TableField("admin_role")
    private String adminRole;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 (0:未删除 1:已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
