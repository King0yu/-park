import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 路由配置
 */

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
  {
    path: '/device/manage',
    name: 'DeviceManage',
    component: () => import('@/views/parking/ParkingManage.vue'),
    meta: {
      title: '设备管理',
      requiresAuth: true,
      roles: [0, 1, 2]
    }
  },

  {
    path: '/system',
    name: 'System',
    component: () => import('@/layout/Layout.vue'),
    meta: { title: '系统管理', requiresAuth: true },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserList.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/parking/ParkingManage.vue'),
        meta: {
          title: '设备管理',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/record/RecordList.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          roles: [0, 1, 2],
          selfOrderOnly: true
        }
      },
      {
        path: 'parking-simulate',
        name: 'ParkingSimulate',
        component: () => import('@/views/simulate/ParkingSimulate.vue'),
        meta: {
          title: '模拟停车登记',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: {
          title: '系统设置',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      }
    ]
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫
 */
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 小区停车场管理系统`
    : '小区停车场管理系统'

  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    if (to.meta.roles) {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        // 将 role 转换为数字类型，确保类型匹配
        const userRole = Number(user.role)
        if (!to.meta.roles.includes(userRole)) {
          ElMessage.error('您没有访问该页面的权限')
          next('/')
          return
        }
      }
    }
  }

  next()
})

export default router
