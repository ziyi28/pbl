import request from './index'
import type { Result } from '../types'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<string>>('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
