import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import userRoutes from './user.routes.js'
import adminRoutes from './admin.routes.js'

/**
 * 角色常量
 */
const ROLE_SUPER_ADMIN = 0
const ROLE_ADMIN = 1
const ROLE_USER = 2

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },

  // ==================== 用户端（车主） ====================
  {
    path: '/user',
    component: () => import('@/layout/UserLayout.vue'),
    meta: { title: '我的停车', requiresAuth: true, roles: [ROLE_USER] },
    children: userRoutes
  },

  // ==================== 管理端（管理员+超级管理员） ====================
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { title: '管理后台', requiresAuth: true, roles: [ROLE_SUPER_ADMIN, ROLE_ADMIN] },
    children: adminRoutes
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  const titlePrefix = to.matched[0]?.meta?.title || '小区停车场管理系统'
  document.title = to.meta.title
    ? `${to.meta.title} - ${titlePrefix}`
    : titlePrefix

  // 无需认证，直接放行
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // 检查 token
  const token = localStorage.getItem('token')
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        const userRole = Number(user.role)

        if (!to.meta.roles.includes(userRole)) {
          ElMessage.error('您没有访问该页面的权限')

          // 根据角色跳转到各自的首页
          if (userRole === ROLE_USER) {
            next('/user/dashboard')
          } else {
            next('/admin/dashboard')
          }
          return
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }

  next()
})

export default router
