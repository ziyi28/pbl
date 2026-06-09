<template>
  <el-card shadow="never" class="event-card" @click="$router.push(`/events/${event.id}`)" :body-style="{ padding: '0px' }">
    <div class="card-cover-wrapper">
      <div class="card-cover" v-if="event.coverImage">
        <img :src="event.coverImage" :alt="event.title" />
      </div>
      <div class="card-cover placeholder" v-else>
        <el-icon :size="40"><Calendar /></el-icon>
      </div>
      <!-- Floating status badge over image -->
      <el-tag class="status-badge" :type="statusType" effect="plain" size="small">{{ StatusMap[event.status] }}</el-tag>
    </div>
    
    <div class="card-body">
      <div class="card-header">
        <h3 class="card-title">{{ event.title }}</h3>
      </div>
      <div class="card-meta">
        <span><el-icon><Location /></el-icon> {{ event.location }}</span>
        <span><el-icon><Clock /></el-icon> {{ formatDate(event.startTime) }}</span>
      </div>
      <div class="card-footer">
        <el-tag size="small" effect="light" class="category-tag">{{ CategoryMap[event.category] }}</el-tag>
        <div class="participants-info">
          <el-icon><User /></el-icon>
          <span>{{ event.currentParticipants }} / {{ event.maxParticipants }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Location, Clock, Calendar, User } from '@element-plus/icons-vue'
import type { EventItem } from '../types'
import { CategoryMap, StatusMap } from '../types'

const props = defineProps<{ event: EventItem }>()

const statusType = computed(() => {
  switch (props.event.status) {
    case 'OPEN': return 'success'
    case 'ONGOING': return 'warning'
    case 'ENDED': return 'info'
    default: return 'info'
  }
})

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.event-card {
  cursor: pointer;
  border-radius: 8px !important;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  border: 1px solid #d5d5d5 !important;
  background: #ffffff !important;
}

.event-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
}

.event-card:hover .card-cover img {
  transform: scale(1.02);
}

.card-cover-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.card-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  /* Beautiful animated gradient for fallback */
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  color: #fff;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.card-body {
  padding: 20px;
}

.card-header {
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.card-meta {
  color: #8e8e93;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed rgba(0, 0, 0, 0.08);
}

.category-tag {
  border-radius: 6px;
  padding: 0 12px;
  background-color: #f5f5f7 !important;
  color: #000000 !important;
  border: none !important;
}

.participants-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--c-primary);
  background: rgba(118, 75, 162, 0.08);
  padding: 4px 10px;
  border-radius: 12px;
}
</style>
