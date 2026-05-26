import request from './index'
import type { Result } from '../types'

const MAX_FILE_SIZE = 2 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp']

export function validateImageFile(file: File) {
  const name = file.name.toLowerCase()
  const hasValidExt = ALLOWED_EXTENSIONS.some(ext => name.endsWith(ext))
  if (!hasValidExt) {
    return '仅支持jpg、jpeg、png、gif、webp格式'
  }
  if (file.size > MAX_FILE_SIZE) {
    return '文件大小不能超过 2MB'
  }
  return null
}

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<string>>('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
