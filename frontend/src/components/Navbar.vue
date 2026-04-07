<template>
  <el-menu mode="horizontal" :default-active="route.path" router class="navbar">
    <el-menu-item index="/">
      <el-icon><HomeFilled /></el-icon>
      校园活动平台
    </el-menu-item>

    <div class="nav-spacer" />

    <template v-if="userStore.isLoggedIn">
      <el-menu-item v-if="userStore.isAdmin" index="/events/create">
        <el-icon><Plus /></el-icon>
        发布活动
      </el-menu-item>
      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        {{ userStore.username }}
      </el-menu-item>
      <el-menu-item @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出
      </el-menu-item>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
      <el-menu-item index="/register">注册</el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.navbar {
  padding: 0 20px;
}

.nav-spacer {
  flex: 1;
}
</style>
