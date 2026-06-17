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
          <span class="uploading-hint" v-if="avatarUploading">
            <el-icon class="is-loading" :size="14"><Loading /></el-icon>
            上传中…
          </span>
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
              加入于 {{ formatDate(userStore.user?.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- ── 主体双栏 ────────────────────── -->
    <div class="profile-body">
      <!-- 左侧：编辑资料 -->
      <aside class="profile-sidebar">
        <el-card class="edit-card" shadow="never">
          <template #header>
            <div class="card-header-row">
              <el-icon :size="16"><Edit /></el-icon>
              <span>编辑资料</span>
            </div>
          </template>
          <el-form :model="profileForm" label-position="top" v-if="userStore.user">
            <el-form-item label="邮箱">
              <el-input
                v-model="profileForm.email"
                placeholder="your@email.com"
                class="profile-input"
              />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="5"
                placeholder="介绍一下你自己…"
                maxlength="200"
                show-word-limit
                class="profile-textarea"
              />
            </el-form-item>
            <el-button class="save-btn" @click="handleUpdateProfile" :loading="saving">
              保存修改
            </el-button>
          </el-form>
        </el-card>
      </aside>

      <!-- 右侧：报名 / 收藏 -->
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
                <el-icon :size="15"><Ticket /></el-icon>
                <span>我的报名</span>
                <span v-if="regCount > 0" class="pill-count">{{ regCount }}</span>
              </button>
              <button
                class="pill-tab"
                :class="{ active: activeTab === 'favorites' }"
                @click="switchTab('favorites')"
              >
                <el-icon :size="15"><StarFilled /></el-icon>
                <span>我的收藏</span>
                <span v-if="favCount > 0" class="pill-count">{{ favCount }}</span>
              </button>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="tab-content" v-loading="listLoading">
            <transition-group
              name="card-list"
              tag="div"
              class="event-grid"
              v-if="myEvents.length > 0"
            >
              <EventCard
                v-for="(event, index) in myEvents"
                :key="event.id"
                :event="event"
                :style="{ animationDelay: `${index * 0.06}s` }"
                class="stagger-fade-in"
              />
            </transition-group>

            <!-- 空状态 —— 线性图标 + 引导 -->
            <div class="empty-state" v-else>
              <el-icon :size="44" class="empty-icon"><Calendar /></el-icon>
              <p class="empty-text">
                {{ activeTab === 'registrations' ? '还没有报名任何活动' : '还没有收藏任何活动' }}
              </p>
              <p class="empty-hint">
                {{ activeTab === 'registrations'
                    ? '去活动大厅发现感兴趣的活动吧'
                    : '收藏喜欢的活动，方便随时查看' }}
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
import { ElMessage } from 'element-plus'
import { Calendar, Edit, StarFilled, Ticket, Camera, Loading } from '@element-plus/icons-vue'
import { updateCurrentUser } from '../api/auth'
import { getMyRegistrations } from '../api/event'
import { getMyFavorites } from '../api/favorite'
import { uploadFile, validateImageFile } from '../api/upload'
import { useUserStore } from '../stores/user'
import EventCard from '../components/EventCard.vue'
import type { EventItem } from '../types'

const userStore = useUserStore()

const profileForm = reactive({
  email: '',
  bio: '',
})

const activeTab = ref('registrations')
const myEvents = ref<EventItem[]>([])
const listLoading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)
const regCount = ref(0)
const favCount = ref(0)

function formatDate(dateStr?: string) {
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
    if (target) target.value = ''
    return
  }

  avatarUploading.value = true
  try {
    const res = await uploadFile(file)
    await updateCurrentUser({ avatar: res.data })
    await userStore.fetchUser()
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    avatarUploading.value = false
    if (target) target.value = ''
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
  } finally {
    listLoading.value = false
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
  if (tab === 'registrations') loadRegistrations()
  else loadFavorites()
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
  })
})
</script>

<style scoped>
/* ================================================
   Hero —— 深色纯色背景
   ================================================ */
.profile-hero {
  margin: -80px -20px 0;
  padding-top: 80px;
  background: #18181B;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.hero-content {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 20px 36px;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* ── 头像 ────────────────────────────── */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.avatar-wrapper {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  position: relative;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.12);
  overflow: hidden;
  transition: border-color 0.2s ease;
}

.avatar-wrapper:hover {
  border-color: var(--c-primary);
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
  font-size: 36px;
  font-weight: 700;
  font-family: var(--font-heading);
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

.uploading-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* ── 用户元信息 ──────────────────────── */
.user-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.username-display {
  font-size: 26px;
  font-weight: 700;
  color: #FFFFFF;
  font-family: var(--font-heading);
  letter-spacing: -0.2px;
  line-height: 1.2;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

/* 角色徽章 —— 小药丸 */
.role-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.1);
  color: #A1A1AA;
}

.role-badge.is-admin {
  background: #FFF7ED;
  color: #B45309;
}

/* 加入时间 */
.join-date {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #A1A1AA;
}

/* ================================================
   Body —— 双栏布局
   ================================================ */
.profile-body {
  max-width: 960px;
  margin: 24px auto 40px;
  padding: 0 20px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-sidebar {
  width: 230px;
  flex-shrink: 0;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

/* ================================================
   左侧编辑资料卡片
   ================================================ */
.edit-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.edit-card :deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom: 1px solid var(--c-border-light);
}

.edit-card :deep(.el-card__body) {
  padding: 18px;
}

.card-header-row {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

/* 统一输入框样式 */
:deep(.profile-input .el-input__wrapper),
:deep(.profile-textarea .el-textarea__inner) {
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: none !important;
  background: #FAFAFA;
  font-size: 13px;
  color: var(--c-text);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.profile-input .el-input__wrapper.is-focus),
:deep(.profile-textarea .el-textarea__inner:focus) {
  border-color: var(--c-primary) !important;
  box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.1) !important;
}

:deep(.profile-input .el-input__wrapper .el-input__inner::placeholder),
:deep(.profile-textarea .el-textarea__inner::placeholder) {
  color: #C4C4C8;
}

:deep(.profile-textarea .el-textarea__inner) {
  resize: none;
  line-height: 1.6;
}

/* 保存按钮 —— 强调色 */
.save-btn {
  width: 100%;
  height: 38px;
  border-radius: var(--radius-sm) !important;
  background: #B45309 !important;
  border: none !important;
  color: #FFFFFF !important;
  font-weight: 600;
  font-size: 13px;
  margin-top: 4px;
  transition: background 0.2s ease;
}

.save-btn:hover {
  background: #D97742 !important;
}

/* ================================================
   右侧选项卡卡片
   ================================================ */
.tabs-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.tabs-card :deep(.el-card__body) {
  padding: 0;
}

/* ── 胶囊标签组 ──────────────────────── */
.tabs-header {
  display: flex;
  justify-content: center;
  padding: 16px 20px 0;
  border-bottom: none;
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
  padding: 7px 18px;
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

.pill-tab .el-icon {
  color: #A1A1AA;
  transition: color 0.2s ease;
}

.pill-tab:hover {
  color: #52525B;
}

.pill-tab.active {
  background: #18181B;
  color: #FFFFFF;
}

.pill-tab.active .el-icon {
  color: #FFFFFF;
}

.pill-count {
  font-size: 11px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.15);
  color: inherit;
  padding: 1px 6px;
  border-radius: 8px;
  line-height: 1.5;
}

.pill-tab:not(.active) .pill-count {
  background: #E4E4E7;
  color: #71717A;
}

/* ── 内容区 ──────────────────────────── */
.tab-content {
  padding: 20px 24px 24px;
  min-height: 360px;
}

/* ── 活动卡片网格 ────────────────────── */
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.stagger-fade-in {
  animation: fadeInUp 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  0%   { transform: translateY(12px); opacity: 0; }
  100% { transform: translateY(0);    opacity: 1; }
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
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: #71717A;
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 13px;
  color: #A1A1AA;
  margin-bottom: 20px;
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

/* ── 列表过渡 ────────────────────────── */
.card-list-enter-active,
.card-list-leave-active {
  transition: all 0.3s ease;
}
.card-list-enter-from,
.card-list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ================================================
   响应式
   ================================================ */
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
    font-size: 22px;
  }

  .profile-body {
    flex-direction: column;
    padding: 0 16px;
  }

  .profile-sidebar {
    width: 100%;
  }

  .event-grid {
    grid-template-columns: 1fr;
  }
}
</style>
