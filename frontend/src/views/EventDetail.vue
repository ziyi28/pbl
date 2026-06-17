<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="event">
      <!-- ═══ Hero ══════════════════════════════ -->
      <section class="hero-banner" :style="bannerStyle">
        <div class="hero-overlay">
          <div class="hero-content">
            <button class="back-link" @click="$router.back()">
              <el-icon :size="16"><ArrowLeft /></el-icon>
              <span>返回</span>
            </button>
            <h1 class="hero-title">{{ event.title }}</h1>
            <div class="hero-pills">
              <span class="hero-pill">{{ CategoryMap[event.category] }}</span>
              <span class="hero-pill">发起人：{{ event.creatorName }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 主体双栏 ══════════════════════════ -->
      <div class="detail-body">
        <!-- 左栏 -->
        <div class="main-col">
          <!-- 活动详情 -->
          <el-card class="content-card" shadow="never">
            <h3 class="card-heading">活动详情</h3>
            <p class="detail-text">{{ event.description }}</p>
          </el-card>

          <!-- 讨论区 -->
          <el-card class="content-card" shadow="never">
            <h3 class="card-heading">讨论区</h3>

            <!-- 发表评论 -->
            <div class="comment-form" v-if="userStore.isLoggedIn">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="发表你的看法…"
                maxlength="500"
                show-word-limit
                class="comment-textarea"
              />
              <div class="comment-form-actions">
                <button
                  class="comment-submit"
                  :disabled="!commentContent.trim() || commentSubmitting"
                  @click="handleComment"
                >
                  发送
                </button>
              </div>
            </div>
            <div class="comment-login-hint" v-else>
              请 <router-link to="/login">登录</router-link> 后参与讨论
            </div>

            <!-- 评论列表 -->
            <div class="comment-list" v-if="comments.length > 0">
              <div
                class="comment-item"
                v-for="comment in comments"
                :key="comment.id"
              >
                <div class="comment-avatar">
                  <img
                    v-if="comment.userAvatar"
                    :src="comment.userAvatar"
                    class="comment-avatar-img"
                    :alt="comment.username"
                  />
                  <span v-else>{{ comment.username.charAt(0).toUpperCase() }}</span>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-user">{{ comment.username }}</span>
                    <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
                    <button
                      v-if="userStore.isLoggedIn && (userStore.user?.id === comment.userId || userStore.isAdmin)"
                      class="comment-delete"
                      @click="handleDeleteComment(comment.id)"
                    >
                      删除
                    </button>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <button
                      v-if="userStore.isLoggedIn"
                      class="like-btn"
                      :class="{ liked: comment.isLiked }"
                      @click="handleToggleLike(comment)"
                      :disabled="likeLoading[comment.id]"
                    >
                      <el-icon :size="14"><StarFilled v-if="comment.isLiked" /><Star v-else /></el-icon>
                      {{ comment.likeCount || 0 }}
                    </button>
                    <span v-else class="like-static">
                      <el-icon :size="14"><Star /></el-icon>
                      {{ comment.likeCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空评论 -->
            <div class="comment-empty" v-else>
              <p>暂无评论，来说点什么吧</p>
            </div>

            <!-- 评论分页 -->
            <div class="comment-pagination" v-if="commentTotal > 20">
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

        <!-- 右栏：信息汇总卡片 -->
        <div class="side-col">
          <el-card class="info-card" shadow="never">
            <!-- 地点 -->
            <div class="info-item">
              <el-icon :size="18" class="info-icon"><Location /></el-icon>
              <div class="info-body">
                <span class="info-label">地点</span>
                <span class="info-value">{{ event.location }}</span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 时间 -->
            <div class="info-item">
              <el-icon :size="18" class="info-icon"><Clock /></el-icon>
              <div class="info-body">
                <span class="info-label">时间</span>
                <span class="info-value">
                  {{ formatDateTime(event.startTime) }}
                  <span class="info-value-sep">—</span>
                  {{ formatDateTime(event.endTime) }}
                </span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 截止报名 -->
            <div class="info-item">
              <el-icon :size="18" class="info-icon"><Calendar /></el-icon>
              <div class="info-body">
                <span class="info-label">截止报名</span>
                <span class="info-value">{{ formatDateTime(event.registrationDeadline) }}</span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 参与人数 + 进度条 -->
            <div class="info-item">
              <el-icon :size="18" class="info-icon"><User /></el-icon>
              <div class="info-body">
                <span class="info-label">参与人数</span>
                <span class="info-value number-value">
                  {{ event.currentParticipants }} / {{ event.maxParticipants }}
                </span>
                <div class="info-progress">
                  <div class="progress-track">
                    <div
                      class="progress-fill"
                      :style="{ width: progressPercent + '%' }"
                    />
                  </div>
                  <span class="progress-text">{{ progressPercent }}%</span>
                </div>
              </div>
            </div>

            <!-- 操作区 -->
            <div class="info-actions">
              <!-- 已登录 -->
              <template v-if="userStore.isLoggedIn">
                <template v-if="event.status === 'OPEN'">
                  <button
                    v-if="isRegistered"
                    class="action-btn action-outline"
                    @click="handleCancelReg"
                  >
                    取消报名
                  </button>
                  <button
                    v-else-if="isDeadlinePassed"
                    class="action-btn action-disabled"
                    disabled
                  >
                    报名已截止
                  </button>
                  <button
                    v-else-if="event.currentParticipants < event.maxParticipants"
                    class="action-btn action-primary"
                    @click="handleRegister"
                  >
                    立刻报名参加
                  </button>
                  <button
                    v-else
                    class="action-btn action-disabled"
                    disabled
                  >
                    名额已满
                  </button>
                </template>
                <template v-else>
                  <button class="action-btn action-disabled" disabled>
                    {{ StatusMap[event.status] }}
                  </button>
                </template>

                <!-- 收藏 -->
                <button
                  class="fav-btn"
                  :class="{ favorited: isFavorited }"
                  @click="handleFavoriteToggle"
                >
                  <el-icon :size="16"><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
                  <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
                </button>

                <!-- 管理员操作 -->
                <template v-if="userStore.isAdmin">
                  <div class="info-divider" />
                  <div class="admin-actions">
                    <button class="admin-btn" @click="$router.push(`/events/${event.id}/edit`)">
                      <el-icon :size="14"><Edit /></el-icon>
                      编辑活动
                    </button>
                    <button class="admin-btn admin-btn-danger" @click="handleDeleteEvent">
                      <el-icon :size="14"><Delete /></el-icon>
                      删除活动
                    </button>
                  </div>
                </template>
              </template>

              <!-- 未登录 -->
              <template v-else>
                <button
                  class="action-btn action-primary"
                  @click="$router.push({ path: '/login', query: { redirect: $route.fullPath } })"
                >
                  登录后即可报名
                </button>
              </template>
            </div>

            <!-- 参与人员 -->
            <template v-if="participants.length > 0">
              <div class="info-divider" />
              <div class="participants-section">
                <span class="participants-heading">参与人员 · {{ participants.length }} 人</span>
                <div class="participants-list">
                  <div
                    class="participant-row"
                    v-for="p in participants"
                    :key="p.userId"
                  >
                    <div class="participant-avatar-sm">
                      <img
                        v-if="p.avatar"
                        :src="p.avatar"
                        :alt="p.username"
                      />
                      <span v-else>{{ p.username.charAt(0).toUpperCase() }}</span>
                    </div>
                    <span class="participant-name-sm">{{ p.username }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-card>
        </div>
      </div>
    </template>

    <!-- 活动不存在 -->
    <div v-else class="not-found">
      <el-icon :size="48" class="not-found-icon"><Warning /></el-icon>
      <p class="not-found-text">活动不存在或已被删除</p>
      <button class="not-found-btn" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Star, StarFilled, Location, Clock, Calendar,
  User, ArrowLeft, Edit, Delete, Warning,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEvent, registerEvent, cancelRegistration,
  deleteEvent, getEventParticipants,
} from '../api/event'
import {
  getComments, createComment, deleteComment, toggleCommentLike,
} from '../api/comment'
import { addFavorite, removeFavorite } from '../api/favorite'
import { useUserStore } from '../stores/user'
import { CategoryMap, StatusMap } from '../types'
import type { EventItem, Comment, Participant } from '../types'

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
const commentSubmitting = ref(false)
const likeLoading = ref<Record<number, boolean>>({})

const isRegistered = ref(false)
const isFavorited = ref(false)
const participants = ref<Participant[]>([])

const isDeadlinePassed = computed(() => {
  if (!event.value?.registrationDeadline) return false
  return new Date(event.value.registrationDeadline) < new Date()
})

const progressPercent = computed(() => {
  if (!event.value) return 0
  const { currentParticipants, maxParticipants } = event.value
  if (maxParticipants === 0) return 0
  return Math.min(100, Math.round((currentParticipants / maxParticipants) * 100))
})

const bannerStyle = computed(() => {
  if (event.value?.coverImage) {
    return { backgroundImage: `url(${event.value.coverImage})` }
  }
  return {}
})

function formatDateTime(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    event.value = res.data
    isRegistered.value = res.data.isRegistered ?? false
    isFavorited.value = res.data.isFavorited ?? false
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  const res = await getComments(eventId, { page: commentPage.value, size: 20 })
  comments.value = res.data.records
  commentTotal.value = res.data.total
}

async function loadParticipants() {
  const res = await getEventParticipants(eventId)
  participants.value = res.data
}

async function handleRegister() {
  try {
    await registerEvent(eventId)
    ElMessage.success('报名成功')
    await loadEvent()
    await loadParticipants()
  } catch { /* interceptor handles */ }
}

async function handleCancelReg() {
  await ElMessageBox.confirm(
    '确定取消报名吗？取消后名额可能被其他人抢占。',
    '确认取消',
    { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' },
  )
  try {
    await cancelRegistration(eventId)
    ElMessage.success('已取消报名')
    await loadEvent()
    await loadParticipants()
  } catch { /* interceptor handles */ }
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
  } catch { /* interceptor handles */ }
}

async function handleDeleteEvent() {
  await ElMessageBox.confirm(
    '确定永久删除该活动吗？',
    '系统警告',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  try {
    await deleteEvent(eventId)
    ElMessage.success('活动删除成功')
    router.replace('/')
  } catch { /* interceptor handles */ }
}

async function handleComment() {
  if (!commentContent.value.trim() || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    await createComment(eventId, { content: commentContent.value })
    commentContent.value = ''
    ElMessage.success('评论成功')
    loadComments()
  } catch { /* interceptor handles */ }
  commentSubmitting.value = false
}

async function handleDeleteComment(id: number) {
  await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' })
  try {
    await deleteComment(id)
    ElMessage.success('评论已删除')
    loadComments()
  } catch { /* interceptor handles */ }
}

async function handleToggleLike(comment: Comment) {
  likeLoading.value[comment.id] = true
  try {
    await toggleCommentLike(comment.id)
    comment.isLiked = !comment.isLiked
    comment.likeCount = comment.isLiked
      ? (comment.likeCount || 0) + 1
      : Math.max(0, (comment.likeCount || 0) - 1)
  } catch { /* interceptor handles */ }
  likeLoading.value[comment.id] = false
}

onMounted(() => {
  loadEvent()
  loadComments()
  if (userStore.isLoggedIn) {
    loadParticipants()
  }
})
</script>

<style scoped>
/* ================================================
   Hero
   ================================================ */
.hero-banner {
  margin: -80px -20px 0;
  padding-top: 80px;
  height: 320px;
  background-color: #18181B;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  /* 有图片时加深蒙层，纯色背景时不额外叠加 */
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
}

.hero-content {
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px 36px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  font-family: var(--font-base);
  cursor: pointer;
  padding: 0;
  margin-bottom: 14px;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #FFFFFF;
}

.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.3;
  margin-bottom: 16px;
  letter-spacing: -0.2px;
}

/* 统一药丸标签 */
.hero-pills {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-pill {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.12);
  line-height: 1.5;
}

/* ================================================
   双栏主体
   ================================================ */
.detail-body {
  max-width: 960px;
  margin: -24px auto 40px;
  padding: 0 20px;
  position: relative;
  z-index: 10;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-col {
  width: 340px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

/* ================================================
   通用卡片
   ================================================ */
.content-card,
.info-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.content-card :deep(.el-card__body) {
  padding: 24px;
}

.info-card :deep(.el-card__body) {
  padding: 20px 22px 22px;
}

.card-heading {
  font-size: 17px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 16px;
}

/* ── 活动详情 ────────────────────────── */
.detail-text {
  font-size: 15px;
  color: var(--c-text-light);
  line-height: 1.85;
  white-space: pre-wrap;
}

/* ================================================
   讨论区
   ================================================ */
.comment-form {
  margin-bottom: 24px;
}

:deep(.comment-textarea .el-textarea__inner) {
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: none !important;
  background: #FAFAFA;
  font-size: 14px;
  color: var(--c-text);
}

:deep(.comment-textarea .el-textarea__inner:focus) {
  border-color: var(--c-primary) !important;
  box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.1) !important;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 20px;
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

.comment-submit:hover:not(:disabled) {
  background: #D97742;
}

.comment-submit:disabled {
  background: #D4D4D8;
  cursor: not-allowed;
}

.comment-login-hint {
  text-align: center;
  padding: 20px;
  color: var(--c-text-muted);
  font-size: 14px;
  margin-bottom: 24px;
}

.comment-login-hint a {
  color: var(--c-primary);
  font-weight: 500;
}

/* ── 评论列表 ────────────────────────── */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: #FAFAFA;
}

.comment-avatar {
  width: 38px;
  height: 38px;
  min-width: 38px;
  border-radius: 50%;
  background: #B45309;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  overflow: hidden;
}

.comment-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-user {
  font-weight: 600;
  font-size: 13px;
  color: var(--c-text);
}

.comment-time {
  font-size: 12px;
  color: var(--c-text-muted);
}

.comment-delete {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 12px;
  color: #D4D4D8;
  cursor: pointer;
  font-family: var(--font-base);
  padding: 2px 4px;
  transition: color 0.2s ease;
}

.comment-delete:hover {
  color: #EF4444;
}

.comment-text {
  font-size: 14px;
  color: var(--c-text);
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: 12px;
  color: var(--c-text-muted);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: var(--font-base);
  transition: color 0.2s ease;
}

.like-btn:hover:not(:disabled) {
  color: #B45309;
}

.like-btn.liked {
  color: #B45309;
}

.like-static {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.comment-empty {
  text-align: center;
  padding: 32px 0;
  color: var(--c-text-muted);
  font-size: 14px;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* ================================================
   右栏信息卡片
   ================================================ */
.info-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
}

.info-item:first-child {
  padding-top: 0;
}

.info-icon {
  color: var(--c-text-muted);
  margin-top: 2px;
  flex-shrink: 0;
}

.info-body {
  flex: 1;
  min-width: 0;
}

.info-label {
  display: block;
  font-size: 12px;
  color: var(--c-text-muted);
  margin-bottom: 3px;
}

.info-value {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: var(--c-text);
  line-height: 1.5;
}

.number-value {
  font-size: 17px;
  font-weight: 600;
  color: var(--c-text);
}

.info-value-sep {
  color: var(--c-text-muted);
  margin: 0 4px;
  font-weight: 400;
}

/* 分隔线 */
.info-divider {
  height: 1px;
  background: #F4F4F5;
  margin: 0;
}

/* 进度条 */
.info-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.progress-track {
  flex: 1;
  height: 6px;
  background: #F4F4F5;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #B45309;
  border-radius: 3px;
  transition: width 0.4s ease;
}

.progress-text {
  font-size: 12px;
  font-weight: 500;
  color: var(--c-text-muted);
  min-width: 32px;
  text-align: right;
}

/* ── 操作按钮区 ──────────────────────── */
.info-actions {
  padding-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

/* 主操作 —— 唯一的强视觉焦点 */
.action-primary {
  background: #B45309;
  color: #FFFFFF;
}

.action-primary:hover {
  background: #D97742;
}

/* 次要操作 */
.action-outline {
  background: transparent;
  color: var(--c-text);
  border: 1px solid var(--c-border);
}

.action-outline:hover {
  border-color: var(--c-text-muted);
}

/* 禁用态 */
.action-disabled {
  background: #F4F4F5;
  color: #A1A1AA;
  cursor: not-allowed;
}

/* 收藏按钮 */
.fav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 34px;
  padding: 0 14px;
  background: transparent;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--c-text-muted);
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.fav-btn:hover {
  border-color: var(--c-text-muted);
  color: var(--c-text);
}

.fav-btn.favorited {
  border-color: #B45309;
  color: #B45309;
}

/* 管理员操作 */
.admin-actions {
  display: flex;
  gap: 8px;
}

.admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--c-text-light);
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.admin-btn:hover {
  border-color: var(--c-text-muted);
}

.admin-btn-danger:hover {
  border-color: #FCA5A5;
  color: #EF4444;
}

/* ── 参与人员 ────────────────────────── */
.participants-section {
  padding-top: 14px;
}

.participants-heading {
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text-light);
  display: block;
  margin-bottom: 10px;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
}

.participant-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  border-radius: 6px;
  transition: background 0.15s ease;
}

.participant-row:hover {
  background: #FAFAFA;
}

.participant-avatar-sm {
  width: 28px;
  height: 28px;
  min-width: 28px;
  border-radius: 50%;
  background: #D4D4D8;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 11px;
  overflow: hidden;
}

.participant-avatar-sm img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.participant-name-sm {
  font-size: 13px;
  color: var(--c-text);
  font-weight: 400;
}

/* ================================================
   不存在
   ================================================ */
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  padding: 60px 20px;
  text-align: center;
}

.not-found-icon {
  color: #D4D4D8;
  margin-bottom: 16px;
}

.not-found-text {
  font-size: 15px;
  color: var(--c-text-light);
  margin-bottom: 20px;
}

.not-found-btn {
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

.not-found-btn:hover {
  background: #D97742;
}

/* ================================================
   响应式
   ================================================ */
@media (max-width: 900px) {
  .detail-body {
    flex-direction: column;
  }

  .side-col {
    width: 100%;
    position: static;
  }

  .hero-banner {
    height: 240px;
  }

  .hero-title {
    font-size: 24px;
  }
}
</style>
