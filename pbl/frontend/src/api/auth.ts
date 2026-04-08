import request from './index'
import type { Result, LoginRequest, RegisterRequest, LoginResponse, User, UserUpdateRequest } from '../types'

export function login(data: LoginRequest) {
  return request.post<any, Result<LoginResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<any, Result<void>>('/auth/register', data)
}

export function getCurrentUser() {
  return request.get<any, Result<User>>('/users/me')
}

export function updateCurrentUser(data: UserUpdateRequest) {
  return request.put<any, Result<User>>('/users/me', data)
}
