<template>
  <div class="profile-page">
    <h1>个人中心</h1>

    <!-- 个人信息 -->
    <el-card style="margin-bottom: 20px">
      <template #header><h3>个人信息</h3></template>
      <el-form :model="profileForm" label-width="80px" v-if="userStore.user">
        <el-form-item label="用户名">
          <el-input :value="userStore.user.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="profileForm.avatar" placeholder="头像图片地址" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="profileForm.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签页：我的报名 / 我的收藏 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="我的报名" name="registrations">
        <div class="event-grid" v-loading="listLoading">
          <EventCard v-for="event in myEvents" :key="event.id" :event="event" />
        </div>
        <el-empty v-if="!listLoading && myEvents.length === 0" description="暂无报名记录" />
      </el-tab-pane>
      <el-tab-pane label="我的收藏" name="favorites">
        <div class="event-grid" v-loading="listLoading">
          <EventCard v-for="event in myEvents" :key="event.id" :event="event" />
        </div>
        <el-empty v-if="!listLoading && myEvents.length === 0" description="暂无收藏" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { updateCurrentUser } from '../api/auth'
import { getMyRegistrations } from '../api/event'
import { getMyFavorites } from '../api/favorite'
import { useUserStore } from '../stores/user'
import EventCard from '../components/EventCard.vue'
import type { EventItem } from '../types'

const userStore = useUserStore()

const profileForm = reactive({
  email: '',
  avatar: '',
  bio: '',
})

const activeTab = ref('registrations')
const myEvents = ref<EventItem[]>([])
const listLoading = ref(false)

async function handleUpdateProfile() {
  try {
    await updateCurrentUser(profileForm)
    ElMessage.success('信息已更新')
    userStore.fetchUser()
  } catch { /* error handled by interceptor */ }
}

async function loadRegistrations() {
  listLoading.value = true
  try {
    const res = await getMyRegistrations()
    myEvents.value = res.data
  } finally {
    listLoading.value = false
  }
}

async function loadFavorites() {
  listLoading.value = true
  try {
    const res = await getMyFavorites()
    myEvents.value = res.data
  } finally {
    listLoading.value = false
  }
}

function handleTabChange(tab: string | number) {
  if (tab === 'registrations') loadRegistrations()
  else loadFavorites()
}

onMounted(async () => {
  await userStore.fetchUser()
  if (userStore.user) {
    profileForm.email = userStore.user.email || ''
    profileForm.avatar = userStore.user.avatar || ''
    profileForm.bio = userStore.user.bio || ''
  }
  loadRegistrations()
})
</script>

<style scoped>
.profile-page h1 {
  margin-bottom: 20px;
}

.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 100px;
}
</style>
