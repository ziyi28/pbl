import request from './index'
import type { Result, NotificationItem } from '../types'

export function getNotifications() {
  return request.get<any, Result<NotificationItem[]>>('/notifications')
}

export function getUnreadCount() {
  return request.get<any, Result<number>>('/notifications/unread-count')
}

export function markAsRead(notificationId: number) {
  return request.put<any, Result<void>>(`/notifications/${notificationId}/read`)
}

export function markAllAsRead() {
  return request.put<any, Result<void>>('/notifications/read-all')
}
