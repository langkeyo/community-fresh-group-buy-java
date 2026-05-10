CREATE TABLE IF NOT EXISTS `coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(64) NOT NULL COMMENT '优惠券编码',
  `title` VARCHAR(128) NOT NULL COMMENT '优惠券名称',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '减免金额',
  `minimum_spend` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低消费门槛',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `start_time` DATETIME NULL COMMENT '生效时间',
  `end_time` DATETIME NULL COMMENT '失效时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

INSERT INTO `coupons` (`code`, `title`, `discount_amount`, `minimum_spend`, `status`)
SELECT 'fresh-5', '生鲜满30减5', 5.00, 30.00, 1
WHERE NOT EXISTS (SELECT 1 FROM `coupons` WHERE `code` = 'fresh-5');

INSERT INTO `coupons` (`code`, `title`, `discount_amount`, `minimum_spend`, `status`)
SELECT 'group-10', '拼团满59减10', 10.00, 59.00, 1
WHERE NOT EXISTS (SELECT 1 FROM `coupons` WHERE `code` = 'group-10');
