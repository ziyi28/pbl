<template>
  <el-card shadow="hover" class="event-card" @click="$router.push(`/events/${event.id}`)">
    <div class="card-cover" v-if="event.coverImage">
      <img :src="event.coverImage" :alt="event.title" />
    </div>
    <div class="card-cover placeholder" v-else>
      <el-icon :size="40"><Calendar /></el-icon>
    </div>
    <div class="card-body">
      <div class="card-header">
        <h3 class="card-title">{{ event.title }}</h3>
        <el-tag :type="statusType" size="small">{{ StatusMap[event.status] }}</el-tag>
      </div>
      <div class="card-meta">
        <span><el-icon><Location /></el-icon> {{ event.location }}</span>
        <span><el-icon><Clock /></el-icon> {{ formatDate(event.startTime) }}</span>
      </div>
      <div class="card-footer">
        <el-tag size="small" effect="plain">{{ CategoryMap[event.category] }}</el-tag>
        <span class="participants">
          {{ event.currentParticipants }} / {{ event.maxParticipants }} 人
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
  transition: transform 0.2s;
}

.event-card:hover {
  transform: translateY(-4px);
}

.card-cover {
  height: 160px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title {
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.card-meta {
  color: #909399;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants {
  font-size: 13px;
  color: #606266;
}
</style>
