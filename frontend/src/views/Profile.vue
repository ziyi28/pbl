<template>
  <div class="profile-page">
    <!-- ── 用户信息区 ──────────────────── -->
    <section class="profile-hero">
      <div class="hero-content">
        <!-- 头像 -->
        <div class="avatar-section" @click="triggerAvatarUpload">
          <div class="avatar-wrapper">
            <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" class="avatar-img" alt="头像" />
            <div v-else class="avatar-placeholder">
              <span>{{ userStore.user?.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <div class="avatar-overlay">
              <el-icon :size="20"><Camera /></el-icon>
            </div>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarUpload"
          />
        </div>

        <!-- 用户元信息 -->
        <div class="user-meta">
          <h1 class="username-display">{{ userStore.user?.username }}</h1>
          <div class="meta-row">
            <span class="role-badge" :class="{ 'is-admin': userStore.isAdmin }">
              {{ userStore.isAdmin ? '管理员' : '用户' }}
            </span>
            <span class="join-date">
              <el-icon :size="14"><Calendar /></el-icon>
              加入于 {{ formatJoinDate(userStore.user?.createdAt) }}
            </span>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-num">{{ stats.registrations }}</span>
            <span class="stat-label">报名</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.upcoming }}</span>
            <span class="stat-label">即将开始</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.favorites }}</span>
            <span class="stat-label">收藏</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ── 主体双栏 ────────────────────── -->
    <div class="profile-body">
      <!-- 左侧：编辑资料 + 管理入口 -->
      <aside class="profile-sidebar">
        <el-card class="side-card" shadow="never">
          <template #header>
            <div class="card-header-row">
              <el-icon :size="15"><Edit /></el-icon>
              <span>编辑资料</span>
            </div>
          </template>
          <el-form :model="profileForm" label-position="top" v-if="userStore.user">
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="your@email.com" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下你自己…"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            <button class="save-btn" @click="handleUpdateProfile" :disabled="saving">
              {{ saving ? '保存中…' : '保存修改' }}
            </button>
          </el-form>
        </el-card>

        <!-- 管理员入口 -->
        <el-card v-if="userStore.isAdmin" class="side-card admin-panel" shadow="never">
          <template #header>
            <div class="card-header-row">
              <el-icon :size="15"><Setting /></el-icon>
              <span>管理控制台</span>
            </div>
          </template>
          <p class="admin-desc">你拥有管理员权限，可以创建和管理所有活动。</p>
          <div class="admin-links">
            <button class="admin-link-btn admin-link-primary" @click="$router.push('/events/create')">
              <el-icon :size="14"><Plus /></el-icon>
              发布新活动
            </button>
            <button class="admin-link-btn" @click="$router.push('/')">
              <el-icon :size="14"><List /></el-icon>
              管理活动大厅
            </button>
          </div>
        </el-card>
      </aside>

      <!-- 右侧：报名 / 收藏列表 -->
      <section class="profile-main">
        <el-card class="tabs-card" shadow="never">
          <!-- 胶囊标签 -->
          <div class="tabs-header">
            <div class="pill-group">
              <button
                class="pill-tab"
                :class="{ active: activeTab === 'registrations' }"
                @click="switchTab('registrations')"
              >
                <span>我的报名</span>
                <span v-if="regCount > 0" class="pill-count">{{ regCount }}</span>
              </button>
              <button
                class="pill-tab"
                :class="{ active: activeTab === 'favorites' }"
                @click="switchTab('favorites')"
              >
                <span>我的收藏</span>
                <span v-if="favCount > 0" class="pill-count">{{ favCount }}</span>
              </button>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="tab-content" v-loading="listLoading">
            <template v-if="myEvents.length > 0">
              <div
                class="list-item"
                v-for="event in myEvents"
                :key="event.id"
                @click="$router.push(`/events/${event.id}`)"
              >
                <!-- 封面缩略图 -->
                <div class="item-cover" :style="{ backgroundColor: getCategoryBg(event.category) }">
                  <img v-if="event.coverImage" :src="event.coverImage" :alt="event.title" />
                  <el-icon v-else :size="22" class="item-cover-icon">
                    <component :is="getCategoryIcon(event.category)" />
                  </el-icon>
                </div>

                <!-- 信息 -->
                <div class="item-body">
                  <div class="item-top">
                    <span class="item-status" :class="getStatusClass(event)">
                      {{ getStatusLabel(event) }}
                    </span>
                    <span class="item-category">{{ CategoryLabels[event.category] || event.category }}</span>
                  </div>
                  <h4 class="item-title">{{ event.title }}</h4>
                  <div class="item-meta">
                    <span class="item-meta-text">
                      <el-icon :size="13"><Clock /></el-icon>
                      {{ formatDate(event.startTime) }}
                    </span>
                    <span class="item-meta-text">
                      <el-icon :size="13"><Location /></el-icon>
                      {{ event.location }}
                    </span>
                  </div>
                  <div class="item-info" v-if="activeTab === 'registrations'">
                    <span class="item-people">{{ event.currentParticipants }}/{{ event.maxParticipants }} 人</span>
                  </div>
                </div>

                <!-- 快速操作 -->
                <div class="item-actions" @click.stop>
                  <template v-if="activeTab === 'registrations'">
                    <button
                      v-if="canRegister(event) || (event.isRegistered && event.status === 'OPEN')"
                      class="item-action-btn item-action-danger"
                      @click="handleQuickCancelReg(event)"
                    >
                      取消报名
                    </button>
                  </template>
                  <template v-else>
                    <button
                      class="item-action-btn"
                      @click="handleQuickUnfavorite(event)"
                    >
                      取消收藏
                    </button>
                  </template>
                </div>
              </div>
            </template>

            <!-- 空状态 -->
            <div class="empty-state" v-else>
              <el-icon :size="40" class="empty-icon">
                <Ticket v-if="activeTab === 'registrations'" />
                <StarFilled v-else />
              </el-icon>
              <p class="empty-text">
                {{ activeTab === 'registrations' ? '还没有报名任何活动' : '还没有收藏任何活动' }}
              </p>
              <p class="empty-hint">
                {{ activeTab === 'registrations' ? '去活动大厅发现感兴趣的活动吧' : '收藏喜欢的活动，方便随时查看' }}
              </p>
              <button class="empty-action" @click="$router.push('/')">探索活动</button>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Edit, StarFilled, Ticket, Camera, Setting,
  Plus, List, Clock, Location,
  Microphone, TrophyBase, UserFilled, MoreFilled,
} from '@element-plus/icons-vue'
import { updateCurrentUser } from '../api/auth'
import { getMyRegistrations, cancelRegistration } from '../api/event'
import { getMyFavorites, removeFavorite } from '../api/favorite'
import { uploadFile, validateImageFile } from '../api/upload'
import { useUserStore } from '../stores/user'
import {
  canRegister, getStatusLabel, getStatusClass,
  getCategoryBg, formatDate, CategoryLabels,
} from '../utils/eventUtils'
import type { EventItem, EventCategory } from '../types'

const userStore = useUserStore()

const profileForm = reactive({
  email: '',
  bio: '',
})

const activeTab = ref('registrations')
const myEvents = ref<EventItem[]>([])
const listLoading = ref(false)
const saving = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)
const regCount = ref(0)
const favCount = ref(0)

const stats = reactive({
  registrations: 0,
  upcoming: 0,
  favorites: 0,
})

function getCategoryIcon(category: EventCategory) {
  const map: Record<EventCategory, any> = {
    LECTURE: Microphone,
    SPORTS: TrophyBase,
    CLUB: UserFilled,
    VOLUNTEER: StarFilled,
    OTHER: MoreFilled,
  }
  return map[category] ?? MoreFilled
}

function formatJoinDate(dateStr?: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const msg = validateImageFile(file)
  if (msg) {
    ElMessage.error(msg)
    target.value = ''
    return
  }

  try {
    const res = await uploadFile(file)
    await updateCurrentUser({ avatar: res.data })
    await userStore.fetchUser()
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    target.value = ''
  }
}

async function handleUpdateProfile() {
  saving.value = true
  try {
    await updateCurrentUser(profileForm)
    ElMessage.success('信息已更新')
    await userStore.fetchUser()
  } catch {
    /* interceptor handles */
  } finally {
    saving.value = false
  }
}

async function loadRegistrations() {
  listLoading.value = true
  try {
    const res = await getMyRegistrations()
    myEvents.value = res.data
    regCount.value = res.data.length
    stats.registrations = res.data.length
    stats.upcoming = res.data.filter(
      (e: EventItem) => e.status === 'OPEN' && new Date(e.startTime) > new Date()
    ).length
  } finally {
    listLoading.value = false
  }
}

async function loadFavorites() {
  listLoading.value = true
  try {
    const res = await getMyFavorites()
    myEvents.value = res.data
    favCount.value = res.data.length
    stats.favorites = res.data.length
  } finally {
    listLoading.value = false
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
  if (tab === 'registrations') loadRegistrations()
  else loadFavorites()
}

/** 快速取消报名 */
async function handleQuickCancelReg(event: EventItem) {
  try {
    await ElMessageBox.confirm('确定取消报名吗？', '确认取消', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
    })
  } catch {
    return
  }
  try {
    await cancelRegistration(event.id)
    ElMessage.success('已取消报名')
    loadRegistrations()
  } catch {
    /* interceptor handles */
  }
}

/** 快速取消收藏 */
async function handleQuickUnfavorite(event: EventItem) {
  try {
    await removeFavorite(event.id)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch {
    /* interceptor handles */
  }
}

onMounted(async () => {
  await userStore.fetchUser()
  if (userStore.user) {
    profileForm.email = userStore.user.email || ''
    profileForm.bio = userStore.user.bio || ''
  }
  loadRegistrations()
  getMyFavorites().then((res) => {
    favCount.value = res.data.length
    stats.favorites = res.data.length
  })
})
</script>

<style scoped>
/* ════════════════════════════════════════
   Hero
   ════════════════════════════════════════ */
.profile-hero {
  margin: -80px -20px 0;
  padding-top: 80px;
  background: #18181B;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.hero-content {
  max-width: 960px;
  margin: 0 auto;
  padding: 28px 20px 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

/* ── 头像 ────────────────────────────── */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  position: relative;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.12);
  overflow: hidden;
  transition: border-color 0.2s ease;
}

.avatar-wrapper:hover {
  border-color: #B45309;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #B45309;
  color: #FFFFFF;
  font-size: 32px;
  font-weight: 700;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: #FFFFFF;
}

/* ── 用户元信息 ──────────────────────── */
.user-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username-display {
  font-size: 24px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
  letter-spacing: -0.2px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.role-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.1);
  color: #A1A1AA;
}

.role-badge.is-admin {
  background: #FFF7ED;
  color: #B45309;
}

.join-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #A1A1AA;
}

/* ── 统计卡片 ────────────────────────── */
.stats-row {
  display: flex;
  gap: 20px;
  margin-left: auto;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #FFFFFF;
}

.stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

/* ════════════════════════════════════════
   Body
   ════════════════════════════════════════ */
.profile-body {
  max-width: 960px;
  margin: 24px auto 40px;
  padding: 0 20px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

/* ── 侧栏卡片 ────────────────────────── */
.side-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.side-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--c-border-light);
}

.side-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.save-btn {
  width: 100%;
  height: 36px;
  border-radius: var(--radius-sm) !important;
  background: #B45309 !important;
  border: none !important;
  color: #FFFFFF !important;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  font-family: var(--font-base);
  transition: background 0.2s ease;
}

.save-btn:hover:not(:disabled) {
  background: #D97742 !important;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 管理员链接 */
.admin-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-link-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  background: transparent;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--c-text-light);
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.admin-link-btn:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.admin-link-primary {
  background: #B45309;
  color: #FFFFFF;
  border-color: #B45309;
}

.admin-link-primary:hover {
  background: #D97742;
  border-color: #D97742;
  color: #FFFFFF;
}

.admin-desc {
  font-size: 12px;
  color: var(--c-text-muted);
  line-height: 1.6;
  margin-bottom: 12px;
  padding: 0 2px;
}

/* ════════════════════════════════════════
   选项卡卡片
   ════════════════════════════════════════ */
.tabs-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.tabs-card :deep(.el-card__body) {
  padding: 0;
}

.tabs-header {
  display: flex;
  justify-content: center;
  padding: 14px 20px 0;
}

.pill-group {
  display: inline-flex;
  background: #F4F4F5;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}

.pill-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #71717A;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-family: var(--font-base);
  white-space: nowrap;
}

.pill-tab:hover {
  color: #52525B;
}

.pill-tab.active {
  background: #18181B;
  color: #FFFFFF;
}

.pill-count {
  font-size: 11px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.15);
  color: inherit;
  padding: 1px 6px;
  border-radius: 8px;
}

.pill-tab:not(.active) .pill-count {
  background: #E4E4E7;
  color: #71717A;
}

/* ── 列表内容 ────────────────────────── */
.tab-content {
  padding: 16px 20px 20px;
  min-height: 300px;
}

/* 列表项（卡片行） */
.list-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s ease;
  border: 1px solid transparent;
}

.list-item:hover {
  background: #FAFAFA;
  border-color: var(--c-border-light);
}

.list-item + .list-item {
  border-top: 1px solid var(--c-border-light);
}

.item-cover {
  width: 64px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-cover-icon {
  opacity: 0.5;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.item-status {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 3px;
}

.item-status.status-open {
  background: #FFF7ED;
  color: #B45309;
}

.item-status.status-ongoing {
  background: #ECFDF5;
  color: #059669;
}

.item-status.status-ended {
  background: #FAFAFA;
  color: #A1A1AA;
}

.item-status.status-closed {
  background: #F4F4F5;
  color: #71717A;
}

.item-status.status-cancelled {
  background: #FEF2F2;
  color: #DC2626;
}

.item-category {
  font-size: 11px;
  color: var(--c-text-muted);
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-meta-text {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.item-info {
  margin-top: 4px;
}

.item-people {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* 快速操作按钮 */
.item-actions {
  flex-shrink: 0;
}

.item-action-btn {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--c-text-light);
  background: transparent;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.item-action-btn:hover {
  border-color: var(--c-text-muted);
  color: var(--c-text);
}

.item-action-danger:hover {
  border-color: #FCA5A5;
  color: #EF4444;
}

/* ── 空状态 ──────────────────────────── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  text-align: center;
}

.empty-icon {
  color: #D4D4D8;
  margin-bottom: 14px;
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: #71717A;
  margin-bottom: 4px;
}

.empty-hint {
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

/* ════════════════════════════════════════
   响应式
   ════════════════════════════════════════ */
@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 24px 20px 28px;
  }

  .meta-row {
    justify-content: center;
  }

  .username-display {
    font-size: 20px;
  }

  .stats-row {
    margin-left: 0;
  }

  .profile-body {
    flex-direction: column;
    padding: 0 16px;
  }

  .profile-sidebar {
    width: 100%;
  }

  /* 移动端列表项改为纵向布局，操作按钮放到底部 */
  .list-item {
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 10px;
  }

  .item-body {
    flex-basis: calc(100% - 76px);
  }

  .item-actions {
    flex-basis: 100%;
    display: flex;
    justify-content: flex-end;
    padding-top: 4px;
  }

  .item-action-btn {
    font-size: 11px;
    padding: 4px 10px;
  }
}
</style>
