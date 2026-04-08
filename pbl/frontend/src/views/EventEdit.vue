<template>
  <div class="edit-page">
    <el-page-header @back="$router.back()">
      <template #content><span>编辑活动</span></template>
    </el-page-header>

    <el-card style="margin-top: 20px" v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" v-if="loaded">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="活动分类" prop="category">
          <el-select v-model="form.category">
            <el-option label="讲座" value="LECTURE" />
            <el-option label="文体" value="SPORTS" />
            <el-option label="社团" value="CLUB" />
            <el-option label="志愿" value="VOLUNTEER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" />
        </el-form-item>
        <el-form-item label="报名截止" prop="registrationDeadline">
          <el-date-picker v-model="form.registrationDeadline" type="datetime" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="图片 URL（可选）" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
  max-width: 800px;
  margin: 0 auto;
}
</style>
