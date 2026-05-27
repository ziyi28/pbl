<template>
  <header class="navbar-container glass-effect">
    <router-link to="/" class="logo">
      <el-icon class="logo-icon"><HomeFilled /></el-icon>
      <span class="logo-text">校园活动 Pro</span>
    </router-link>

    <div class="nav-spacer" />

    <nav class="nav-links">
      <div class="action-group">
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
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

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
  top: 0;
  left: 0;
  width: 100%;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 100;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  font-family: var(--font-heading);
  text-decoration: none;
  color: white;
}

.logo-icon {
  font-size: 22px;
  color: white;
}

.nav-spacer {
  flex: 1;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-group {
  display: flex;
  gap: 2px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 4px;
  align-items: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  color: #f5f5f5;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
  border-radius: 6px;
}

.nav-item:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
}

.router-link-active {
  color: #ffffff;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  border: none;
  background: transparent;
  color: #f5f5f5;
  transition: all 0.2s ease;
  border-radius: 6px;
}

.nav-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
}

.register-btn {
  background: transparent;
  color: #f5f5f5;
}

.register-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
}

.logout-btn {
  color: #f5f5f5;
}

/* Fix element plus routing style conflict */
.nav-links .nav-item.router-link-exact-active:not(.logo) {
  color: var(--c-primary);
  font-weight: 600;
}
</style>
