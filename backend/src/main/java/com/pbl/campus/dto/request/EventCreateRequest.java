package com.pbl.campus.dto.request;

import com.pbl.campus.common.enums.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotNull(message = "分类不能为空")
    private EventCategory category;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须晚于当前时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "最大人数不能为空")
    @Min(value = 1, message = "最大人数至少为 1")
    private Integer maxParticipants;

    private String coverImage;
}
