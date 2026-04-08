package com.pbl.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long eventId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
