<template>
  <div class="auth-page">
    <div class="auth-glass-card glass-effect">
      <div class="auth-header">
        <h2 class="text-gradient">欢迎回来</h2>
        <p class="auth-subtitle">登录校园活动平台，发现更多精彩</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @submit.prevent="handleLogin" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" class="custom-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="auth-submit-btn" :loading="loading" native-type="submit">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLoginData(res.data.token, res.data.username, res.data.role)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  padding: 20px;
}

.auth-glass-card {
  width: 100%;
  max-width: 440px;
  padding: 48px;
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.auth-header {
  text-align: center;
}

.auth-header h2 {
  font-size: 32px;
  margin-bottom: 8px;
}

.auth-subtitle {
  color: var(--c-text-light);
  font-size: 15px;
}

.auth-form {
  margin-top: 10px;
}

/* Let the inputs look larger and rounder */
:deep(.el-input__wrapper) {
  padding: 8px 16px;
  height: 48px;
  border-radius: var(--radius-md) !important;
  background-color: rgba(255, 255, 255, 0.6) !important;
}

:deep(.el-input__prefix-inner) {
  font-size: 18px;
  color: var(--c-primary);
}

.auth-submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md) !important;
  margin-top: 12px;
}

.auth-footer {
  text-align: center;
  color: var(--c-text-light);
  font-size: 15px;
}

.register-link {
  color: var(--c-primary);
  font-weight: 600;
  margin-left: 6px;
}

.register-link:hover {
  color: var(--c-primary-light);
  text-decoration: underline;
}
</style>
