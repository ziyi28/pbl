<template>
  <div class="edit-page">
    <div class="page-header">
      <button class="back-link" @click="$router.back()">
        <el-icon :size="16"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h1 class="page-title">编辑活动</h1>
    </div>

    <!-- 当前状态条 -->
    <div class="status-bar" v-if="loaded">
      <div class="status-item">
        <span class="status-label">当前状态</span>
        <span class="status-value" :class="statusClass">{{ StatusLabels[eventStatus] || eventStatus }}</span>
      </div>
      <div class="status-divider" />
      <div class="status-item">
        <span class="status-label">已报名</span>
        <span class="status-value">{{ currentParticipants }} 人</span>
      </div>
      <div class="status-divider" />
      <div class="status-item">
        <span class="status-label">人数上限</span>
        <span class="status-value">{{ form.maxParticipants }} 人</span>
      </div>
    </div>

    <div class="form-body" v-loading="loading">
      <template v-if="loaded">
        <!-- ═══ 基础信息 ═══ -->
        <section class="form-section">
          <h2 class="section-title">基础信息</h2>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="活动标题" maxlength="100" show-word-limit />
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
                  <el-input v-model="form.location" placeholder="活动地点" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </section>

        <!-- ═══ 时间规则 ═══ -->
        <section class="form-section">
          <h2 class="section-title">时间规则</h2>
          <el-form :model="form" ref="formRef2" label-position="top">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="开始时间">
                  <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="结束时间">
                  <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="报名截止时间">
                  <el-date-picker v-model="form.registrationDeadline" type="datetime" placeholder="报名截止时间" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="人数上限">
                  <el-input-number
                    v-model="form.maxParticipants"
                    :min="currentParticipants"
                    :max="9999"
                    style="width: 100%"
                  />
                  <span class="field-hint">不能小于当前报名人数（{{ currentParticipants }}）</span>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </section>

        <!-- ═══ 封面与详情 ═══ -->
        <section class="form-section">
          <h2 class="section-title">封面与详情</h2>
          <el-form :model="form" label-position="top">
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
                    <span class="cover-hint">JPG/PNG/WebP，≤2MB</span>
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
                  <el-input v-model="form.coverImage" placeholder="手动输入封面图片 URL" />
                </div>
              </div>
            </el-form-item>

            <el-form-item label="活动描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="6"
                placeholder="详细描述活动内容…"
                maxlength="5000"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </section>

        <!-- ═══ 操作按钮 ═══ -->
        <div class="form-actions">
          <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
            <el-icon v-if="submitting" class="is-loading" :size="16"><Loading /></el-icon>
            <span>{{ submitting ? '保存中…' : '保存修改' }}</span>
          </button>
          <button class="cancel-btn" @click="$router.back()">取消</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Plus, Loading } from '@element-plus/icons-vue'
import { getEvent, updateEvent } from '../api/event'
import { uploadFile, validateImageFile } from '../api/upload'
import { StatusLabels, getStatusClass } from '../utils/eventUtils'

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.id)
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const loaded = ref(false)
const coverUploading = ref(false)
const coverInput = ref<HTMLInputElement | null>(null)
const coverPreview = ref('')
const eventStatus = ref('')
const currentParticipants = ref(0)

const form = reactive({
  title: '',
  description: '',
  category: '' as string,
  location: '',
  startTime: '' as string | Date,
  endTime: '' as string | Date,
  registrationDeadline: '' as string | Date,
  maxParticipants: 0,
  coverImage: '',
})

const statusClass = computed(() => getStatusClass({ status: eventStatus.value } as any))

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
}

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    const e = res.data
    eventStatus.value = e.status
    currentParticipants.value = e.currentParticipants
    coverPreview.value = e.coverImage || ''
    Object.assign(form, {
      title: e.title,
      description: e.description,
      category: e.category,
      location: e.location,
      startTime: e.startTime,
      endTime: e.endTime,
      registrationDeadline: e.registrationDeadline,
      maxParticipants: e.maxParticipants,
      coverImage: e.coverImage || '',
    })
    loaded.value = true
  } finally {
    loading.value = false
  }
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
    form.coverImage = res.data
    coverPreview.value = res.data
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('上传失败')
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

  // 前端时间校验
  const start = new Date(form.startTime)
  const end = new Date(form.endTime)
  const deadline = new Date(form.registrationDeadline)

  if (end <= start) {
    ElMessage.error('结束时间必须晚于开始时间')
    return
  }
  if (deadline >= start) {
    ElMessage.error('报名截止时间必须早于开始时间')
    return
  }
  if (form.maxParticipants < currentParticipants.value) {
    ElMessage.error(`人数上限不能小于当前报名人数（${currentParticipants.value}）`)
    return
  }

  submitting.value = true
  try {
    await updateEvent(eventId, {
      title: form.title,
      description: form.description,
      category: form.category as any,
      location: form.location,
      startTime: new Date(form.startTime).toISOString(),
      endTime: new Date(form.endTime).toISOString(),
      registrationDeadline: new Date(form.registrationDeadline).toISOString(),
      maxParticipants: form.maxParticipants,
      coverImage: form.coverImage || undefined,
    })
    ElMessage.success('活动更新成功')
    router.push(`/events/${eventId}`)
  } catch {
    /* interceptor handles */
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadEvent())
</script>

<style scoped>
.edit-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0 60px;
}

.page-header {
  margin-bottom: 20px;
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

/* ── 状态栏 ──────────────────────────── */
.status-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 20px;
  background: #FAFAFA;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-label {
  font-size: 11px;
  color: var(--c-text-muted);
}

.status-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.status-value.status-open { color: #B45309; }
.status-value.status-ongoing { color: #059669; }
.status-value.status-ended { color: #A1A1AA; }
.status-value.status-closed { color: #71717A; }

.status-divider {
  width: 1px;
  height: 32px;
  background: var(--c-border);
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

.field-hint {
  display: block;
  font-size: 11px;
  color: var(--c-text-muted);
  margin-top: 4px;
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

  .status-bar {
    gap: 12px;
  }
}
</style>
