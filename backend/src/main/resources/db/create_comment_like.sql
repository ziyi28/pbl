-- 评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '点赞 ID',
    `comment_id` BIGINT       NOT NULL COMMENT '评论 ID',
    `user_id`    BIGINT       NOT NULL COMMENT '点赞用户 ID',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
    KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';