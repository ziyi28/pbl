<template>
  <div class="admin-events">
    <div class="page-header">
      <h1>活动管理台</h1>
      <p class="subtitle">集中管理所有活动</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input
          v-model="keyword"
          placeholder="搜索活动标题..."
          clearable
          style="width: 200px"
          @keyup.enter="resetAndLoad"
          @clear="resetAndLoad"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="category" placeholder="全部分类" clearable style="width: 120px" @change="resetAndLoad">
          <el-option label="讲座" value="LECTURE" />
          <el-option label="文体" value="SPORTS" />
          <el-option label="社团" value="CLUB" />
          <el-option label="志愿" value="VOLUNTEER" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="status" placeholder="全部状态" clearable style="width: 120px" @change="resetAndLoad">
          <el-option label="报名中" value="OPEN" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-checkbox v-model="availableOnly" @change="resetAndLoad">可报名</el-checkbox>
        <el-checkbox v-model="fullOnly" @change="resetAndLoad">已满</el-checkbox>
        <el-checkbox v-model="deadlinePassedOnly" @change="resetAndLoad">已截止</el-checkbox>
        <el-button @click="resetAndLoad">查询</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <el-table :data="events" v-loading="loading" stripe style="width: 100%" size="default">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="活动标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <router-link :to="`/events/${row.id}`" class="event-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="80">
          <template #default="{ row }">
            <el-tag size="small" :color="getCategoryBg(row.category)" :style="{ color: getCategoryColor(row.category), border: 'none' }">
              {{ CategoryMap[row.category as EventCategory] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTagType(row)">{{ getStatusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="150">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="报名截止" width="150">
          <template #default="{ row }">{{ formatDateTime(row.registrationDeadline) }}</template>
        </el-table-column>
        <el-table-column label="报名人数" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isFull(row) }">
              {{ row.currentParticipants }}/{{ row.maxParticipants }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="$router.push(`/events/${row.id}`)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button size="small" text type="primary" @click="$router.push(`/events/${row.id}/edit`)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" text type="primary" @click="showParticipants(row)">
              <el-icon><User /></el-icon> 报名
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
              <el-button size="small" text type="info">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="export">
                    <el-icon><Download /></el-icon> 导出名单
                  </el-dropdown-item>
                  <el-dropdown-item command="cancel" :disabled="row.status === 'CANCELLED' || row.status === 'ENDED'">
                    <el-icon><CircleClose /></el-icon> 取消活动
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon> 删除活动
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadEvents"
      />
    </div>

    <!-- 参与者弹窗 -->
    <el-dialog v-model="participantDialogVisible" title="参与者列表" width="600px">
      <div v-loading="participantLoading">
        <el-table :data="participants" stripe size="small" max-height="400">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column label="报名时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.registeredAt) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!participantLoading && participants.length === 0" description="暂无报名" />
      </div>
      <template #footer>
        <el-button @click="participantDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExportFromDialog">导出名单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Edit, User, MoreFilled, Download, CircleClose, Delete } from '@element-plus/icons-vue'
import { getEvents, deleteEvent, cancelEvent, exportParticipants as exportParticipantsApi, getEventParticipants } from '../api/event'
import { getStatusLabel, isFull, formatDateTime, getCategoryColor, getCategoryBg } from '../utils/eventUtils'
import { CategoryMap, type EventItem, type EventCategory, type EventStatus, type Participant } from '../types'

const events = ref<EventItem[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(15)
const total = ref(0)
const keyword = ref('')
const category = ref<EventCategory | ''>('')
const status = ref<EventStatus | ''>('')
const availableOnly = ref(false)
const fullOnly = ref(false)
const deadlinePassedOnly = ref(false)

// 参与者弹窗
const participantDialogVisible = ref(false)
const participantLoading = ref(false)
const participants = ref<Participant[]>([])
const currentEvent = ref<EventItem | null>(null)

function getStatusTagType(event: EventItem) {
  if (event.status === 'CANCELLED') return 'info'
  if (event.status === 'ENDED') return 'info'
  if (event.status === 'ONGOING') return 'success'
  if (isFull(event)) return 'danger'
  if (new Date(event.registrationDeadline) < new Date()) return 'warning'
  return ''
}

async function loadEvents() {
  loading.value = true
  try {
    const res = await getEvents({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      status: (status.value || undefined) as EventStatus | undefined,
      availableOnly: availableOnly.value || undefined,
      fullOnly: fullOnly.value || undefined,
      deadlinePassedOnly: deadlinePassedOnly.value || undefined,
    })
    events.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetAndLoad() {
  page.value = 1
  loadEvents()
}

async function showParticipants(event: EventItem) {
  currentEvent.value = event
  participantDialogVisible.value = true
  participantLoading.value = true
  try {
    const res = await getEventParticipants(event.id)
    participants.value = res.data
  } finally {
    participantLoading.value = false
  }
}

async function handleExportFromDialog() {
  if (!currentEvent.value) return
  await doExport(currentEvent.value.id)
}

async function doExport(eventId: number) {
  try {
    const res = await exportParticipantsApi(eventId)
    const blob = new Blob([res as any], { type: 'text/csv;charset=UTF-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `participants-${eventId}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

async function handleCommand(cmd: string, row: EventItem) {
  switch (cmd) {
    case 'export':
      await doExport(row.id)
      break
    case 'cancel':
      await handleCancel(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

async function handleCancel(event: EventItem) {
  try {
    await ElMessageBox.confirm(
      `确定要取消活动「${event.title}」吗？取消后所有已报名用户将收到通知，报名记录保留。`,
      '确认取消',
      { confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning' }
    )
    await cancelEvent(event.id)
    ElMessage.success('活动已取消')
    loadEvents()
  } catch {
    // 用户取消
  }
}

async function handleDelete(event: EventItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除活动「${event.title}」吗？此操作不可恢复！`,
      '确认删除',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
    )
    await deleteEvent(event.id)
    ElMessage.success('活动已删除')
    loadEvents()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.admin-events {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--c-text-primary, #18181B);
}

.subtitle {
  font-size: 14px;
  color: var(--c-text-secondary, #71717A);
  margin: 0;
}

.filter-bar {
  background: var(--c-bg-card, #FFFFFF);
  border: 1px solid var(--c-border, #E4E4E7);
  border-radius: var(--radius-md, 8px);
  padding: 12px 16px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-wrapper {
  background: var(--c-bg-card, #FFFFFF);
  border: 1px solid var(--c-border, #E4E4E7);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.event-link {
  color: var(--c-primary, #B45309);
  text-decoration: none;
  font-weight: 500;
}

.event-link:hover {
  text-decoration: underline;
}

.text-danger {
  color: #DC2626;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-row > * {
    width: 100% !important;
  }
}
</style>
