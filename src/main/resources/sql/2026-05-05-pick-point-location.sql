-- pick_points 增加经纬度字段（MySQL 8+）
-- 执行前请先备份数据库

ALTER TABLE `pick_points`
  ADD COLUMN `latitude` DOUBLE NULL COMMENT '纬度' AFTER `phone`,
  ADD COLUMN `longitude` DOUBLE NULL COMMENT '经度' AFTER `latitude`;

