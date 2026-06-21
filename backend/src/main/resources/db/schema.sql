-- 校园活动发布平台 建表 SQL
-- 数据库: campus_activity

CREATE DATABASE IF NOT EXISTS `campus_activity` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `campus_activity`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱',
    `avatar`     VARCHAR(255) NULL     COMMENT '头像 URL',
    `bio`        VARCHAR(500) NULL     COMMENT '个人简介',
    `role`       VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 活动表
CREATE TABLE IF NOT EXISTS `event` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '活动 ID',
    `title`                 VARCHAR(200)  NOT NULL COMMENT '标题',
    `description`           TEXT          NOT NULL COMMENT '描述',
    `category`              VARCHAR(20)   NOT NULL COMMENT '分类: LECTURE/SPORTS/CLUB/VOLUNTEER/OTHER',
    `location`              VARCHAR(200)  NOT NULL COMMENT '地点',
    `start_time`            DATETIME      NOT NULL COMMENT '开始时间',
    `end_time`              DATETIME      NOT NULL COMMENT '结束时间',
    `registration_deadline` DATETIME      NOT NULL COMMENT '报名截止时间',
    `max_participants`      INT           NOT NULL COMMENT '最大人数',
    `current_participants`  INT           NOT NULL DEFAULT 0 COMMENT '当前报名人数',
    `cover_image`           VARCHAR(255)  NULL     COMMENT '封面图 URL',
    `status`                VARCHAR(10)   NOT NULL DEFAULT 'OPEN' COMMENT '状态: OPEN/ONGOING/ENDED',
    `is_deleted`            TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除: 0-否 1-是',
    `creator_id`            BIGINT        NOT NULL COMMENT '创建者 ID',
    `version`               INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 报名表
CREATE TABLE IF NOT EXISTS `registration` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户 ID',
    `event_id`   BIGINT   NOT NULL COMMENT '活动 ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_event` (`user_id`, `event_id`),
    KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
    `content`    VARCHAR(500) NOT NULL COMMENT '评论内容',
    `user_id`    BIGINT       NOT NULL COMMENT '评论者 ID',
    `event_id`   BIGINT       NOT NULL COMMENT '活动 ID',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏 ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户 ID',
    `event_id`   BIGINT   NOT NULL COMMENT '活动 ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_event` (`user_id`, `event_id`),
    KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

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

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知 ID',
    `user_id`          BIGINT       NOT NULL COMMENT '用户 ID',
    `title`            VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`          VARCHAR(500) NOT NULL COMMENT '通知内容',
    `type`             VARCHAR(30)  NOT NULL COMMENT '通知类型: REGISTRATION_SUCCESS/REGISTRATION_CANCELLED/EVENT_CANCELLED/EVENT_UPDATED',
    `is_read`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `related_event_id` BIGINT       NULL     COMMENT '关联活动 ID',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入默认管理员（密码: admin123，BCrypt 加密）
INSERT INTO `user` (`username`, `password`, `email`, `role`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@campus.com', 'ADMIN');
