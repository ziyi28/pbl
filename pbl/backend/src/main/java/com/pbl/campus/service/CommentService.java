package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.campus.common.PageResult;
import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.CommentCreateRequest;
import com.pbl.campus.dto.response.CommentResponse;
import com.pbl.campus.entity.Comment;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.CommentMapper;
import com.pbl.campus.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public Result<CommentResponse> createComment(Long userId, Long eventId, CommentCreateRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUserId(userId);
        comment.setEventId(eventId);
        commentMapper.insert(comment);

        return Result.success("评论成功", toResponse(comment));
    }

    public Result<Void> deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            return Result.error(403, "无权删除该评论");
        }

        commentMapper.deleteById(commentId);
        return Result.success("评论已删除", null);
    }

    public Result<PageResult<CommentResponse>> listComments(Long eventId, int page, int size) {
        Page<Comment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getEventId, eventId)
                        .orderByDesc(Comment::getCreatedAt));

        List<CommentResponse> records = pageResult.getRecords().stream()
                .map(this::toResponse)
                .toList();

        PageResult<CommentResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUserId());
        response.setEventId(comment.getEventId());
        response.setCreatedAt(comment.getCreatedAt());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setUserAvatar(user.getAvatar());
        }
        return response;
    }
}
