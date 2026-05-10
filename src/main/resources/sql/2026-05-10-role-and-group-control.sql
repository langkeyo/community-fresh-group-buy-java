ALTER TABLE `users`
    ADD COLUMN `admin_role` VARCHAR(32) NULL COMMENT 'super_admin/ops_admin/leader' AFTER `is_leader`;

UPDATE `users`
SET `admin_role` = CASE
    WHEN `is_leader` = 1 THEN 'leader'
    ELSE 'super_admin'
END
WHERE `admin_role` IS NULL OR `admin_role` = '';

ALTER TABLE `products`
    ADD COLUMN `group_open` TINYINT NULL DEFAULT 0 COMMENT '0未开团 1开团' AFTER `status`,
    ADD COLUMN `group_start_time` DATETIME NULL COMMENT '开团开始时间' AFTER `group_open`,
    ADD COLUMN `group_end_time` DATETIME NULL COMMENT '开团结束时间' AFTER `group_start_time`;

UPDATE `products`
SET `group_open` = 1
WHERE `group_open` IS NULL;
