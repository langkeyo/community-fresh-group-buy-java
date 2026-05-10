CREATE TABLE IF NOT EXISTS `support_action_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `action_type` VARCHAR(32) NOT NULL COMMENT 'CREATE/ASSIGN/REPLY/TRANSFER/CLOSE',
    `operator` VARCHAR(64) NOT NULL COMMENT '操作人',
    `detail` VARCHAR(1000) NOT NULL COMMENT '操作详情',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_support_action_ticket` (`ticket_id`),
    KEY `idx_support_action_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服操作审计日志';
