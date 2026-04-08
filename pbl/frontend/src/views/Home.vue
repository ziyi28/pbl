<template>
  <div class="home-page">
    <div class="hero-section">
      <h1 class="bounce-in">探索卓越校园体验</h1>
      <p class="fade-in-up">参与、分享、发现属于你的高光时刻</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar glass-effect">
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          placeholder="探索你感兴趣的活动..."
          :prefix-icon="Search"
          clearable
          class="custom-input"
          @keyup.enter="loadEvents"
        />
      </div>
      <div class="filter-selectors">
        <el-select v-model="category" placeholder="全部分类" clearable class="custom-select" @change="loadEvents">
          <el-option label="讲座" value="LECTURE" />
          <el-option label="文体" value="SPORTS" />
          <el-option label="社团" value="CLUB" />
          <el-option label="志愿" value="VOLUNTEER" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="status" placeholder="所有状态" clearable class="custom-select" @change="loadEvents">
          <el-option label="报名中" value="OPEN" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="event-grid" v-loading="loading">
      <EventCard v-for="(event, index) in events" :key="event.id" :event="event" :style="{ animationDelay: `${index * 0.1}s` }" class="stagger-fade-in" />
    </div>

    <el-empty v-if="!loading && events.length === 0" description="在这片荒原中，暂时没有发现活动的踪迹" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadEvents"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getEvents } from '../api/event'
import EventCard from '../components/EventCard.vue'
import type { EventItem, EventCategory, EventStatus } from '../types'

const events = ref<EventItem[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const category = ref<EventCategory | ''>('')
const status = ref<EventStatus | ''>('')

async function loadEvents() {
  loading.value = true
  try {
    const res = await getEvents({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
      category: (category.value || undefined) as EventCategory | undefined,
      status: (status.value || undefined) as EventStatus | undefined,
    })
    events.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => loadEvents())
</script>

<style scoped>
.hero-section {
  text-align: center;
  padding: 60px 0 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.hero-section h1 {
  font-size: 56px;
  font-weight: 800;
  letter-spacing: -1px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-secondary-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-section p {
  color: var(--c-text-light);
  font-size: 20px;
  font-weight: 500;
  max-width: 600px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.search-input-wrapper {
  flex: 1;
  min-width: 280px;
}

.filter-selectors {
  display: flex;
  gap: 16px;
}

:deep(.custom-input .el-input__wrapper),
:deep(.custom-select .el-input__wrapper) {
  height: 48px;
  border-radius: var(--radius-md) !important;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.custom-input .el-input__wrapper.is-focus),
:deep(.custom-select .el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--c-primary-light);
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.1) !important;
}

.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  min-height: 200px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 20px;
}

:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--c-primary);
}

/* Animations */
.bounce-in {
  animation: bounceIn 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.fade-in-up {
  animation: fadeInUp 1s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
  animation-delay: 0.2s;
}

.stagger-fade-in {
  animation: fadeInUp 0.5s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
}

@keyframes bounceIn {
  0% { transform: scale(0.9); opacity: 0; }
  50% { transform: scale(1.02); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes fadeInUp {
  0% { transform: translateY(20px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

@media (max-width: 768px) {
  .hero-section h1 { font-size: 36px; }
  .filter-bar { flex-direction: column; align-items: stretch; }
  .filter-selectors { width: 100%; justify-content: space-between; }
  .custom-select { flex: 1; }
}
</style>
