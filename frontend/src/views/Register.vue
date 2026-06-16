<template>
  <div class="auth-page">
    <div class="auth-glass-card glass-effect">
      <div class="auth-header">
        <h2 class="text-gradient">加入我们</h2>
        <p class="auth-subtitle">创建你的专属账号，开启校园新生活</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @submit.prevent="handleRegister" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请设置用户名" :prefix-icon="User" size="large" class="custom-input" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入常用邮箱" :prefix-icon="Message" size="large" class="custom-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请设置密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="auth-submit-btn" :loading="loading" native-type="submit">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6-30 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await register({ username: form.username, password: form.password, email: form.email })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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

.login-link {
  color: var(--c-primary);
  font-weight: 600;
  margin-left: 6px;
}

.login-link:hover {
  color: var(--c-primary-light);
  text-decoration: underline;
}
</style>
