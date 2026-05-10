ALTER TABLE `support_ticket`
    ADD COLUMN `assigned_to` VARCHAR(64) NULL COMMENT '接单客服' AFTER `reply_time`,
    ADD COLUMN `closed_by` VARCHAR(64) NULL COMMENT '关闭人' AFTER `assigned_to`,
    ADD COLUMN `closed_time` DATETIME NULL COMMENT '关闭时间' AFTER `closed_by`,
    ADD COLUMN `last_message_time` DATETIME NULL COMMENT '最后消息时间' AFTER `closed_time`,
    ADD COLUMN `unread_admin_count` INT NULL DEFAULT 0 COMMENT '管理员未读数' AFTER `last_message_time`,
    ADD COLUMN `unread_user_count` INT NULL DEFAULT 0 COMMENT '用户未读数' AFTER `unread_admin_count`;

CREATE TABLE IF NOT EXISTS `support_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `sender_type` VARCHAR(16) NOT NULL COMMENT 'USER/ADMIN',
    `sender_id` BIGINT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(64) NULL COMMENT '发送者名称',
    `content` VARCHAR(2000) NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_support_message_ticket_id` (`ticket_id`),
    KEY `idx_support_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话消息表';

UPDATE `support_ticket`
SET `assigned_to` = IFNULL(`assigned_to`, ''),
    `closed_by` = IFNULL(`closed_by`, ''),
    `unread_admin_count` = IFNULL(`unread_admin_count`, 0),
    `unread_user_count` = IFNULL(`unread_user_count`, 0),
    `last_message_time` = IFNULL(`last_message_time`, `create_time`)
WHERE 1 = 1;
