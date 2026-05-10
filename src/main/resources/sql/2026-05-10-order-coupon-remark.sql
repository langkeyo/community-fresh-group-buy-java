ALTER TABLE `orders`
    ADD COLUMN `coupon_id` VARCHAR(64) NULL COMMENT '优惠券ID' AFTER `group_buy_id`,
    ADD COLUMN `coupon_title` VARCHAR(128) NULL COMMENT '优惠券标题' AFTER `coupon_id`,
    ADD COLUMN `coupon_amount` DECIMAL(10,2) NULL COMMENT '优惠减免金额' AFTER `coupon_title`,
    ADD COLUMN `remark` VARCHAR(255) NULL COMMENT '用户备注' AFTER `coupon_amount`;
