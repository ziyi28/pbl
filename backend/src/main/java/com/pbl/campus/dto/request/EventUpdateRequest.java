package com.pbl.campus.dto.request;

import com.pbl.campus.common.enums.EventCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventUpdateRequest {

    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @Size(max = 2000, message = "活动描述长度不能超过2000")
    private String description;

    private EventCategory category;

    @Size(max = 200, message = "地点长度不能超过200")
    private String location;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;

    @Min(value = 1, message = "最大人数必须大于0")
    private Integer maxParticipants;

    @Size(max = 500, message = "封面地址长度不能超过500")
    private String coverImage;
}
