<template>
  <div class="home-page">
    <!-- Hero -->
    <section class="hero-section">
      <h1>探索校园活动</h1>
      <p>参与、发现、分享属于你的高光时刻</p>
    </section>

    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          placeholder="搜索你感兴趣的活动…"
          :prefix-icon="Search"
          clearable
          class="filter-input"
          @keyup.enter="resetAndLoad"
        />
      </div>
      <el-select
        v-model="category"
        placeholder="全部分类"
        clearable
        class="filter-select"
        @change="resetAndLoad"
      >
        <el-option label="讲座" value="LECTURE" />
        <el-option label="文体" value="SPORTS" />
        <el-option label="社团" value="CLUB" />
        <el-option label="志愿" value="VOLUNTEER" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-select
        v-model="status"
        placeholder="所有状态"
        clearable
        class="filter-select"
        @change="resetAndLoad"
      >
        <el-option label="报名中" value="OPEN" />
        <el-option label="进行中" value="ONGOING" />
        <el-option label="已结束" value="ENDED" />
      </el-select>
    </div>

    <!-- 活动卡片网格 -->
    <div class="event-grid" v-loading="loading">
      <EventCard
        v-for="(event, index) in events"
        :key="event.id"
        :event="event"
        :style="{ animationDelay: `${index * 0.06}s` }"
        class="stagger-fade-in"
      />
    </div>

    <el-empty
      v-if="!loading && events.length === 0"
      description="暂未发现活动，换个关键词试试"
    />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="9"
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

function resetAndLoad() {
  page.value = 1
  loadEvents()
}

async function loadEvents() {
  loading.value = true
  try {
    const res = await getEvents({
      page: page.value,
      size: 9,
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
/* ── Hero ────────────────────────────── */
.hero-section {
  text-align: center;
  padding: 48px 20px 28px;
  margin-top: 56px; /* navbar height */
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border-light);
}

.hero-section h1 {
  font-size: 30px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.hero-section p {
  color: var(--c-text-light);
  font-size: 15px;
  font-weight: 400;
}

/* ── 筛选栏 ──────────────────────────── */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px 0 28px;
  max-width: 960px;
  margin: 0 auto;
}

.search-input-wrapper {
  flex: 1;
  min-width: 0;
}

.filter-select {
  width: 140px;
  flex: 0 0 auto;
}

/* 统一三个控件的高度、边框、圆角 */
:deep(.filter-input .el-input__wrapper),
:deep(.filter-select .el-input__wrapper) {
  height: 44px;
  border-radius: var(--radius-sm) !important;
  background: var(--c-surface);
  box-shadow: none !important;
  border: 1px solid var(--c-border) !important;
}

:deep(.filter-input .el-input__wrapper.is-focus),
:deep(.filter-select .el-input__wrapper.is-focus) {
  border-color: var(--c-primary) !important;
  box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.1) !important;
}

:deep(.filter-input .el-input__prefix),
:deep(.filter-select .el-input__prefix) {
  color: var(--c-text-muted);
}

/* ── 卡片网格 ────────────────────────── */
.event-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 200px;
  max-width: 960px;
  margin: 0 auto;
  padding: 0 0 24px;
}

/* ── 分页 ────────────────────────────── */
.pagination {
  display: flex;
  justify-content: center;
  padding: 16px 0 40px;
}

/* ── 动画 ────────────────────────────── */
.stagger-fade-in {
  animation: fadeInUp 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  0%   { transform: translateY(12px); opacity: 0; }
  100% { transform: translateY(0);    opacity: 1; }
}

/* ── 响应式 ──────────────────────────── */
@media (max-width: 900px) {
  .event-grid {
    grid-template-columns: repeat(2, 1fr);
    padding: 0 16px;
  }

  .filter-bar {
    padding: 20px 16px 24px;
  }
}

@media (max-width: 640px) {
  .event-grid {
    grid-template-columns: 1fr;
    max-width: 400px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }

  .hero-section h1 {
    font-size: 24px;
  }
}
</style>
