package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.campus.common.PageResult;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.EventStatus;
import com.pbl.campus.dto.request.CommentCreateRequest;
import com.pbl.campus.dto.response.CommentResponse;
import com.pbl.campus.entity.*;
import com.pbl.campus.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final EventMapper eventMapper;
    private final RegistrationMapper registrationMapper;

    public Result<CommentResponse> createComment(Long userId, Long eventId, CommentCreateRequest request) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        if (event.getStatus() == EventStatus.ENDED) {
            return Result.error("活动已结束，不可评论");
        }
        if (event.getStatus() == EventStatus.CANCELLED) {
            return Result.error("活动已取消，不可评论");
        }
        Long registeredCount = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getEventId, eventId));
        if (registeredCount == 0) {
            return Result.error(403, "需报名后才能评论");
        }

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
                        .orderByDesc(Comment::getId));

        List<CommentResponse> records = pageResult.getRecords().stream()
                .map(this::toResponse)
                .toList();

        PageResult<CommentResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    public Result<PageResult<CommentResponse>> listComments(Long eventId, int page, int size, Long currentUserId) {
        Page<Comment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getEventId, eventId)
                        .orderByDesc(Comment::getId));

        List<CommentResponse> records = pageResult.getRecords().stream()
                .map(comment -> toResponse(comment, currentUserId))
                .toList();

        PageResult<CommentResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    public Result<Void> toggleLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }

        CommentLike existingLike = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId));

        if (existingLike != null) {
            if (existingLike.getIsDeleted() == 0) {
                existingLike.setIsDeleted(1);
                commentLikeMapper.updateById(existingLike);
                return Result.success("取消点赞", null);
            } else {
                existingLike.setIsDeleted(0);
                commentLikeMapper.updateById(existingLike);
                return Result.success("点赞成功", null);
            }
        } else {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLikeMapper.insert(commentLike);
            return Result.success("点赞成功", null);
        }
    }

    private CommentResponse toResponse(Comment comment) {
        return toResponse(comment, null);
    }

    private CommentResponse toResponse(Comment comment, Long currentUserId) {
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

        Long likeCount = commentLikeMapper.selectCount(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, comment.getId())
                        .eq(CommentLike::getIsDeleted, 0));
        response.setLikeCount(likeCount.intValue());

        if (currentUserId != null) {
            CommentLike userLike = commentLikeMapper.selectOne(
                    new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getCommentId, comment.getId())
                            .eq(CommentLike::getUserId, currentUserId)
                            .eq(CommentLike::getIsDeleted, 0));
            response.setIsLiked(userLike != null);
        } else {
            response.setIsLiked(false);
        }

        return response;
    }
}
