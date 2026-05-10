ALTER TABLE `orders`
    ADD COLUMN `pay_method` VARCHAR(32) NULL COMMENT '支付方式: WECHAT/BALANCE' AFTER `remark`,
    ADD COLUMN `refund_method` VARCHAR(32) NULL COMMENT '退款方式: ORIGINAL/BALANCE',
    ADD COLUMN `refund_reason` VARCHAR(128) NULL COMMENT '退款原因',
    ADD COLUMN `refund_note` VARCHAR(255) NULL COMMENT '退款备注',
    ADD COLUMN `refund_time` DATETIME NULL COMMENT '退款时间';

UPDATE `orders`
SET `pay_method` = 'WECHAT'
WHERE `pay_method` IS NULL OR `pay_method` = '';
