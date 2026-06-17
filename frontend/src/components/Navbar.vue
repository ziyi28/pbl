<template>
  <header class="navbar-container">
    <router-link to="/" class="logo">
      <el-icon class="logo-icon"><School /></el-icon>
      <span class="logo-text">校园活动</span>
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
        <button class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出
        </button>
      </template>
      <template v-else>
        <router-link to="/login" class="login-link">登录</router-link>
        <router-link to="/register" class="register-btn">立即注册</router-link>
      </template>
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
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 100;
  background: var(--c-bg-dark);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  font-family: var(--font-heading);
  text-decoration: none;
  color: #FFFFFF;
}

.logo:hover {
  color: #FFFFFF;
}

.logo-icon {
  font-size: 20px;
  color: var(--c-primary);
}

.nav-spacer {
  flex: 1;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  color: #D4D4D8;
  font-size: 14px;
  font-weight: 400;
  transition: all 0.2s ease;
  text-decoration: none;
  border-radius: 6px;
}

.nav-item:hover {
  color: #FFFFFF;
  background: rgba(255, 255, 255, 0.08);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  outline: none;
  border: none;
  background: transparent;
  color: #D4D4D8;
  transition: all 0.2s ease;
  border-radius: 6px;
  font-family: var(--font-base);
}

.logout-btn:hover {
  color: #FFFFFF;
  background: rgba(255, 255, 255, 0.08);
}

/* 登录 —— 浅灰色文字链接，低调存在 */
.login-link {
  color: #A1A1AA;
  font-size: 14px;
  font-weight: 400;
  text-decoration: none;
  padding: 6px 8px;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #FFFFFF;
}

/* 立即注册 —— 琥珀色实心按钮，视觉重心 */
.register-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 18px;
  font-size: 13px;
  font-weight: 600;
  color: #FFFFFF;
  background: var(--c-primary);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all 0.2s ease;
}

.register-btn:hover {
  background: var(--c-primary-light);
  color: #FFFFFF;
}
</style>
