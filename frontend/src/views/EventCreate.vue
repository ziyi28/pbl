<template>
  <div class="create-page">
    <!-- 返回 + 标题 -->
    <div class="page-header">
      <button class="back-link" @click="$router.back()">
        <el-icon :size="16"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h1 class="page-title">创建新活动</h1>
    </div>

    <div class="form-body">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <!-- ═══ 第1组：基础信息 ═══ -->
        <section class="form-section">
          <h2 class="section-title">基础信息</h2>
          <el-form-item label="活动标题" prop="title">
            <el-input v-model="form.title" placeholder="给活动取一个清晰的名字" maxlength="100" show-word-limit />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="活动分类" prop="category">
                <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                  <el-option label="讲座" value="LECTURE" />
                  <el-option label="文体" value="SPORTS" />
                  <el-option label="社团" value="CLUB" />
                  <el-option label="志愿" value="VOLUNTEER" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="活动地点" prop="location">
                <el-input v-model="form.location" placeholder="如：图书馆报告厅" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <!-- ═══ 第2组：时间规则 ═══ -->
        <section class="form-section">
          <h2 class="section-title">时间规则</h2>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="开始时间" prop="startTime">
                <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="结束时间" prop="endTime">
                <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="报名截止时间" prop="registrationDeadline">
                <el-date-picker v-model="form.registrationDeadline" type="datetime" placeholder="截止时间须早于开始时间" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="人数上限" prop="maxParticipants">
                <el-input-number v-model="form.maxParticipants" :min="1" :max="9999" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <!-- ═══ 第3组：封面与详情 ═══ -->
        <section class="form-section">
          <h2 class="section-title">封面与详情</h2>
          <el-form-item label="封面图片">
            <div class="cover-upload-area">
              <div class="cover-upload-left">
                <div v-if="coverPreview" class="cover-preview">
                  <img :src="coverPreview" alt="封面预览" />
                  <button class="cover-remove" @click="removeCover">✕</button>
                </div>
                <div v-else class="cover-placeholder" @click="triggerUpload">
                  <el-icon :size="28"><Plus /></el-icon>
                  <span>上传封面</span>
                  <span class="cover-hint">支持 JPG/PNG/WebP，≤2MB</span>
                </div>
                <input
                  ref="coverInput"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="handleCoverUpload"
                />
                <span v-if="coverUploading" class="uploading-text">
                  <el-icon class="is-loading" :size="14"><Loading /></el-icon>
                  上传中…
                </span>
              </div>
              <div class="cover-upload-right">
                <span class="cover-or">或</span>
                <el-input v-model="form.coverImage" placeholder="手动输入封面图片 URL" class="cover-url-input" />
              </div>
            </div>
          </el-form-item>

          <el-form-item label="活动描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="详细描述活动内容、亮点、注意事项等…"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>
        </section>
      </el-form>

      <!-- ═══ 第4组：预览确认 ═══ -->
      <section class="form-section" v-if="form.title">
        <h2 class="section-title">预览确认</h2>
        <div class="preview-card">
          <EventCard :event="previewEvent" :interactive="false" />
        </div>
      </section>

      <!-- ═══ 操作按钮 ═══ -->
      <div class="form-actions">
        <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
          <el-icon v-if="submitting" class="is-loading" :size="16"><Loading /></el-icon>
          <span>{{ submitting ? '发布中…' : '发布活动' }}</span>
        </button>
        <button class="cancel-btn" @click="$router.back()">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Plus, Loading } from '@element-plus/icons-vue'
import { createEvent } from '../api/event'
import { uploadFile, validateImageFile } from '../api/upload'
import EventCard from '../components/EventCard.vue'
import type { EventItem } from '../types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const coverUploading = ref(false)
const coverInput = ref<HTMLInputElement | null>(null)
const coverPreview = ref('')

const form = reactive({
  title: '',
  description: '',
  category: '' as string,
  location: '',
  startTime: '' as string | Date,
  endTime: '' as string | Date,
  registrationDeadline: '' as string | Date,
  maxParticipants: 50,
  coverImage: '',
})

/** 预览用的虚拟 EventItem */
const previewEvent = computed<EventItem>(() => ({
  id: 0,
  title: form.title || '（未填写标题）',
  description: form.description,
  category: (form.category || 'OTHER') as any,
  location: form.location || '（未填写地点）',
  startTime: form.startTime ? new Date(form.startTime).toISOString() : '',
  endTime: form.endTime ? new Date(form.endTime).toISOString() : '',
  registrationDeadline: form.registrationDeadline ? new Date(form.registrationDeadline).toISOString() : '',
  maxParticipants: form.maxParticipants,
  currentParticipants: 0,
  coverImage: coverPreview.value || form.coverImage || null,
  status: 'OPEN',
  creatorId: 0,
  creatorName: '预览',
  createdAt: '',
}))

const validateStartTime = (_rule: any, value: any, callback: any) => {
  if (value && new Date(value) <= new Date()) {
    callback(new Error('开始时间必须晚于当前时间'))
  } else {
    callback()
  }
}

const validateEndTime = (_rule: any, value: any, callback: any) => {
  if (form.startTime && value && new Date(value) <= new Date(form.startTime)) {
    callback(new Error('结束时间必须晚于开始时间'))
  } else {
    callback()
  }
}

const validateDeadline = (_rule: any, value: any, callback: any) => {
  if (form.startTime && value && new Date(value) >= new Date(form.startTime)) {
    callback(new Error('报名截止时间必须早于开始时间'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { validator: validateStartTime, trigger: 'change' },
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' },
  ],
  registrationDeadline: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' },
    { validator: validateDeadline, trigger: 'change' },
  ],
  maxParticipants: [{ required: true, message: '请输入人数上限', trigger: 'change' }],
}

/** 将 Date 转为本地时间字符串 YYYY-MM-DDTHH:mm:ss */
function toLocalDateTimeString(val: string | Date): string {
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function triggerUpload() {
  coverInput.value?.click()
}

async function handleCoverUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const msg = validateImageFile(file)
  if (msg) {
    ElMessage.error(msg)
    target.value = ''
    return
  }

  coverUploading.value = true
  try {
    const res = await uploadFile(file)
    const url = res.data
    form.coverImage = url
    coverPreview.value = url
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('上传失败，请重试')
  } finally {
    coverUploading.value = false
    target.value = ''
  }
}

function removeCover() {
  coverPreview.value = ''
  form.coverImage = ''
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await createEvent({
      title: form.title,
      description: form.description,
      category: form.category as any,
      location: form.location,
      startTime: toLocalDateTimeString(form.startTime),
      endTime: toLocalDateTimeString(form.endTime),
      registrationDeadline: toLocalDateTimeString(form.registrationDeadline),
      maxParticipants: form.maxParticipants,
      coverImage: form.coverImage || undefined,
    })
    ElMessage.success('活动创建成功')
    // 使用返回的 id 跳转到详情页
    const eventId = res.data?.id
    if (eventId) {
      router.push(`/events/${eventId}`)
    } else {
      router.push('/')
    }
  } catch {
    /* interceptor handles */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0 60px;
}

/* ── 页面标题 ────────────────────────── */
.page-header {
  margin-bottom: 24px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--c-text-muted);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 8px;
  font-family: var(--font-base);
}

.back-link:hover {
  color: var(--c-text);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
}

/* ── 表单分区 ────────────────────────── */
.form-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-section {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-md);
  padding: 24px 28px;
  box-shadow: var(--shadow-sm);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 18px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--c-border-light);
}

/* ── 封面上传 ────────────────────────── */
.cover-upload-area {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.cover-upload-left {
  flex-shrink: 0;
}

.cover-preview {
  position: relative;
  width: 240px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--c-border);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #FFF;
  border: none;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder {
  width: 240px;
  height: 140px;
  border: 2px dashed var(--c-border);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: var(--c-text-muted);
  font-size: 13px;
  transition: border-color 0.2s ease;
}

.cover-placeholder:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.cover-hint {
  font-size: 11px;
  color: var(--c-text-muted);
}

.uploading-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
  margin-top: 6px;
}

.cover-upload-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.cover-or {
  font-size: 12px;
  color: var(--c-text-muted);
}

/* ── 预览卡片 ────────────────────────── */
.preview-card {
  max-width: 360px;
}

/* ── 操作按钮 ────────────────────────── */
.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 1;
  height: 44px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 600;
  color: #FFFFFF;
  background: #B45309;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-base);
  transition: background 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  background: #D97742;
}

.submit-btn:disabled {
  background: #D4D4D8;
  cursor: not-allowed;
}

.cancel-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--c-text-light);
  background: transparent;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-base);
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  border-color: var(--c-text-muted);
  color: var(--c-text);
}

/* ── 响应式 ──────────────────────────── */
@media (max-width: 640px) {
  .form-section {
    padding: 18px 16px;
  }

  .cover-upload-area {
    flex-direction: column;
  }

  .cover-placeholder,
  .cover-preview {
    width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
