package com.pbl.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long userId;

    private Long eventId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
