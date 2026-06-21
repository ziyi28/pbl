<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="event">
      <!-- ═══ 顶部信息栏 ═══════════════════ -->
      <section class="detail-hero">
        <div class="hero-body">
          <button class="back-link" @click="$router.back()">
            <el-icon :size="16"><ArrowLeft /></el-icon>
            <span>返回</span>
          </button>
          <h1 class="hero-title">{{ event.title }}</h1>
          <div class="hero-meta">
            <span class="hero-tag">{{ CategoryLabels[event.category] || event.category }}</span>
            <span class="hero-tag hero-tag-status" :class="statusClass">
              {{ statusLabel }}
            </span>
            <span class="hero-creator">发起人：{{ event.creatorName }}</span>
          </div>
        </div>
      </section>

      <!-- ═══ 主体双栏 ═══════════════════════ -->
      <div class="detail-body">
        <!-- 左栏：封面 + 正文 + 评论区 -->
        <div class="main-col">
          <!-- 封面图 -->
          <div v-if="event.coverImage" class="cover-section">
            <img :src="event.coverImage" :alt="event.title" class="cover-image" />
          </div>

          <!-- 活动详情 -->
          <el-card class="content-card" shadow="never">
            <h3 class="card-heading">活动详情</h3>
            <p class="detail-text">{{ event.description }}</p>
          </el-card>

          <!-- 讨论区 -->
          <el-card class="content-card" shadow="never">
            <h3 class="card-heading">讨论区{{ comments.length > 0 ? ` · ${commentTotal}` : '' }}</h3>

            <!-- 评论输入 / 提示 -->
            <div class="comment-input-area">
              <!-- 未登录 -->
              <div v-if="!userStore.isLoggedIn" class="comment-hint">
                <el-icon :size="16"><ChatLineSquare /></el-icon>
                <span>请 <router-link to="/login">登录</router-link> 后参与讨论</span>
              </div>

              <!-- 已登录但未报名 -->
              <div v-else-if="!event.isRegistered && event.status !== 'ENDED'" class="comment-hint">
                <el-icon :size="16"><ChatLineSquare /></el-icon>
                <span>报名活动后才能参与讨论</span>
              </div>

              <!-- 活动已结束 -->
              <div v-else-if="event.status === 'ENDED'" class="comment-hint">
                <el-icon :size="16"><ChatLineSquare /></el-icon>
                <span>活动已结束，讨论区已关闭</span>
              </div>

              <!-- 已报名，可评论 -->
              <div v-else class="comment-form">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="发表你的看法…"
                  maxlength="500"
                  show-word-limit
                />
                <div class="comment-form-actions">
                  <button
                    class="comment-submit"
                    :disabled="!commentContent.trim() || commentSubmitting"
                    @click="handleComment"
                  >
                    发送评论
                  </button>
                </div>
              </div>
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
                      class="comment-delete-btn"
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

            <!-- 空评论提示 -->
            <div class="comment-empty" v-else-if="event.isRegistered || event.status === 'ENDED'">
              <p>暂无评论</p>
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

        <!-- 右栏：信息 + 操作面板 -->
        <div class="side-col">
          <el-card class="info-card" shadow="never">
            <!-- 地点 -->
            <div class="info-item">
              <el-icon :size="16" class="info-icon"><Location /></el-icon>
              <div class="info-body">
                <span class="info-label">地点</span>
                <span class="info-value">{{ event.location }}</span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 时间 -->
            <div class="info-item">
              <el-icon :size="16" class="info-icon"><Clock /></el-icon>
              <div class="info-body">
                <span class="info-label">活动时间</span>
                <span class="info-value">
                  {{ formatDateTime(event.startTime) }}
                  <span class="info-sep">—</span>
                  {{ formatDateTime(event.endTime) }}
                </span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 报名截止 -->
            <div class="info-item">
              <el-icon :size="16" class="info-icon"><Calendar /></el-icon>
              <div class="info-body">
                <span class="info-label">报名截止</span>
                <span class="info-value" :class="{ 'deadline-passed': isDeadlinePassed(event) }">
                  {{ formatDateTime(event.registrationDeadline) }}
                </span>
              </div>
            </div>

            <div class="info-divider" />

            <!-- 参与人数 + 进度条 -->
            <div class="info-item">
              <el-icon :size="16" class="info-icon"><User /></el-icon>
              <div class="info-body">
                <span class="info-label">名额</span>
                <span class="info-value number-value">
                  {{ event.currentParticipants }} / {{ event.maxParticipants }}
                  <span class="remaining-badge" v-if="event.status === 'OPEN'">
                    余 {{ remainingSlots(event) }}
                  </span>
                </span>
                <div class="info-progress">
                  <div class="progress-track">
                    <div
                      class="progress-fill"
                      :style="{ width: progressPercent(event) + '%' }"
                    />
                  </div>
                  <span class="progress-pct">{{ progressPercent(event) }}%</span>
                </div>
              </div>
            </div>

            <!-- ═══ 操作区 ═══════════════════ -->
            <div class="info-actions">
              <!-- 未登录 -->
              <template v-if="!userStore.isLoggedIn">
                <button
                  class="action-btn action-primary"
                  @click="$router.push({ path: '/login', query: { redirect: $route.fullPath } })"
                >
                  登录后即可报名
                </button>
              </template>

              <!-- 已登录 -->
              <template v-else>
                <!-- 可取消报名（已报名 + OPEN + 未开始） -->
                <button
                  v-if="canCancelRegistration(event)"
                  class="action-btn action-outline"
                  @click="handleCancelReg"
                >
                  取消报名
                </button>

                <!-- 可报名（未报名 + OPEN + 未截止 + 未满） -->
                <button
                  v-else-if="canRegister(event)"
                  class="action-btn action-primary"
                  @click="handleRegister"
                >
                  立即报名
                </button>

                <!-- 其他状态：禁用态描述 -->
                <button v-else class="action-btn action-disabled" disabled>
                  <template v-if="event.isRegistered && event.status === 'OPEN'">
                    已报名 · {{ isDeadlinePassed(event) ? '报名已截止' : '名额已满' }}
                  </template>
                  <template v-else-if="event.isRegistered && event.status === 'ONGOING'">
                    已报名 · 进行中
                  </template>
                  <template v-else-if="event.isRegistered && event.status === 'ENDED'">
                    已参加 · 已结束
                  </template>
                  <template v-else-if="event.status === 'ENDED'">
                    活动已结束
                  </template>
                  <template v-else-if="event.status === 'ONGOING'">
                    进行中
                  </template>
                  <template v-else-if="isDeadlinePassed(event)">
                    报名已截止
                  </template>
                  <template v-else>
                    名额已满
                  </template>
                </button>

                <!-- 收藏 -->
                <button
                  class="fav-btn"
                  :class="{ favorited: event.isFavorited }"
                  @click="handleFavoriteToggle"
                >
                  <el-icon :size="15"><StarFilled v-if="event.isFavorited" /><Star v-else /></el-icon>
                  <span>{{ event.isFavorited ? '已收藏' : '收藏活动' }}</span>
                </button>

                <!-- 管理员操作 -->
                <template v-if="isEventAdmin">
                  <div class="info-divider" />
                  <div class="admin-actions">
                    <span class="admin-label">管理</span>
                    <div class="admin-btn-group">
                      <button class="admin-btn" @click="$router.push(`/events/${event.id}/edit`)">
                        <el-icon :size="14"><Edit /></el-icon>
                        编辑
                      </button>
                      <button class="admin-btn admin-btn-danger" @click="handleDeleteEvent">
                        <el-icon :size="14"><Delete /></el-icon>
                        删除
                      </button>
                      <button class="admin-btn" @click="showParticipants = !showParticipants">
                        <el-icon :size="14"><User /></el-icon>
                        参与者
                      </button>
                    </div>
                  </div>
                </template>
              </template>
            </div>

            <!-- 参与者列表（折叠面板） -->
            <template v-if="showParticipants && participants.length > 0">
              <div class="info-divider" />
              <div class="participants-section">
                <span class="participants-heading">参与者 · {{ participants.length }} 人</span>
                <div class="participants-list">
                  <div
                    class="participant-row"
                    v-for="p in participants"
                    :key="p.userId"
                  >
                    <div class="participant-avatar">
                      <img v-if="p.avatar" :src="p.avatar" :alt="p.username" />
                      <span v-else>{{ p.username.charAt(0).toUpperCase() }}</span>
                    </div>
                    <span class="participant-name">{{ p.username }}</span>
                  </div>
                </div>
              </div>
            </template>
            <div v-else-if="showParticipants && participants.length === 0" class="info-divider">
              <div class="participants-section">
                <span class="participants-heading">暂无参与者</span>
              </div>
            </div>
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
  User, ArrowLeft, Edit, Delete, Warning, ChatLineSquare,
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
import { CategoryLabels } from '../utils/eventUtils'
import {
  canRegister, canCancelRegistration, isDeadlinePassed,
  remainingSlots, progressPercent, getStatusLabel, getStatusClass,
  formatDateTime,
} from '../utils/eventUtils'
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
const participants = ref<Participant[]>([])
const showParticipants = ref(false)

const statusLabel = computed(() => event.value ? getStatusLabel(event.value) : '')
const statusClass = computed(() => event.value ? getStatusClass(event.value) : '')

const isEventAdmin = computed(() => {
  if (!userStore.isLoggedIn || !event.value) return false
  return userStore.isAdmin || userStore.user?.id === event.value.creatorId
})

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    event.value = res.data
    // 管理员/创建者可查看参与者
    if (isEventAdmin.value) {
      loadParticipants()
    }
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
  try {
    const res = await getEventParticipants(eventId)
    participants.value = res.data
  } catch {
    participants.value = []
  }
}

async function handleRegister() {
  try {
    await registerEvent(eventId)
    ElMessage.success('报名成功')
    await loadEvent()
  } catch { /* interceptor handles */ }
}

async function handleCancelReg() {
  await ElMessageBox.confirm(
    '确定取消报名吗？',
    '确认取消',
    { type: 'warning', confirmButtonText: '确定', cancelButtonText: '再想想' },
  )
  try {
    await cancelRegistration(eventId)
    ElMessage.success('已取消报名')
    await loadEvent()
  } catch { /* interceptor handles */ }
}

async function handleFavoriteToggle() {
  try {
    if (event.value?.isFavorited) {
      await removeFavorite(eventId)
      ElMessage.success('已取消收藏')
      if (event.value) event.value.isFavorited = false
    } else {
      await addFavorite(eventId)
      ElMessage.success('收藏成功')
      if (event.value) event.value.isFavorited = true
    }
  } catch { /* interceptor handles */ }
}

async function handleDeleteEvent() {
  await ElMessageBox.confirm(
    '确定永久删除该活动吗？此操作不可撤销。',
    '系统警告',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  try {
    await deleteEvent(eventId)
    ElMessage.success('活动已删除')
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
    await loadComments()
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
})
</script>

<style scoped>
/* ════════════════════════════════════════
   Hero — 干净深色标题区
   ════════════════════════════════════════ */
.detail-hero {
  margin: -80px -20px 0;
  padding-top: 80px;
  background: #18181B;
}

.hero-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 28px 20px 32px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.55);
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
  font-size: 28px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.35;
  margin-bottom: 14px;
  letter-spacing: -0.2px;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  line-height: 1.6;
}

.hero-tag-status.status-open {
  background: rgba(180, 83, 9, 0.3);
  color: #FBBF24;
}

.hero-tag-status.status-ongoing {
  background: rgba(5, 150, 105, 0.25);
  color: #6EE7B7;
}

.hero-tag-status.status-ended {
  background: rgba(255, 255, 255, 0.06);
  color: #A1A1AA;
}

.hero-tag-status.status-closed {
  background: rgba(255, 255, 255, 0.06);
  color: #A1A1AA;
}

.hero-creator {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

/* ════════════════════════════════════════
   双栏主体
   ════════════════════════════════════════ */
.detail-body {
  max-width: 960px;
  margin: 24px auto 40px;
  padding: 0 20px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-col {
  width: 340px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

/* ── 封面图 ──────────────────────────── */
.cover-section {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--c-border);
}

.cover-image {
  width: 100%;
  display: block;
  max-height: 400px;
  object-fit: cover;
}

/* ════════════════════════════════════════
   内容卡片
   ════════════════════════════════════════ */
.content-card,
.info-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: var(--shadow-sm) !important;
}

.content-card :deep(.el-card__body) {
  padding: 22px 24px;
}

.info-card :deep(.el-card__body) {
  padding: 18px 20px 20px;
}

.card-heading {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 14px;
}

/* ── 活动详情正文 ────────────────────── */
.detail-text {
  font-size: 14px;
  color: var(--c-text-light);
  line-height: 1.85;
  white-space: pre-wrap;
}

/* ════════════════════════════════════════
   讨论区
   ════════════════════════════════════════ */
.comment-input-area {
  margin-bottom: 20px;
}

.comment-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #FAFAFA;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--c-text-muted);
}

.comment-hint a {
  color: var(--c-primary);
  font-weight: 500;
}

.comment-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--c-border) !important;
  box-shadow: none !important;
  background: #FAFAFA;
  font-size: 13px;
  color: var(--c-text);
  resize: none;
}

.comment-form :deep(.el-textarea__inner:focus) {
  border-color: var(--c-primary) !important;
  box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.1) !important;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
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

/* ── 评论列表 ────────────────────────── */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: #FAFAFA;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background: #B45309;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
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
  margin-bottom: 5px;
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

.comment-delete-btn {
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

.comment-delete-btn:hover {
  color: #EF4444;
}

.comment-text {
  font-size: 13px;
  color: var(--c-text);
  line-height: 1.65;
}

.comment-actions {
  margin-top: 6px;
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
  padding: 28px 0;
  color: var(--c-text-muted);
  font-size: 13px;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

/* ════════════════════════════════════════
   右栏信息卡片
   ════════════════════════════════════════ */
.info-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
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
  font-size: 11px;
  color: var(--c-text-muted);
  margin-bottom: 2px;
}

.info-value {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--c-text);
  line-height: 1.5;
}

.info-value.deadline-passed {
  color: #DC2626;
}

.number-value {
  font-size: 16px;
  font-weight: 600;
}

.remaining-badge {
  font-size: 12px;
  font-weight: 500;
  color: #059669;
  margin-left: 4px;
}

.info-sep {
  color: var(--c-text-muted);
  margin: 0 3px;
  font-weight: 400;
}

.info-divider {
  height: 1px;
  background: #F4F4F5;
  margin: 0;
}

/* ── 进度条 ──────────────────────────── */
.info-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.progress-track {
  flex: 1;
  height: 5px;
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

.progress-pct {
  font-size: 11px;
  font-weight: 500;
  color: var(--c-text-muted);
  min-width: 32px;
  text-align: right;
}

/* ════════════════════════════════════════
   操作按钮
   ════════════════════════════════════════ */
.info-actions {
  padding-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.action-primary {
  background: #B45309;
  color: #FFFFFF;
}

.action-primary:hover {
  background: #D97742;
}

.action-outline {
  background: transparent;
  color: var(--c-text);
  border: 1px solid var(--c-border);
}

.action-outline:hover {
  border-color: var(--c-text-muted);
}

.action-disabled {
  background: #F4F4F5;
  color: #A1A1AA;
  cursor: not-allowed;
  font-size: 13px;
}

/* 收藏按钮 */
.fav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
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

/* ── 管理员操作 ──────────────────────── */
.admin-actions {
  padding-top: 4px;
}

.admin-label {
  font-size: 11px;
  color: var(--c-text-muted);
  display: block;
  margin-bottom: 8px;
}

.admin-btn-group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
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

/* ── 参与者 ──────────────────────────── */
.participants-section {
  padding-top: 12px;
}

.participants-heading {
  font-size: 12px;
  font-weight: 500;
  color: var(--c-text-light);
  display: block;
  margin-bottom: 8px;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.participant-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px;
  border-radius: 6px;
}

.participant-row:hover {
  background: #FAFAFA;
}

.participant-avatar {
  width: 26px;
  height: 26px;
  min-width: 26px;
  border-radius: 50%;
  background: #D4D4D8;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 10px;
  overflow: hidden;
}

.participant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.participant-name {
  font-size: 13px;
  color: var(--c-text);
}

/* ════════════════════════════════════════
   不存在
   ════════════════════════════════════════ */
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

/* ════════════════════════════════════════
   响应式
   ════════════════════════════════════════ */
@media (max-width: 900px) {
  .detail-body {
    flex-direction: column;
  }

  /* 移动端：操作面板置顶，在正文之前 */
  .main-col {
    order: 2;
  }

  .side-col {
    order: 1;
    width: 100%;
    position: static;
  }

  .hero-title {
    font-size: 22px;
  }
}
</style>
