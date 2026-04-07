<template>
  <div class="home-page">
    <div class="page-header">
      <h1>校园活动</h1>
      <p>发现精彩活动，丰富校园生活</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索活动..."
        :prefix-icon="Search"
        clearable
        style="width: 300px"
        @keyup.enter="loadEvents"
      />
      <el-select v-model="category" placeholder="分类" clearable style="width: 120px" @change="loadEvents">
        <el-option label="讲座" value="LECTURE" />
        <el-option label="文体" value="SPORTS" />
        <el-option label="社团" value="CLUB" />
        <el-option label="志愿" value="VOLUNTEER" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-select v-model="status" placeholder="状态" clearable style="width: 120px" @change="loadEvents">
        <el-option label="报名中" value="OPEN" />
        <el-option label="进行中" value="ONGOING" />
        <el-option label="已结束" value="ENDED" />
      </el-select>
    </div>

    <!-- 活动列表 -->
    <div class="event-grid" v-loading="loading">
      <EventCard v-for="event in events" :key="event.id" :event="event" />
    </div>

    <el-empty v-if="!loading && events.length === 0" description="暂无活动" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadEvents"
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
.page-header {
  text-align: center;
  padding: 40px 0 30px;
}

.page-header h1 {
  font-size: 32px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-header p {
  color: #909399;
  font-size: 16px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
