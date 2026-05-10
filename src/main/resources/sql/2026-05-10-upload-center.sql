CREATE TABLE IF NOT EXISTS `upload_file_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务分类',
    `origin_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `content_type` VARCHAR(128) NULL COMMENT 'MIME类型',
    `size_bytes` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `uploaded_by` VARCHAR(64) NULL COMMENT '上传人标识',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_upload_file_record_biz` (`biz_type`),
    KEY `idx_upload_file_record_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一上传文件记录表';
