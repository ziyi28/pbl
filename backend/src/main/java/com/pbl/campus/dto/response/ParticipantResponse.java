package com.pbl.campus.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipantResponse {

    private Long userId;
    private String username;
    private String avatar;
    private String email;
    private LocalDateTime registeredAt;
}
