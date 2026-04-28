// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  avatar: string | null
  bio: string | null
  role: 'USER' | 'ADMIN'
  createdAt: string
}

// 活动相关类型
export type EventCategory = 'LECTURE' | 'SPORTS' | 'CLUB' | 'VOLUNTEER' | 'OTHER'
export type EventStatus = 'OPEN' | 'ONGOING' | 'ENDED'

export interface EventItem {
  id: number
  title: string
  description: string
  category: EventCategory
  location: string
  startTime: string
  endTime: string
  registrationDeadline: string
  maxParticipants: number
  currentParticipants: number
  coverImage: string | null
  status: EventStatus
  creatorId: number
  creatorName: string
  createdAt: string
  isRegistered?: boolean
  isFavorited?: boolean
}

// 评论类型
export interface Comment {
  id: number
  content: string
  userId: number
  username: string
  userAvatar: string | null
  eventId: number
  createdAt: string
}

// 统一响应类型
export interface Result<T> {
  code: number
  message: string
  data: T
}

// 分页类型
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 请求类型
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface EventCreateRequest {
  title: string
  description: string
  category: EventCategory
  location: string
  startTime: string
  endTime: string
  registrationDeadline: string
  maxParticipants: number
  coverImage?: string
}

export interface EventUpdateRequest {
  title?: string
  description?: string
  category?: EventCategory
  location?: string
  startTime?: string
  endTime?: string
  registrationDeadline?: string
  maxParticipants?: number
  coverImage?: string
}

export interface CommentCreateRequest {
  content: string
}

export interface UserUpdateRequest {
  avatar?: string
  email?: string
  bio?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  username: string
  role: string
}

// 分类映射
export const CategoryMap: Record<EventCategory, string> = {
  LECTURE: '讲座',
  SPORTS: '文体',
  CLUB: '社团',
  VOLUNTEER: '志愿',
  OTHER: '其他',
}

// 状态映射
export const StatusMap: Record<EventStatus, string> = {
  OPEN: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
}
