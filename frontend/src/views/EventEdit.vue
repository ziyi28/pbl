<template>
  <div class="edit-page">
    <div class="page-header">
      <el-page-header @back="$router.back()">
        <template #content><span class="header-title">编辑活动</span></template>
      </el-page-header>
      <p class="header-subtitle fade-in-up">修改活动信息，确保参与者获取最新动态</p>
    </div>

    <div class="form-glass-card glass-effect fade-in-up" style="animation-delay: 0.1s" v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" v-if="loaded" class="custom-form">
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入活动标题" class="custom-input" />
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :sm="12">
            <el-form-item label="活动分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" class="custom-select" style="width: 100%">
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
              <el-input v-model="form.location" placeholder="请输入活动地点" class="custom-input" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" class="custom-date" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" class="custom-date" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="报名截止" prop="registrationDeadline">
              <el-date-picker v-model="form.registrationDeadline" type="datetime" placeholder="选择报名截止时间" class="custom-date" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="活动人数上限" prop="maxParticipants">
              <el-input-number v-model="form.maxParticipants" :min="1" :max="99999" class="custom-input-number" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="封面图片链接">
              <el-input v-model="form.coverImage" placeholder="请输入封面图片的 URL（可选）" class="custom-input" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="活动描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="6" placeholder="详细描述活动的内容与亮点..." class="custom-textarea" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmit" class="submit-btn" size="large">保存修改</el-button>
          <el-button @click="$router.back()" class="cancel-btn" size="large">取消</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { getEvent, updateEvent } from '../api/event'

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.id)
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const loaded = ref(false)

const form = reactive({
  title: '',
  description: '',
  category: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: 0,
  coverImage: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
}

async function loadEvent() {
  loading.value = true
  try {
    const res = await getEvent(eventId)
    Object.assign(form, {
      title: res.data.title,
      description: res.data.description,
      category: res.data.category,
      location: res.data.location,
      startTime: res.data.startTime,
      endTime: res.data.endTime,
      registrationDeadline: res.data.registrationDeadline,
      maxParticipants: res.data.maxParticipants,
      coverImage: res.data.coverImage || '',
    })
    loaded.value = true
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await updateEvent(eventId, form as any)
    ElMessage.success('活动更新成功')
    router.push(`/events/${eventId}`)
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadEvent())
</script>

<style scoped>
.edit-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0 60px;
}

.page-header {
  margin-bottom: 30px;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  font-family: var(--font-heading);
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-secondary-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  color: var(--c-text-light);
  font-size: 15px;
  margin-top: 12px;
  margin-left: 40px; /* Aligned with title considering page-header back icon */
}

.form-glass-card {
  padding: 40px;
  border-radius: var(--radius-xl);
}

:deep(.custom-input .el-input__wrapper),
:deep(.custom-select .el-input__wrapper),
:deep(.custom-date .el-input__wrapper),
:deep(.custom-textarea .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md) !important;
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.custom-input .el-input__wrapper.is-focus),
:deep(.custom-select .el-input__wrapper.is-focus),
:deep(.custom-date .el-input__wrapper.is-focus),
:deep(.custom-textarea .el-textarea__inner:focus) {
  background: #fff;
  border-color: var(--c-primary-light);
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.1) !important;
}

:deep(.custom-input-number .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md) !important;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 8px !important;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.submit-btn {
  flex: 1;
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-primary-light) 100%) !important;
  border: none !important;
  font-weight: 600;
  font-size: 16px;
  border-radius: var(--radius-md) !important;
}

.cancel-btn {
  flex: 1;
  font-weight: 600;
  font-size: 16px;
  border-radius: var(--radius-md) !important;
}

.fade-in-up {
  animation: fadeInUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  0% { transform: translateY(20px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

@media (max-width: 768px) {
  .form-glass-card {
    padding: 24px;
  }
  .form-actions {
    flex-direction: column;
  }
  .header-subtitle {
    margin-left: 0;
  }
}
</style>
