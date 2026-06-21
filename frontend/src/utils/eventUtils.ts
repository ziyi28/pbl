import type { EventItem } from '../types'

/**
 * 活动是否可报名
 * 条件：状态为 OPEN（报名中）、未过报名截止时间、名额未满
 */
export function canRegister(event: EventItem): boolean {
  if (event.status !== 'OPEN') return false
  if (new Date(event.registrationDeadline) < new Date()) return false
  if (event.currentParticipants >= event.maxParticipants) return false
  return true
}

/**
 * 名额是否已满
 */
export function isFull(event: EventItem): boolean {
  return event.currentParticipants >= event.maxParticipants
}

/**
 * 报名是否已截止
 */
export function isDeadlinePassed(event: EventItem): boolean {
  return new Date(event.registrationDeadline) < new Date()
}

/**
 * 活动是否已结束
 */
export function isEnded(event: EventItem): boolean {
  return event.status === 'ENDED'
}

/**
 * 活动是否进行中
 */
export function isOngoing(event: EventItem): boolean {
  return event.status === 'ONGOING'
}

/**
 * 活动是否未开始（报名中且未到开始时间）
 */
export function isUpcoming(event: EventItem): boolean {
  return event.status === 'OPEN' && new Date(event.startTime) > new Date()
}

/**
 * 剩余名额
 */
export function remainingSlots(event: EventItem): number {
  return Math.max(0, event.maxParticipants - event.currentParticipants)
}

/**
 * 报名进度百分比（0-100）
 */
export function progressPercent(event: EventItem): number {
  if (event.maxParticipants === 0) return 0
  return Math.min(100, Math.round((event.currentParticipants / event.maxParticipants) * 100))
}

/**
 * 活动的综合显示状态标签
 * 返回用于卡片/列表展示的次要状态文本
 */
export function getStatusLabel(event: EventItem): string {
  if (event.status === 'ENDED') return '已结束'
  if (event.status === 'ONGOING') return '进行中'
  // status === OPEN
  if (isDeadlinePassed(event)) return '报名已截止'
  if (isFull(event)) return '名额已满'
  if (isUpcoming(event)) return '即将开始'
  return '报名中'
}

/**
 * 活动状态对应的颜色类名（用于 CSS 定制）
 */
export function getStatusClass(event: EventItem): string {
  if (event.status === 'ENDED') return 'status-ended'
  if (event.status === 'ONGOING') return 'status-ongoing'
  // OPEN
  if (isDeadlinePassed(event) || isFull(event)) return 'status-closed'
  return 'status-open'
}

/**
 * 格式化日期时间为简短字符串
 */
export function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${h}:${min}`
}

/**
 * 格式化完整日期时间
 */
export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

/**
 * 获取分类对应的 Material Icon 名称 / Element Plus 图标组件名
 */
export function getCategoryColor(category: string): string {
  const map: Record<string, string> = {
    LECTURE: '#B45309',
    SPORTS: '#059669',
    CLUB: '#6366F1',
    VOLUNTEER: '#D97742',
    OTHER: '#71717A',
  }
  return map[category] || '#71717A'
}

/**
 * 获取分类对应的浅色背景色
 */
export function getCategoryBg(category: string): string {
  const map: Record<string, string> = {
    LECTURE: '#FFF7ED',
    SPORTS: '#ECFDF5',
    CLUB: '#EEF2FF',
    VOLUNTEER: '#FFF7ED',
    OTHER: '#FAFAFA',
  }
  return map[category] || '#FAFAFA'
}

/**
 * 活动状态映射（中文）
 */
export const StatusLabels: Record<string, string> = {
  OPEN: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
}

/**
 * 活动分类映射（中文）
 */
export const CategoryLabels: Record<string, string> = {
  LECTURE: '讲座',
  SPORTS: '文体',
  CLUB: '社团',
  VOLUNTEER: '志愿',
  OTHER: '其他',
}
