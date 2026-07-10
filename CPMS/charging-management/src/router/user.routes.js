/**
 * 用户端（车主）路由
 * 角色限制: role = 2
 */
export default [
  {
    path: 'dashboard',
    name: 'UserDashboard',
    component: () => import('@/views/user/Dashboard.vue'),
    meta: { title: '停车看板' }
  },
  {
    path: 'records',
    name: 'UserRecords',
    component: () => import('@/views/record/RecordList.vue'),
    meta: { title: '我的记录', selfOrderOnly: true }
  },
  {
    path: 'simulate',
    name: 'UserSimulate',
    component: () => import('@/views/simulate/ParkingSimulate.vue'),
    meta: { title: '模拟停车' }
  },
  {
    path: 'profile',
    name: 'UserProfile',
    component: () => import('@/views/profile/Profile.vue'),
    meta: { title: '个人中心' }
  }
]
