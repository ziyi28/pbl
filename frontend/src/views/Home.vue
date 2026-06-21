<template>
  <div class="home-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">活动大厅</h1>
      <p class="page-subtitle">发现校园里的精彩活动</p>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <el-input
          v-model="keyword"
          placeholder="搜索活动名称…"
          :prefix-icon="Search"
          clearable
          class="filter-input"
          @keyup.enter="resetAndLoad"
        />
      </div>
      <div class="filter-options">
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
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-checkbox
          v-model="openOnly"
          class="filter-check"
        >
          只看可报名
        </el-checkbox>
      </div>
    </div>

    <!-- 活动卡片网格 -->
    <div class="event-grid" v-loading="loading">
      <EventCard
        v-for="(event, index) in events"
        :key="event.id"
        :event="event"
        :style="{ animationDelay: `${index * 0.05}s` }"
        class="stagger-fade-in"
      />
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!loading && events.length === 0">
      <el-icon :size="48" class="empty-icon"><Calendar /></el-icon>
      <p class="empty-title">
        {{ events.length === 0 ? '暂无活动' : '没有符合条件的活动' }}
      </p>
      <p class="empty-desc">
        {{ events.length === 0 ? '还没有人发布活动，敬请期待' : '试试调整筛选条件或搜索关键词' }}
      </p>
      <button
        v-if="events.length === 0 && userStore.isAdmin"
        class="empty-action"
        @click="$router.push('/events/create')"
      >
        发布第一个活动
      </button>
    </div>

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
import { ref, onMounted, watch } from 'vue'
import { Search, Calendar } from '@element-plus/icons-vue'
import { getEvents } from '../api/event'
import EventCard from '../components/EventCard.vue'
import { useUserStore } from '../stores/user'
import type { EventItem, EventCategory, EventStatus } from '../types'

const userStore = useUserStore()

const events = ref<EventItem[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const category = ref<EventCategory | ''>('')
const status = ref<EventStatus | ''>('')
const openOnly = ref(false)

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
      availableOnly: openOnly.value || undefined,
    })
    events.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// openOnly 切换时重新加载
watch(openOnly, () => resetAndLoad())

onMounted(() => loadEvents())
</script>

<style scoped>
/* ── 页面标题 ────────────────────────── */
.page-header {
  text-align: center;
  padding: 36px 20px 8px;
  margin-top: 56px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.3px;
}

.page-subtitle {
  color: var(--c-text-muted);
  font-size: 14px;
  margin-top: 4px;
}

/* ── 筛选栏 ──────────────────────────── */
.filter-bar {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-wrapper {
  width: 100%;
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-select {
  width: 130px;
  flex-shrink: 0;
}

.filter-check {
  margin-left: 4px;
  flex-shrink: 0;
  font-size: 13px;
  height: 32px;
  line-height: 32px;
}

/* 统一控件高度 */
:deep(.filter-input .el-input__wrapper),
:deep(.filter-select .el-input__wrapper) {
  height: 40px;
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

/* ── 卡片网格 ────────────────────────── */
.event-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 200px;
  max-width: 960px;
  margin: 0 auto;
  padding: 12px 0 24px;
}

/* ── 空状态 ──────────────────────────── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  color: #D4D4D8;
  margin-bottom: 14px;
}

.empty-title {
  font-size: 16px;
  font-weight: 500;
  color: #71717A;
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 13px;
  color: #A1A1AA;
  margin-bottom: 18px;
}

.empty-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 22px;
  font-size: 13px;
  font-weight: 600;
  color: #FFFFFF;
  background: #B45309;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-base);
  transition: background 0.2s ease;
}

.empty-action:hover {
  background: #D97742;
}

/* ── 分页 ────────────────────────────── */
.pagination {
  display: flex;
  justify-content: center;
  padding: 8px 0 40px;
}

.pagination-muted {
  text-align: center;
  padding: 8px 0 40px;
  font-size: 12px;
  color: var(--c-text-muted);
}

/* ── 动画 ────────────────────────────── */
.stagger-fade-in {
  animation: fadeInUp 0.35s ease both;
}

@keyframes fadeInUp {
  0%   { transform: translateY(10px); opacity: 0; }
  100% { transform: translateY(0);    opacity: 1; }
}

/* ── 响应式 ──────────────────────────── */
@media (max-width: 900px) {
  .event-grid {
    grid-template-columns: repeat(2, 1fr);
    padding: 12px 16px 24px;
  }

  .filter-bar {
    padding: 20px 16px 12px;
  }
}

@media (max-width: 640px) {
  .event-grid {
    grid-template-columns: 1fr;
    max-width: 400px;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
