/**
 * 管理端（管理员 + 超级管理员）路由
 * 角色限制: role = 0 或 1
 */
export default [
  {
    path: 'dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '数据大屏' }
  },
  {
    path: 'users',
    name: 'AdminUsers',
    component: () => import('@/views/user/UserList.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: 'devices',
    name: 'AdminDevices',
    component: () => import('@/views/parking/ParkingManage.vue'),
    meta: { title: '车场设备管理' }
  },
  {
    path: 'orders',
    name: 'AdminOrders',
    component: () => import('@/views/record/RecordList.vue'),
    meta: { title: '停车记录管理' }
  },
  {
    path: 'simulate',
    name: 'AdminSimulate',
    component: () => import('@/views/simulate/ParkingSimulate.vue'),
    meta: { title: '模拟停车登记' }
  },
  {
    path: 'settings',
    name: 'AdminSettings',
    component: () => import('@/views/settings/Settings.vue'),
    meta: { title: '系统设置' }
  },
  {
    path: 'profile',
    name: 'AdminProfile',
    component: () => import('@/views/profile/Profile.vue'),
    meta: { title: '个人中心' }
  }
]
