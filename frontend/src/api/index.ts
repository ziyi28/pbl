import axios from 'axios'
import { getToken, clearAuth } from '../utils/auth'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    // blob 响应（文件下载等）直接返回 data
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    // blob 错误响应：尝试从 blob 中解析错误消息
    if (error.config?.responseType === 'blob' && error.response?.data instanceof Blob) {
      error.response.data.text().then((text: string) => {
        try {
          const err = JSON.parse(text)
          ElMessage.error(err.message || '下载失败')
        } catch {
          ElMessage.error('下载失败')
        }
      })
      return Promise.reject(error)
    }
    if (error.response?.status === 401) {
      clearAuth()
      window.location.href = '/login'
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
