package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.campus.common.PageResult;
import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.CommentCreateRequest;
import com.pbl.campus.dto.response.CommentResponse;
import com.pbl.campus.entity.Comment;
import com.pbl.campus.entity.CommentLike;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.CommentLikeMapper;
import com.pbl.campus.mapper.CommentMapper;
import com.pbl.campus.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;

    public Result<CommentResponse> createComment(Long userId, Long eventId, CommentCreateRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUserId(userId);
        comment.setEventId(eventId);
        comment.setParentId(null);
        commentMapper.insert(comment);

        return Result.success("评论成功", toResponse(comment, 0L, false, new ArrayList<>()));
    }

    public Result<CommentResponse> createReply(Long userId, Long parentId, CommentCreateRequest request) {
        Comment parentComment = commentMapper.selectById(parentId);
        if (parentComment == null) {
            return Result.error(404, "父评论不存在");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUserId(userId);
        comment.setEventId(parentComment.getEventId());
        comment.setParentId(parentId);
        commentMapper.insert(comment);

        return Result.success("回复成功", toResponse(comment, 0L, false, new ArrayList<>()));
    }

    public Result<Void> deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            return Result.error(403, "无权删除该评论");
        }

        // 删除评论及其所有回复
        deleteCommentWithReplies(commentId);
        return Result.success("评论已删除", null);
    }

    private void deleteCommentWithReplies(Long commentId) {
        // 先删除所有子回复
        List<Comment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        for (Comment reply : replies) {
            deleteCommentWithReplies(reply.getId());
        }
        // 删除点赞
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>().eq(CommentLike::getCommentId, commentId));
        // 删除评论
        commentMapper.deleteById(commentId);
    }

    public Result<PageResult<CommentResponse>> listComments(Long eventId, Long userId, int page, int size) {
        // 只查询顶级评论（parentId为null）
        Page<Comment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getEventId, eventId)
                        .isNull(Comment::getParentId)
                        .orderByDesc(Comment::getCreatedAt));

        List<Comment> comments = pageResult.getRecords();
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();

        Map<Long, Long> likeCountMap = getLikeCountMap(commentIds);
        Map<Long, Boolean> likedMap = userId != null ? getLikedMap(commentIds, userId) : new HashMap<>();

        // 获取每条顶级评论的回复
        List<CommentResponse> records = comments.stream()
                .map(comment -> {
                    List<CommentResponse> replies = getReplies(comment.getId(), userId);
                    return toResponse(comment, likeCountMap.get(comment.getId()), likedMap.getOrDefault(comment.getId(), false), replies);
                })
                .toList();

        PageResult<CommentResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    private List<CommentResponse> getReplies(Long parentId, Long userId) {
        List<Comment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getParentId, parentId)
                        .orderByAsc(Comment::getCreatedAt));

        List<Long> replyIds = replies.stream().map(Comment::getId).toList();
        Map<Long, Long> likeCountMap = getLikeCountMap(replyIds);
        Map<Long, Boolean> likedMap = userId != null ? getLikedMap(replyIds, userId) : new HashMap<>();

        return replies.stream()
                .map(reply -> toResponse(reply, likeCountMap.get(reply.getId()), likedMap.getOrDefault(reply.getId(), false), new ArrayList<>()))
                .toList();
    }

    public Result<Void> toggleLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }

        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(wrapper);

        if (existingLike != null) {
            commentLikeMapper.deleteById(existingLike.getId());
            return Result.success("取消点赞成功", null);
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            return Result.success("点赞成功", null);
        }
    }

    private Map<Long, Long> getLikeCountMap(List<Long> commentIds) {
        Map<Long, Long> likeCountMap = new HashMap<>();
        if (commentIds.isEmpty()) {
            return likeCountMap;
        }
        
        List<CommentLike> likes = commentLikeMapper.selectList(
                new LambdaQueryWrapper<CommentLike>().in(CommentLike::getCommentId, commentIds));
        
        for (CommentLike like : likes) {
            likeCountMap.merge(like.getCommentId(), 1L, Long::sum);
        }
        return likeCountMap;
    }

    private Map<Long, Boolean> getLikedMap(List<Long> commentIds, Long userId) {
        Map<Long, Boolean> likedMap = new HashMap<>();
        if (commentIds.isEmpty()) {
            return likedMap;
        }
        
        List<CommentLike> likes = commentLikeMapper.selectList(
                new LambdaQueryWrapper<CommentLike>()
                        .in(CommentLike::getCommentId, commentIds)
                        .eq(CommentLike::getUserId, userId));
        
        for (CommentLike like : likes) {
            likedMap.put(like.getCommentId(), true);
        }
        return likedMap;
    }

    private CommentResponse toResponse(Comment comment, Long likeCount, Boolean liked, List<CommentResponse> replies) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUserId());
        response.setEventId(comment.getEventId());
        response.setParentId(comment.getParentId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setLikeCount(likeCount != null ? likeCount : 0L);
        response.setLiked(liked != null ? liked : false);
        response.setReplies(replies);
        response.setReplyCount((long) replies.size());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setUserAvatar(user.getAvatar());
        }
        return response;
    }
}
