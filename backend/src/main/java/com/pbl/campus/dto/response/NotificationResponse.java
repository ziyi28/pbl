package com.pbl.campus.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private Long relatedEventId;
    private LocalDateTime createdAt;
}
