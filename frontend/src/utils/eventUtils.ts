import type { EventItem } from '../types'

/**
 * 活动是否可报名
 * 条件：状态为 OPEN、未过报名截止时间、名额未满、且用户未报名
 */
export function canRegister(event: EventItem): boolean {
  if (event.isRegistered) return false
  if (event.status !== 'OPEN') return false
  if (new Date(event.registrationDeadline) < new Date()) return false
  if (event.currentParticipants >= event.maxParticipants) return false
  return true
}

/**
 * 活动是否可取消报名
 * 条件：用户已报名、活动状态为 OPEN、活动未开始
 */
export function canCancelRegistration(event: EventItem): boolean {
  if (!event.isRegistered) return false
  if (event.status !== 'OPEN') return false
  // 活动尚未开始
  if (new Date(event.startTime) <= new Date()) return false
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
 * 优先表达报名状态，不模糊：
 *   可报名 → 报名中
 *   已截止 → 报名已截止
 *   已满   → 名额已满
 *   进行中 → 进行中
 *   已结束 → 已结束
 */
export function getStatusLabel(event: EventItem): string {
  if (event.status === 'ENDED') return '已结束'
  if (event.status === 'ONGOING') return '进行中'
  // status === OPEN
  if (isDeadlinePassed(event)) return '报名已截止'
  if (isFull(event)) return '名额已满'
  return '报名中'
}

/**
 * 辅助状态描述：在「报名中」的前提下，如需强调即将开始
 */
export function getSubStatusLabel(event: EventItem): string {
  if (event.status === 'OPEN' && !isDeadlinePassed(event) && !isFull(event)) {
    const now = Date.now()
    const start = new Date(event.startTime).getTime()
    const hoursUntil = (start - now) / 3600_000
    if (hoursUntil > 0 && hoursUntil <= 24) return '即将开始'
    if (hoursUntil > 24) return ''
  }
  return ''
}

/**
 * 活动状态对应的颜色类名
 */
export function getStatusClass(event: EventItem): string {
  if (event.status === 'ENDED') return 'status-ended'
  if (event.status === 'ONGOING') return 'status-ongoing'
  if (isDeadlinePassed(event) || isFull(event)) return 'status-closed'
  return 'status-open'
}

/**
 * 格式化日期为简短字符串 MM-DD HH:mm
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
 * 格式化完整日期时间 YYYY-MM-DD HH:mm
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
 * 将 Date | string 转为本地时间字符串 YYYY-MM-DDTHH:mm:ss（避免 toISOString 时区偏移）
 */
export function toLocalDateTimeString(val: string | Date): string {
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/**
 * 分类对应的颜色色值
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
 * 分类对应的浅色背景色
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

export const StatusLabels: Record<string, string> = {
  OPEN: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
}

export const CategoryLabels: Record<string, string> = {
  LECTURE: '讲座',
  SPORTS: '文体',
  CLUB: '社团',
  VOLUNTEER: '志愿',
  OTHER: '其他',
}
