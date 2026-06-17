<template>
  <el-card shadow="never" class="event-card" @click="$router.push(`/events/${event.id}`)" :body-style="{ padding: '0px' }">
    <!-- 图片区：纯色背景 + 分类线性图标 -->
    <div class="card-cover" :class="`cover-${event.category}`">
      <el-icon :size="48" class="cover-icon"><component :is="categoryIcon" /></el-icon>
      <!-- 状态徽章 —— 浅色底深色字 -->
      <span class="status-badge" :class="`badge-${event.status}`">
        {{ StatusMap[event.status] }}
      </span>
    </div>

    <!-- 卡片内容 -->
    <div class="card-body">
      <h3 class="card-title">{{ event.title }}</h3>
      <div class="card-meta">
        <span class="meta-item">
          <el-icon :size="14"><Location /></el-icon>
          <span>{{ event.location }}</span>
        </span>
        <span class="meta-item">
          <el-icon :size="14"><Clock /></el-icon>
          <span>{{ formatDate(event.startTime) }}</span>
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Location,
  Clock,
  Microphone,
  TrophyBase,
  UserFilled,
  StarFilled,
  MoreFilled,
} from '@element-plus/icons-vue'
import type { EventItem, EventCategory } from '../types'
import { StatusMap } from '../types'

const props = defineProps<{ event: EventItem }>()

/** 按分类返回对应图标组件 */
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

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
/* ── 卡片整体 ────────────────────────── */
.event-card {
  cursor: pointer;
  border-radius: var(--radius-md) !important;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid var(--c-border) !important;
  background: var(--c-surface) !important;
}

.event-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

/* ── 封面区（纯色背景 + 图标） ────────── */
.card-cover {
  position: relative;
  height: 152px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 各分类背景色 */
.cover-LECTURE    { background: #FFF7ED; }  /* amber-50  — 讲座暖调 */
.cover-SPORTS     { background: #F4F4F5; }  /* zinc-100 — 体育中性灰 */
.cover-CLUB       { background: #F5F5F4; }  /* stone-100 — 社团大地灰 */
.cover-VOLUNTEER  { background: #FEF3C7; }  /* amber-100 — 志愿暖调 */
.cover-OTHER      { background: #FAFAFA; }  /* zinc-50  — 通用浅灰 */

/* 各分类图标颜色 */
.cover-LECTURE   .cover-icon { color: #B45309; }
.cover-SPORTS    .cover-icon { color: #52525B; }
.cover-CLUB      .cover-icon { color: #78716C; }
.cover-VOLUNTEER .cover-icon { color: #B45309; }
.cover-OTHER     .cover-icon { color: #A1A1AA; }

/* ── 状态徽章（浅色底 + 深色字） ──────── */
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
  pointer-events: none;
}

.badge-OPEN {
  background: #FFF7ED;
  color: #B45309;
}

.badge-ONGOING {
  background: #F4F4F5;
  color: #52525B;
}

.badge-ENDED {
  background: #FAFAFA;
  color: #A1A1AA;
}

/* ── 卡片内容区 ──────────────────────── */
.card-body {
  padding: 16px 18px 18px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 12px;
}

/* ── 次级信息（地点 + 时间） ──────────── */
.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--c-text-light);
}

.meta-item .el-icon {
  color: var(--c-text-muted);
  flex-shrink: 0;
}
</style>
