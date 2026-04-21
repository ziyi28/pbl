<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="event">
      <!-- Immersive Header Banner -->
      <div class="hero-banner" :style="bannerStyle">
        <div class="hero-overlay">
          <div class="hero-content">
            <el-page-header @back="$router.back()" class="custom-page-header">
              <template #content>
                <el-tag :type="statusType" effect="dark" size="large" class="hero-status-tag">{{ StatusMap[event.status] }}</el-tag>
              </template>
            </el-page-header>
            <h1 class="hero-title">{{ event.title }}</h1>
            <div class="hero-tags">
              <el-tag effect="light" size="large" class="category-tag">{{ CategoryMap[event.category] }}</el-tag>
              <span class="hero-creator">发起人: {{ event.creatorName }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-content-wrapper">
        <div class="main-column">
          <!-- 活动详情 -->
          <el-card class="glass-effect detail-card">
            <div class="description">
              <h3>活动详情</h3>
              <p>{{ event.description }}</p>
            </div>
          </el-card>

          <!-- 评论区 -->
          <el-card class="glass-effect comment-card">
            <template #header>
              <h3 class="section-title">讨论区</h3>
            </template>

            <!-- 发表评论 -->
            <div class="comment-form" v-if="userStore.isLoggedIn">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="发表你的看法..."
                maxlength="500"
                show-word-limit
                class="custom-textarea"
              />
              <el-button type="primary" class="submit-comment-btn" @click="handleComment" :disabled="!commentContent.trim()">
                发 送
              </el-button>
            </div>
            
            <div class="comment-login-hint" v-else>
              请 <router-link to="/login">登录</router-link> 后参与讨论
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <div class="comment-item fade-in-up" v-for="comment in comments" :key="comment.id">
                <div class="comment-avatar">
                  <img v-if="comment.userAvatar" :src="comment.userAvatar" class="comment-avatar-img" :alt="comment.username" />
                  <span v-else>{{ comment.username.charAt(0).toUpperCase() }}</span>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-user">{{ comment.username }}</span>
                    <span class="comment-time">{{ comment.createdAt }}</span>
                    <el-button
                      v-if="userStore.isLoggedIn && (userStore.user?.id === comment.userId || userStore.isAdmin)"
                      type="danger" text size="small" class="delete-comment-btn"
                      @click="handleDeleteComment(comment.id)"
                    >
                      删除
                    </el-button>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                </div>
              </div>
            </div>

            <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发！" :image-size="80" />

            <div class="pagination" v-if="commentTotal > 20">
              <el-pagination
                v-model:current-page="commentPage"
                :page-size="20"
                :total="commentTotal"
                layout="prev, pager, next"
                @current-change="loadComments"
                background
              />
            </div>
          </el-card>
        </div>

        <!-- 侧边信息栏 -->
        <div class="side-column">
          <el-card class="glass-effect info-card">
            <div class="info-list">
              <div class="info-item">
                <div class="info-icon"><el-icon><Location /></el-icon></div>
                <div class="info-text">
                  <label>地点</label>
                  <span>{{ event.location }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon><Clock /></el-icon></div>
                <div class="info-text">
                  <label>时间</label>
                  <span>{{ event.startTime }} - <br>{{ event.endTime }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon><Calendar /></el-icon></div>
                <div class="info-text">
                  <label>截止报名</label>
                  <span>{{ event.registrationDeadline }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon><User /></el-icon></div>
                <div class="info-text">
                  <label>参与人数</label>
                  <div class="progress-wrapper">
                    <span>{{ event.currentParticipants }} / {{ event.maxParticipants }}</span>
                    <el-progress :percentage="Math.min(100, Math.round(event.currentParticipants / event.maxParticipants * 100))" :show-text="false" color="var(--c-primary)" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="actions" v-if="userStore.isLoggedIn">
              <template v-if="event.status === 'OPEN'">
                <el-button v-if="isRegistered" size="large" class="action-btn" @click="handleCancelReg">
                  取消报名
                </el-button>
                <el-button v-else-if="event.currentParticipants < event.maxParticipants" type="primary" size="large" class="action-btn primary-action" @click="handleRegister">
                  立刻报名参加
                </el-button>
                <el-button v-else disabled size="large" class="action-btn">
                  名额已满
                </el-button>
              </template>
              
              <div class="secondary-actions">
                <el-button size="large" :icon="isFavorited ? StarFilled : Star" circle class="favorite-btn" @click="handleFavoriteToggle" :type="isFavorited ? 'warning' : 'default'" />
              </div>
              
              <template v-if="userStore.isAdmin">
                <el-divider />
                <div class="admin-actions">
                  <el-button type="warning" plain class="action-btn" @click="$router.push(`/events/${event.id}/edit`)">编辑活动</el-button>
                  <el-button type="danger" plain class="action-btn" @click="handleDeleteEvent">删除活动</el-button>
                </div>
              </template>
            </div>
            <div class="actions-guest" v-else>
              <el-button type="primary" size="large" class="action-btn primary-action" @click="$router.push('/login')">
                登录后即可报名
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled, Location, Clock, Calendar, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvent, registerEvent, cancelRegistration, deleteEvent, getMyRegistrations } from '../api/event'
import { getComments, createComment, deleteComment } from '../api/comment'
import { addFavorite, removeFavorite, getMyFavorites } from '../api/favorite'
import { useUserStore } from '../stores/user'
import { CategoryMap, StatusMap } from '../types'
import type { EventItem, Comment } from '../types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const eventId = Number(route.params.id)

const event = ref<EventItem | null>(null)
const loading = ref(false)
const comments = ref<Comment[]>([])
const commentContent = ref('')
const commentPage = ref(1)
const commentTotal = ref(0)

const isRegistered = ref(false)
const isFavorited = ref(false)

const statusType = computed(() => {
  switch (event.value?.status) {
    case 'OPEN': return 'success'
    case 'ONGOING': return 'warning'
    case 'ENDED': return 'info'
    default: return 'info'
  }
})

const bannerStyle = computed(() => {
  if (event.value?.coverImage) {
    return { backgroundImage: `url(${event.value.coverImage})` }
  }
  return { background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
})

async function checkUserStatus() {
  if (!userStore.isLoggedIn) return
  try {
    const [regRes, favRes] = await Promise.all([
      getMyRegistrations(),
      getMyFavorites()
    ])
    isRegistered.value = regRes.data.some(e => e.id === eventId)
    isFavorited.value = favRes.data.some(e => e.id === eventId)
  } catch (error) {
    console.error('Failed to check user status', error)
  }
}

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    event.value = res.data
    await checkUserStatus()
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  const res = await getComments(eventId, { page: commentPage.value, size: 20 })
  comments.value = res.data.records
  commentTotal.value = res.data.total
}

async function handleRegister() {
  try {
    await registerEvent(eventId)
    ElMessage.success('报名成功')
    await loadEvent()
  } catch { /* error handled by interceptor */ }
}

async function handleCancelReg() {
  try {
    await cancelRegistration(eventId)
    ElMessage.success('已取消报名')
    await loadEvent()
  } catch { /* error handled by interceptor */ }
}

async function handleFavoriteToggle() {
  try {
    if (isFavorited.value) {
      await removeFavorite(eventId)
      ElMessage.success('已取消收藏')
      isFavorited.value = false
    } else {
      await addFavorite(eventId)
      ElMessage.success('收藏成功')
      isFavorited.value = true
    }
  } catch { /* error handled by interceptor */ }
}

async function handleDeleteEvent() {
  await ElMessageBox.confirm('确定永久删除该活动吗？', '系统警告', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  try {
    await deleteEvent(eventId)
    ElMessage.success('活动删除成功')
    router.replace('/')
  } catch { /* error handled by interceptor */ }
}

async function handleComment() {
  if (!commentContent.value.trim()) return
  try {
    await createComment(eventId, { content: commentContent.value })
    commentContent.value = ''
    ElMessage.success('评论成功')
    loadComments()
  } catch { /* error handled by interceptor */ }
}

async function handleDeleteComment(id: number) {
  await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' })
  try {
    await deleteComment(id)
    ElMessage.success('评论已删除')
    loadComments()
  } catch { /* error handled by interceptor */ }
}

onMounted(() => {
  loadEvent()
  loadComments()
})
</script>

<style scoped>
.detail-page {
  width: 100%;
}

.hero-banner {
  margin: -100px -20px 0;
  padding-top: 100px;
  height: 480px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, rgba(33,53,71,0.9) 100%);
  display: flex;
  align-items: flex-end;
  padding-bottom: 60px;
}

.hero-content {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
}

:deep(.custom-page-header .el-page-header__left) {
  color: #fff;
  margin-bottom: 20px;
}

:deep(.custom-page-header .el-page-header__content) {
  color: #fff;
}

:deep(.custom-page-header .el-page-header__back:hover) {
  color: var(--c-secondary);
}

.hero-title {
  font-size: 48px;
  color: #fff;
  margin-bottom: 24px;
  text-shadow: 0 4px 12px rgba(0,0,0,0.3);
  line-height: 1.2;
}

.hero-tags {
  display: flex;
  align-items: center;
  gap: 16px;
}

.category-tag {
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
}

.hero-creator {
  color: rgba(255,255,255,0.9);
  font-size: 15px;
  background: rgba(255,255,255,0.1);
  padding: 4px 12px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

.detail-content-wrapper {
  max-width: 1200px;
  margin: -40px auto 40px;
  position: relative;
  z-index: 10;
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.main-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.side-column {
  width: 360px;
  position: sticky;
  top: 100px;
}

.detail-card {
  padding: 32px;
}

.section-title {
  font-size: 20px;
  font-family: var(--font-heading);
}

.description p {
  color: var(--c-text);
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 16px;
}

.info-card {
  padding: 24px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  gap: 16px;
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(118, 75, 162, 0.1);
  color: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.info-text {
  flex: 1;
}

.info-text label {
  display: block;
  font-size: 13px;
  color: var(--c-text-light);
  margin-bottom: 4px;
}

.info-text span {
  font-weight: 500;
  color: var(--c-text);
  display: block;
  line-height: 1.4;
}

.progress-wrapper {
  margin-top: 6px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: var(--radius-md) !important;
}

.primary-action {
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-primary-light) 100%) !important;
  border: none !important;
  color: white;
}

.secondary-actions {
  display: flex;
  gap: 12px;
}

.favorite-btn {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md) !important;
  margin-left: 0;
}

.admin-actions {
  display: flex;
  gap: 12px;
}

/* Comments section */
.comment-card {
  padding: 24px;
}

:deep(.custom-textarea .el-textarea__inner) {
  border-radius: var(--radius-md) !important;
  background: rgba(255, 255, 255, 0.5);
}

.submit-comment-btn {
  margin-top: 16px;
  float: right;
  border-radius: var(--radius-sm) !important;
}

.comment-login-hint {
  text-align: center;
  padding: 20px;
  background: rgba(0,0,0,0.02);
  border-radius: var(--radius-md);
  color: var(--c-text-light);
}

.comment-login-hint a {
  font-weight: 600;
}

.comment-list {
  margin-top: 50px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.4);
}

.comment-avatar {
  width: 44px;
  height: 44px;
  min-width: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--c-secondary-light) 0%, var(--c-primary-light) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  overflow: hidden;
}

.comment-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 600;
  color: var(--c-primary-dark);
}

.comment-time {
  font-size: 12px;
  color: var(--c-text-light);
}

.delete-comment-btn {
  margin-left: auto;
}

.comment-content {
  color: var(--c-text);
  line-height: 1.6;
}

@media (max-width: 900px) {
  .detail-content-wrapper {
    flex-direction: column;
  }
  .side-column {
    width: 100%;
    position: static;
  }
  .hero-title {
    font-size: 32px;
  }
}
</style>
