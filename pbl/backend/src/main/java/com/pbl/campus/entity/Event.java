package com.pbl.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pbl.campus.common.enums.EventCategory;
import com.pbl.campus.common.enums.EventStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event")
public class Event {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    @EnumValue
    private EventCategory category;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String coverImage;

    @EnumValue
    private EventStatus status;

    @TableLogic
    private Boolean isDeleted;

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
