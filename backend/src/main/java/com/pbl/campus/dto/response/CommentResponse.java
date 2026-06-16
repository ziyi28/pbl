package com.pbl.campus.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {

    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long eventId;
    private Long parentId;
    private LocalDateTime createdAt;
    private Long likeCount;
    private Boolean liked;
    private List<CommentResponse> replies;
    private Long replyCount;
}
