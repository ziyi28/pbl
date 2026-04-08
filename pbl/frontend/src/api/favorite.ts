import request from './index'
import type { Result, EventItem } from '../types'

export function addFavorite(eventId: number) {
  return request.post<any, Result<void>>(`/events/${eventId}/favorites`)
}

export function removeFavorite(eventId: number) {
  return request.delete<any, Result<void>>(`/events/${eventId}/favorites`)
}

export function getMyFavorites() {
  return request.get<any, Result<EventItem[]>>('/users/me/favorites')
}
