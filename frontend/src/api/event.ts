import request from './index'
import type { Result, PageResult, EventItem, EventCreateRequest, EventUpdateRequest, EventCategory, EventStatus, Participant } from '../types'

export function getEvents(params: {
  page?: number
  size?: number
  category?: EventCategory
  status?: EventStatus
  keyword?: string
  availableOnly?: boolean
}) {
  return request.get<any, Result<PageResult<EventItem>>>('/events', { params })
}

export function getEvent(id: number) {
  return request.get<any, Result<EventItem>>(`/events/${id}`)
}

export function createEvent(data: EventCreateRequest) {
  return request.post<any, Result<EventItem>>('/events', data)
}

export function updateEvent(id: number, data: EventUpdateRequest) {
  return request.put<any, Result<EventItem>>(`/events/${id}`, data)
}

export function deleteEvent(id: number) {
  return request.delete<any, Result<void>>(`/events/${id}`)
}

// 报名
export function registerEvent(eventId: number) {
  return request.post<any, Result<void>>(`/events/${eventId}/registrations`)
}

export function cancelRegistration(eventId: number) {
  return request.delete<any, Result<void>>(`/events/${eventId}/registrations`)
}

export function getMyRegistrations() {
  return request.get<any, Result<EventItem[]>>('/users/me/registrations')
}

export function getEventParticipants(eventId: number) {
  return request.get<any, Result<Participant[]>>(`/events/${eventId}/participants`)
}
