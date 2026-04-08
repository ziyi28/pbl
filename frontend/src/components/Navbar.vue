<template>
  <header class="navbar-container glass-effect">
    <router-link to="/" class="logo">
      <el-icon class="logo-icon"><HomeFilled /></el-icon>
      <span class="logo-text text-gradient">校园活动 Pro</span>
    </router-link>

    <div class="nav-spacer" />

    <nav class="nav-links">
      <template v-if="userStore.isLoggedIn">
        <router-link v-if="userStore.isAdmin" to="/events/create" class="nav-item">
          <el-icon><Plus /></el-icon> 发布活动
        </router-link>
        <router-link to="/profile" class="nav-item">
          <el-icon><User /></el-icon> {{ userStore.username }}
        </router-link>
        <button class="nav-btn logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出
        </button>
      </template>
      <template v-else>
        <router-link to="/login" class="nav-item login-link">登录</router-link>
        <router-link to="/register" class="nav-btn register-btn">立即注册</router-link>
      </template>
    </nav>
  </header>
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
.navbar-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 40px);
  max-width: 1200px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 100;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 800;
  font-family: var(--font-heading);
  text-decoration: none;
}

.logo-icon {
  font-size: 24px;
  color: var(--c-primary);
}

.nav-spacer {
  flex: 1;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-round);
  color: var(--c-text);
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
}

.nav-item:hover {
  background: rgba(118, 75, 162, 0.08); /* slight purple tint */
  color: var(--c-primary);
}

.router-link-active {
  color: var(--c-primary);
  background: rgba(118, 75, 162, 0.08);
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 100px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  border: none;
  transition: all 0.2s ease;
}

.register-btn {
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-primary-light) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(118, 75, 162, 0.2);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
  color: white;
}

.logout-btn {
  background: transparent;
  color: #f56c6c;
}

.logout-btn:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

/* Fix element plus routing style conflict */
.nav-links .nav-item.router-link-exact-active:not(.logo) {
  color: var(--c-primary);
  font-weight: 600;
}
</style>
