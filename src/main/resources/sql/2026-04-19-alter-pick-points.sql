-- pick_points 补充团长与审计字段（MySQL 8+）
-- 执行前请先备份数据库

ALTER TABLE `pick_points`
  ADD COLUMN `leader_name` VARCHAR(50) NULL COMMENT '团长姓名' AFTER `address`,
  ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '联系电话' AFTER `leader_name`,
  ADD COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `phone`,
  ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`,
  ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删,1已删)' AFTER `update_time`;

-- 可选：按需补一个常用查询索引
CREATE INDEX `idx_pick_points_deleted` ON `pick_points` (`deleted`);
