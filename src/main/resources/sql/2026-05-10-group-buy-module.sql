CREATE TABLE IF NOT EXISTS `group_buy_campaign` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(128) NOT NULL COMMENT '活动标题',
    `description` VARCHAR(1000) NULL COMMENT '活动描述',
    `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
    `pick_point_id` BIGINT NULL COMMENT '限定自提点ID',
    `group_target` INT NOT NULL DEFAULT 2 COMMENT '成团人数',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_group_buy_campaign_product` (`product_id`),
    KEY `idx_group_buy_campaign_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动主表';
