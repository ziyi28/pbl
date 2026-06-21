<template>
  <div
    class="event-card"
    :class="{ 'is-interactive': interactive, 'is-static': !interactive }"
    :role="interactive ? 'button' : undefined"
    :tabindex="interactive ? 0 : undefined"
    @click="handleClick"
    @keydown.enter="handleClick"
  >
    <!-- 封面区 -->
    <div class="card-cover" :style="coverStyle">
      <img v-if="event.coverImage" :src="event.coverImage" class="cover-img" :alt="event.title" />
      <template v-else>
        <el-icon :size="40" class="cover-icon"><component :is="categoryIcon" /></el-icon>
      </template>

      <!-- 状态徽章 -->
      <span class="status-badge" :class="statusClass">{{ statusLabel }}</span>

      <!-- 已报名标记 -->
      <span v-if="event.isRegistered" class="reg-badge">
        <el-icon :size="12"><Check /></el-icon>
        已报名
      </span>
      <span v-else-if="event.isFavorited" class="fav-indicator">
        <el-icon :size="14"><StarFilled /></el-icon>
      </span>
    </div>

    <!-- 卡片内容 -->
    <div class="card-body">
      <div class="card-meta-top">
        <span class="category-tag" :style="{ color: categoryColor }">
          {{ CategoryLabels[event.category] || event.category }}
        </span>
        <span class="creator-name">{{ event.creatorName }}</span>
      </div>

      <h3 class="card-title">{{ event.title }}</h3>

      <div class="card-info">
        <div class="info-row">
          <el-icon :size="14" class="info-icon"><Location /></el-icon>
          <span class="info-text">{{ event.location }}</span>
        </div>
        <div class="info-row">
          <el-icon :size="14" class="info-icon"><Clock /></el-icon>
          <span class="info-text">{{ formatDateTime(event.startTime) }}</span>
        </div>
        <div v-if="event.status === 'OPEN'" class="info-row">
          <el-icon :size="14" class="info-icon"><Calendar /></el-icon>
          <span class="info-text" :class="{ 'deadline-warn': isDeadlineClose }">
            截止 {{ formatDate(event.registrationDeadline) }}
          </span>
        </div>
      </div>

      <!-- 名额进度条 -->
      <div class="capacity-row">
        <div class="progress-track">
          <div
            class="progress-fill"
            :class="capacityLevel"
            :style="{ width: progressPercent(event) + '%' }"
          />
        </div>
        <span class="capacity-text">
          <span class="capacity-current">{{ event.currentParticipants }}</span>
          <span class="capacity-sep">/</span>
          <span class="capacity-max">{{ event.maxParticipants }}</span>
          <span v-if="showRemaining" class="capacity-left">（余{{ remainingSlots(event) }}）</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Location,
  Clock,
  Calendar,
  Microphone,
  TrophyBase,
  UserFilled,
  StarFilled,
  MoreFilled,
  Check,
} from '@element-plus/icons-vue'
import type { EventItem, EventCategory } from '../types'
import {
  getStatusLabel,
  getStatusClass,
  progressPercent,
  remainingSlots,
  isDeadlinePassed,
  isFull,
  formatDate,
  formatDateTime,
  getCategoryColor,
  getCategoryBg,
  CategoryLabels,
} from '../utils/eventUtils'

const props = withDefaults(defineProps<{
  event: EventItem
  /** 是否可交互（点击跳转详情）。默认 true */
  interactive?: boolean
}>(), {
  interactive: true,
})

const router = useRouter()

function handleClick() {
  if (!props.interactive) return
  router.push(`/events/${props.event.id}`)
}

const categoryIcon = computed(() => {
  const map: Record<EventCategory, any> = {
    LECTURE: Microphone,
    SPORTS: TrophyBase,
    CLUB: UserFilled,
    VOLUNTEER: StarFilled,
    OTHER: MoreFilled,
  }
  return map[props.event.category] ?? MoreFilled
})

const categoryColor = computed(() => getCategoryColor(props.event.category))

const coverStyle = computed(() => {
  if (props.event.coverImage) return {}
  return { backgroundColor: getCategoryBg(props.event.category) }
})

const statusLabel = computed(() => getStatusLabel(props.event))
const statusClass = computed(() => getStatusClass(props.event))

/** 只在活动 OPEN 且未截止/未满时显示「余 X」 */
const showRemaining = computed(() => {
  return props.event.status === 'OPEN'
    && !isDeadlinePassed(props.event)
    && !isFull(props.event)
})

/** 报名截止是否临近（48小时内） */
const isDeadlineClose = computed(() => {
  if (props.event.status !== 'OPEN') return false
  const deadline = new Date(props.event.registrationDeadline).getTime()
  const now = Date.now()
  const diff = deadline - now
  return diff > 0 && diff < 48 * 60 * 60 * 1000
})

/** 进度条颜色级别 */
const capacityLevel = computed(() => {
  const pct = progressPercent(props.event)
  if (pct >= 95) return 'fill-full'
  if (pct >= 70) return 'fill-high'
  return 'fill-normal'
})
</script>

<style scoped>
/* ── 卡片整体 ────────────────────────── */
.event-card {
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  display: flex;
  flex-direction: column;
}

.event-card.is-interactive {
  cursor: pointer;
}

.event-card.is-interactive:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.event-card.is-interactive:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

/* ── 封面区 ──────────────────────────── */
.card-cover {
  position: relative;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-icon {
  opacity: 0.55;
}

/* ── 状态徽章 ────────────────────────── */
.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.6;
  pointer-events: none;
}

.status-open {
  background: #FFF7ED;
  color: #B45309;
}

.status-closed {
  background: #F4F4F5;
  color: #71717A;
}

.status-ongoing {
  background: #ECFDF5;
  color: #059669;
}

.status-ended {
  background: #FAFAFA;
  color: #A1A1AA;
}

.status-cancelled {
  background: #FEF2F2;
  color: #DC2626;
}

/* 已报名标记 */
.reg-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  background: #059669;
  color: #FFFFFF;
  pointer-events: none;
}

/* 已收藏星标 */
.fav-indicator {
  position: absolute;
  top: 10px;
  left: 10px;
  color: #B45309;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2));
  pointer-events: none;
}

/* ── 卡片内容区 ──────────────────────── */
.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.card-meta-top {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.category-tag {
  font-weight: 600;
}

.creator-name {
  color: var(--c-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ── 信息行 ──────────────────────────── */
.card-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-icon {
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.info-text {
  font-size: 12px;
  color: var(--c-text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deadline-warn {
  color: #DC2626;
  font-weight: 500;
}

/* ── 名额进度条 ──────────────────────── */
.capacity-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.progress-track {
  flex: 1;
  height: 5px;
  background: #F4F4F5;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.4s ease;
}

.fill-normal {
  background: #B45309;
}

.fill-high {
  background: #EA580C;
}

.fill-full {
  background: #DC2626;
}

.capacity-text {
  font-size: 12px;
  color: var(--c-text-muted);
  white-space: nowrap;
}

.capacity-current {
  font-weight: 600;
  color: var(--c-text);
}

.capacity-sep {
  color: var(--c-text-muted);
  margin: 0 1px;
}

.capacity-max {
  color: var(--c-text-muted);
}

.capacity-left {
  color: var(--c-text-muted);
  font-size: 11px;
}

/* ── 响应式 ──────────────────────────── */
@media (max-width: 640px) {
  .card-cover {
    height: 140px;
  }
}
</style>
