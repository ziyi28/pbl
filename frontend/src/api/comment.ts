import request from './index'
import type { Result, PageResult, Comment, CommentCreateRequest } from '../types'

export function getComments(eventId: number, params: { page?: number; size?: number }) {
  return request.get<any, Result<PageResult<Comment>>>(`/events/${eventId}/comments`, { params })
}

export function createComment(eventId: number, data: CommentCreateRequest) {
  return request.post<any, Result<Comment>>(`/events/${eventId}/comments`, data)
}

export function createReply(eventId: number, commentId: number, data: CommentCreateRequest) {
  return request.post<any, Result<Comment>>(`/events/${eventId}/comments/${commentId}/replies`, data)
}

export function deleteComment(commentId: number) {
  return request.delete<any, Result<void>>(`/comments/${commentId}`)
}

export function toggleCommentLike(eventId: number, commentId: number) {
  return request.post<any, Result<void>>(`/events/${eventId}/comments/${commentId}/likes`)
}
