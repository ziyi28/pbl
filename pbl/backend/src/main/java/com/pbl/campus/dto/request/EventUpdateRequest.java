package com.pbl.campus.dto.request;

import com.pbl.campus.common.enums.EventCategory;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventUpdateRequest {

    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private Integer maxParticipants;
    private String coverImage;
}
