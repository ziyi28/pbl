import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: { title: '首页' },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { title: '登录', guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
      meta: { title: '注册', guest: true },
    },
    {
      path: '/events/:id',
      name: 'EventDetail',
      component: () => import('../views/EventDetail.vue'),
      meta: { title: '活动详情' },
    },
    {
      path: '/events/create',
      name: 'EventCreate',
      component: () => import('../views/EventCreate.vue'),
      meta: { title: '创建活动', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/events/:id/edit',
      name: 'EventEdit',
      component: () => import('../views/EventEdit.vue'),
      meta: { title: '编辑活动', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Profile.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '校园活动平台'} - 校园活动平台`

  const token = getToken()

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.guest && token) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
