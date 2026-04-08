package com.pbl.campus.dto.response;

import com.pbl.campus.common.enums.EventCategory;
import com.pbl.campus.common.enums.EventStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String coverImage;
    private EventStatus status;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
}
