<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="event">
      <el-page-header @back="$router.back()">
        <template #content>
          <span>{{ event.title }}</span>
        </template>
      </el-page-header>

      <div class="detail-content">
        <!-- 活动信息 -->
        <el-card class="info-card">
          <div class="cover" v-if="event.coverImage">
            <img :src="event.coverImage" :alt="event.title" />
          </div>

          <h1>{{ event.title }}</h1>

          <div class="tags">
            <el-tag>{{ CategoryMap[event.category] }}</el-tag>
            <el-tag :type="statusType">{{ StatusMap[event.status] }}</el-tag>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="地点">{{ event.location }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ event.creatorName }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ event.startTime }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ event.endTime }}</el-descriptions-item>
            <el-descriptions-item label="报名截止">{{ event.registrationDeadline }}</el-descriptions-item>
            <el-descriptions-item label="报名人数">
              {{ event.currentParticipants }} / {{ event.maxParticipants }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="description">
            <h3>活动详情</h3>
            <p>{{ event.description }}</p>
          </div>

          <!-- 操作按钮 -->
          <div class="actions" v-if="userStore.isLoggedIn">
            <el-button type="primary" size="large" @click="handleRegister" v-if="event.status === 'OPEN'">
              报名参加
            </el-button>
            <el-button size="large" @click="handleCancelReg">
              取消报名
            </el-button>
            <el-button :icon="Star" circle @click="handleFavorite" />
          </div>
        </el-card>

        <!-- 评论区 -->
        <el-card class="comment-card">
          <template #header>
            <h3>评论</h3>
          </template>

          <!-- 发表评论 -->
          <div class="comment-form" v-if="userStore.isLoggedIn">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="发表你的评论..."
              maxlength="500"
              show-word-limit
            />
            <el-button type="primary" style="margin-top: 8px" @click="handleComment" :disabled="!commentContent.trim()">
              发表评论
            </el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div class="comment-item" v-for="comment in comments" :key="comment.id">
              <div class="comment-header">
                <span class="comment-user">{{ comment.username }}</span>
                <span class="comment-time">{{ comment.createdAt }}</span>
                <el-button
                  v-if="userStore.isLoggedIn && (userStore.user?.id === comment.userId || userStore.isAdmin)"
                  type="danger"
                  text
                  size="small"
                  @click="handleDeleteComment(comment.id)"
                >
                  删除
                </el-button>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </div>

          <el-empty v-if="comments.length === 0" description="暂无评论" :image-size="60" />

          <div class="pagination" v-if="commentTotal > 20">
            <el-pagination
              v-model:current-page="commentPage"
              :page-size="20"
              :total="commentTotal"
              layout="prev, pager, next"
              @current-change="loadComments"
            />
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvent, registerEvent, cancelRegistration } from '../api/event'
import { getComments, createComment, deleteComment } from '../api/comment'
import { addFavorite } from '../api/favorite'
import { useUserStore } from '../stores/user'
import { CategoryMap, StatusMap } from '../types'
import type { EventItem, Comment } from '../types'

const route = useRoute()
const userStore = useUserStore()
const eventId = Number(route.params.id)

const event = ref<EventItem | null>(null)
const loading = ref(false)
const comments = ref<Comment[]>([])
const commentContent = ref('')
const commentPage = ref(1)
const commentTotal = ref(0)

const statusType = computed(() => {
  switch (event.value?.status) {
    case 'OPEN': return 'success'
    case 'ONGOING': return 'warning'
    case 'ENDED': return 'info'
    default: return 'info'
  }
})

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    event.value = res.data
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
    loadEvent()
  } catch { /* error handled by interceptor */ }
}

async function handleCancelReg() {
  try {
    await cancelRegistration(eventId)
    ElMessage.success('已取消报名')
    loadEvent()
  } catch { /* error handled by interceptor */ }
}

async function handleFavorite() {
  try {
    await addFavorite(eventId)
    ElMessage.success('收藏成功')
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
  max-width: 900px;
  margin: 0 auto;
}

.detail-content {
  margin-top: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.cover {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  max-height: 400px;
}

.cover img {
  width: 100%;
  object-fit: cover;
}

.info-card h1 {
  font-size: 24px;
  margin-bottom: 12px;
}

.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.description {
  margin-top: 20px;
}

.description h3 {
  margin-bottom: 8px;
}

.description p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.comment-form {
  margin-bottom: 20px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-user {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
