package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "评论接口", description = "评论删除、点赞相关接口")
public class CommentController {

    private final CommentService commentService;

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "用户可删除自己的评论，管理员可删除任意评论")
    public Result<Void> deleteComment(Authentication authentication, @Parameter(description = "评论ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return commentService.deleteComment(id, userId, isAdmin);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞", description = "用户对评论进行点赞或取消点赞")
    public Result<Void> toggleLike(Authentication authentication, @Parameter(description = "评论ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return commentService.toggleLike(id, userId);
    }
}
