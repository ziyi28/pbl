import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser } from '../api/auth'
import { getToken, setToken, setStoredUser, clearAuth, getStoredUser } from '../utils/auth'
import type { User } from '../types'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(getToken())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    if (user.value) return user.value.role === 'ADMIN'
    const stored = getStoredUser()
    return stored?.role === 'ADMIN'
  })
  const username = computed(() => {
    if (user.value) return user.value.username
    const stored = getStoredUser()
    return stored?.username || ''
  })

  function setLoginData(t: string, uname: string, role: string) {
    token.value = t
    setToken(t)
    setStoredUser(uname, role)
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const res = await getCurrentUser()
      user.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    user.value = null
    token.value = null
    clearAuth()
  }

  return { user, token, isLoggedIn, isAdmin, username, setLoginData, fetchUser, logout }
})
