<template>
  <div class="profile-page">
    <!-- Hero Header -->
    <div class="profile-hero">
      <div class="hero-bg-pattern"></div>
      <div class="profile-header-content">
        <!-- Avatar Section -->
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerAvatarUpload">
            <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" class="avatar-img" alt="头像" />
            <div v-else class="avatar-placeholder">
              <span>{{ userStore.user?.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <div class="avatar-overlay">
              <el-icon :size="24"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarUpload"
            />
          </div>
          <div class="uploading-indicator" v-if="avatarUploading">
            <el-icon class="is-loading" :size="16"><Loading /></el-icon>
            <span>上传中…</span>
          </div>
        </div>
        <!-- User Info -->
        <div class="user-meta">
          <h1 class="username-display">{{ userStore.user?.username }}</h1>
          <p class="user-bio" v-if="userStore.user?.bio">{{ userStore.user.bio }}</p>
          <p class="user-bio placeholder-bio" v-else>这个人很懒，还没有写简介…</p>
          <div class="user-badges">
            <el-tag :type="userStore.isAdmin ? 'danger' : 'primary'" effect="dark" round size="small">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
            <span class="join-date">
              <el-icon><Calendar /></el-icon>
              加入于 {{ formatDate(userStore.user?.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Area -->
    <div class="profile-body">
      <!-- Sidebar: Edit Profile -->
      <div class="profile-sidebar">
        <el-card class="glass-effect edit-card">
          <template #header>
            <div class="card-header-row">
              <el-icon><Edit /></el-icon>
              <h3>编辑资料</h3>
            </div>
          </template>
          <el-form :model="profileForm" label-position="top" v-if="userStore.user">
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="your@email.com" class="custom-input" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下你自己..."
                maxlength="200"
                show-word-limit
                class="custom-textarea"
              />
            </el-form-item>
            <el-button type="primary" class="save-btn" @click="handleUpdateProfile" :loading="saving">
              保存修改
            </el-button>
          </el-form>
        </el-card>
      </div>

      <!-- Main: Tabs -->
      <div class="profile-main">
        <el-card class="glass-effect tabs-card">
          <div class="tabs-header">
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'registrations' }"
              @click="switchTab('registrations')"
            >
              <el-icon><Ticket /></el-icon>
              <span>我的报名</span>
              <el-badge :value="regCount" :max="99" v-if="regCount > 0" class="tab-badge" />
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'favorites' }"
              @click="switchTab('favorites')"
            >
              <el-icon><StarFilled /></el-icon>
              <span>我的收藏</span>
              <el-badge :value="favCount" :max="99" v-if="favCount > 0" class="tab-badge" />
            </button>
          </div>

          <div class="tab-content" v-loading="listLoading">
            <transition-group name="card-list" tag="div" class="event-grid" v-if="myEvents.length > 0">
              <EventCard
                v-for="(event, index) in myEvents"
                :key="event.id"
                :event="event"
                :style="{ animationDelay: `${index * 0.08}s` }"
                class="stagger-fade-in"
              />
            </transition-group>
            <el-empty v-else :description="activeTab === 'registrations' ? '暂无报名记录' : '暂无收藏'" :image-size="120">
              <template #description>
                <p class="empty-desc">{{ activeTab === 'registrations' ? '去活动大厅看看有什么感兴趣的吧！' : '收藏你喜欢的活动，方便随时查看' }}</p>
              </template>
              <el-button type="primary" class="explore-btn" @click="$router.push('/')">
                探索活动
              </el-button>
            </el-empty>
          </div>
        </el-card>
      </div>
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
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const validationMessage = validateImageFile(file)
  if (validationMessage) {
    ElMessage.error(validationMessage)
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
    /* error handled by interceptor */
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
  // 并行加载两个列表的计数
  loadRegistrations()
  getMyFavorites().then(res => { favCount.value = res.data.length })
})
</script>

<style scoped>
/* ========== Hero Section ========== */
.profile-hero {
  margin: -100px -20px 0;
  padding-top: 100px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  min-height: 320px;
  display: flex;
  align-items: flex-end;
}

.hero-bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(ellipse at 20% 50%, rgba(118, 75, 162, 0.25) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(99, 179, 237, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(236, 72, 153, 0.15) 0%, transparent 50%);
  animation: heroShift 12s ease-in-out infinite alternate;
}

@keyframes heroShift {
  0% { transform: scale(1) translateX(0); }
  100% { transform: scale(1.05) translateX(-2%); }
}

.profile-header-content {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 20px 48px;
  display: flex;
  align-items: flex-end;
  gap: 32px;
}

/* ========== Avatar ========== */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  position: relative;
  cursor: pointer;
  border: 4px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.avatar-wrapper:hover {
  border-color: var(--c-primary-light);
  transform: scale(1.05);
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
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-secondary-light) 100%);
  color: white;
  font-size: 48px;
  font-weight: 800;
  font-family: var(--font-heading);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  gap: 4px;
  font-size: 12px;
}

.uploading-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

/* ========== User Meta ========== */
.user-meta {
  flex: 1;
  padding-bottom: 4px;
}

.username-display {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  font-family: var(--font-heading);
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  margin-bottom: 8px;
}

.user-bio {
  color: rgba(255, 255, 255, 0.8);
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 16px;
  max-width: 500px;
}

.placeholder-bio {
  font-style: italic;
  color: rgba(255, 255, 255, 0.4);
}

.user-badges {
  display: flex;
  align-items: center;
  gap: 16px;
}

.join-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

/* ========== Body Layout ========== */
.profile-body {
  max-width: 1200px;
  margin: -24px auto 40px;
  position: relative;
  z-index: 10;
  display: flex;
  gap: 28px;
  align-items: flex-start;
  padding: 0 20px;
}

.profile-sidebar {
  width: 320px;
  position: sticky;
  top: 100px;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

/* ========== Edit Card ========== */
.edit-card {
  padding: 24px;
}

.card-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-heading);
}

.card-header-row h3 {
  font-size: 18px;
  margin: 0;
}

:deep(.custom-input .el-input__wrapper),
:deep(.custom-textarea .el-textarea__inner) {
  border-radius: var(--radius-md) !important;
  background: rgba(255, 255, 255, 0.6);
}

.save-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md) !important;
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-primary-light) 100%) !important;
  border: none !important;
  font-weight: 600;
  font-size: 15px;
}

/* ========== Tabs Card ========== */
.tabs-card {
  padding: 0;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.4);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 18px 16px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: var(--c-text-light);
  position: relative;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  color: var(--c-primary);
  background: rgba(118, 75, 162, 0.04);
}

.tab-btn.active {
  color: var(--c-primary);
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 3px;
  background: linear-gradient(90deg, var(--c-primary) 0%, var(--c-primary-light) 100%);
  border-radius: 3px 3px 0 0;
}

.tab-badge {
  margin-left: 4px;
}

.tab-content {
  padding: 24px;
  min-height: 300px;
}

/* ========== Event Grid ========== */
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.stagger-fade-in {
  animation: fadeInUp 0.5s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  0% { transform: translateY(20px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

.empty-desc {
  color: var(--c-text-light);
  margin-bottom: 16px;
}

.explore-btn {
  border-radius: var(--radius-md) !important;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-light)) !important;
  border: none !important;
}

/* ========== Transition ========== */
.card-list-enter-active,
.card-list-leave-active {
  transition: all 0.4s ease;
}
.card-list-enter-from,
.card-list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* ========== Responsive ========== */
@media (max-width: 900px) {
  .profile-body {
    flex-direction: column;
  }
  .profile-sidebar {
    width: 100%;
    position: static;
  }
  .profile-header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .username-display {
    font-size: 28px;
  }
  .user-badges {
    justify-content: center;
  }
  .user-bio {
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
