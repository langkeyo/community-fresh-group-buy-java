ALTER TABLE `users`
    ADD COLUMN `login_name` VARCHAR(64) NULL COMMENT '后台登录账号' AFTER `mobile`,
    ADD COLUMN `password_hash` VARCHAR(128) NULL COMMENT '后台登录密码哈希' AFTER `login_name`;

UPDATE `users`
SET `login_name` = CASE
    WHEN `mobile` IS NOT NULL AND `mobile` <> '' THEN `mobile`
    ELSE CONCAT('user_', `id`)
END
WHERE `login_name` IS NULL OR `login_name` = '';

-- 默认密码: Admin@123456 (SHA-256 with project salt)
UPDATE `users`
SET `password_hash` = '8ccd9cb94445e31273eebfae256f63cc08dcd52b4e0ec4a37452b2f97aea0450'
WHERE `password_hash` IS NULL OR `password_hash` = '';
