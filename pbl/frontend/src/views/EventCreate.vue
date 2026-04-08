<template>
  <div class="create-page">
    <el-page-header @back="$router.back()">
      <template #content><span>创建活动</span></template>
    </el-page-header>

    <el-card style="margin-top: 20px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="讲座" value="LECTURE" />
            <el-option label="文体" value="SPORTS" />
            <el-option label="社团" value="CLUB" />
            <el-option label="志愿" value="VOLUNTEER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="报名截止" prop="registrationDeadline">
          <el-date-picker v-model="form.registrationDeadline" type="datetime" placeholder="选择报名截止时间" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="99999" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="图片 URL（可选）" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="详细描述活动内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">创建活动</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { createEvent } from '../api/event'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  title: '',
  description: '',
  category: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: 50,
  coverImage: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  registrationDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'change' }],
}

async function handleSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await createEvent(form as any)
    ElMessage.success('活动创建成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-page {
  max-width: 800px;
  margin: 0 auto;
}
</style>
