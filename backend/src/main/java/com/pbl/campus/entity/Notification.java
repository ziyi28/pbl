package com.pbl.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String type;

    @TableField("is_read")
    private Integer isRead;

    private Long relatedEventId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
