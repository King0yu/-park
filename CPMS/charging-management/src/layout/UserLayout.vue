<template>
  <div class="user-layout">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="header-left">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <rect x="6" y="4" width="12" height="16" rx="2" fill="currentColor"/>
              <path d="M8 8h8v2H8zM8 12h8v2H8z" fill="white"/>
              <circle cx="12" cy="18" r="1.5" fill="white"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">我的停车</span>
            <span class="logo-subtitle">My Parking</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="36" icon="User" class="user-avatar" />
              <div class="user-details">
                <span class="username">{{ userName }}</span>
                <span class="user-role-tag">车主</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="main-container">
      <!-- 侧边栏 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-brand" v-show="!sidebarCollapsed">
          <div class="brand-dot"></div>
          <span>停车服务</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          router
          class="sidebar-menu"
          background-color="transparent"
          text-color="#64748b"
          active-text-color="#1677ff"
        >
          <el-menu-item index="/user/dashboard" class="menu-item">
            <el-icon class="menu-icon"><Odometer /></el-icon>
            <template #title>停车看板</template>
          </el-menu-item>
          <el-menu-item index="/user/records" class="menu-item">
            <el-icon class="menu-icon"><Document /></el-icon>
            <template #title>我的记录</template>
          </el-menu-item>
          <el-menu-item index="/user/simulate" class="menu-item">
            <svg class="menu-icon-svg" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
              <path d="M779.93984 119.82848H422.7072l-178.62656 402.16576h154.23488l-109.568 397.23008 446.5664-479.06816h-163.77856L779.93984 119.82848z m0 0" fill="#64748b"/>
            </svg>
            <template v-if="!sidebarCollapsed">模拟停车</template>
          </el-menu-item>
        </el-menu>

        <!-- 当前停车状态卡片 -->
        <div class="parking-status-card" v-show="!sidebarCollapsed">
          <div class="status-header">
            <span class="status-label">停车状态</span>
          </div>
          <div class="status-body">
            <div class="status-indicator" :class="{ active: hasActiveParking }">
              <span class="status-dot"></span>
              <span class="status-text">{{ hasActiveParking ? '停车中' : '未停车' }}</span>
            </div>
            <div class="status-car" v-if="activeCarNumber">
              <el-icon><Van /></el-icon>
              <span>{{ activeCarNumber }}</span>
            </div>
          </div>
        </div>

        <!-- 折叠/展开按钮 -->
        <div class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <span class="toggle-arrow">{{ sidebarCollapsed ? '》' : '《' }}</span>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <div class="content-inner">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Odometer, Van, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import recordApi from '@/api/record'

const router = useRouter()
const route = useRoute()

const userName = ref('用户')
const hasActiveParking = ref(false)
const activeCarNumber = ref('')
const sidebarCollapsed = ref(false)

const activeMenu = computed(() => route.path)

// 监听路由变化，每次进入新页面重新加载停车状态
watch(
  () => route.path,
  () => {
    loadParkingStatus()
  }
)

const loadParkingStatus = async () => {
  try {
    const res = await recordApi.getMyActiveRecords()
    if (res?.code === 200) {
      const records = res.data || []
      hasActiveParking.value = records.length > 0
      if (hasActiveParking.value) {
        activeCarNumber.value = records[0].carNumber || ''
      } else {
        activeCarNumber.value = ''
      }
    }
  } catch (e) {
    console.warn('加载停车状态失败:', e)
  }
}

onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      userName.value = user.name || user.username || '用户'
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  loadParkingStatus()

  // 监听停车状态变化事件（Dashboard 结算后触发）
  window.addEventListener('parking-status-changed', loadParkingStatus)
})

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.success('已退出登录')
        router.push('/login')
      }).catch(() => {})
      break
    case 'profile':
      router.push('/user/profile')
      break
  }
}
</script>

<style lang="scss" scoped>
@use '../styles/variables.scss' as *;
@use '../styles/mixins.scss' as *;

.user-layout {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f0fdf4 0%, #f8fafc 100%);
}

.header {
  height: 72px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $spacing-xl;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(#10b981, 0.3);
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  letter-spacing: -0.3px;
}

.logo-subtitle {
  font-size: $font-size-xs;
  color: $text-muted;
  font-weight: $font-weight-medium;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
  padding: 10px $spacing-md;
  border-radius: $radius-md;
  transition: all $transition-normal;
  background: transparent;
  &:hover { background: rgba(#10b981, 0.06); }
}

.user-avatar {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  box-shadow: 0 4px 12px rgba(#10b981, 0.25);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-size: $font-size-base;
  color: #334155;
  font-weight: $font-weight-semibold;
}

.user-role-tag {
  font-size: 11px;
  color: #10b981;
  font-weight: $font-weight-medium;
  background: rgba(#10b981, 0.1);
  padding: 0 6px;
  border-radius: 4px;
  line-height: 18px;
}

.dropdown-icon {
  color: $text-muted;
  font-size: $font-size-sm;
  transition: transform $transition-normal;
}

.user-dropdown:hover .dropdown-icon {
  transform: translateY(2px);
}

.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// ========== 侧边栏 ==========
.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  overflow: hidden;
  padding: $spacing-lg $spacing-md;
  border-right: 1px solid rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &.collapsed {
    width: 64px;
    padding: $spacing-lg 4px;

    .sidebar-menu { overflow: hidden; }
    .menu-item { justify-content: center; padding: 0 !important; }
    .menu-icon { margin: 0 !important; }
    .menu-icon-svg { margin: 0 !important; }
  }
}

// 折叠/展开按钮
.toggle-btn {
  position: absolute;
  bottom: 50%;
  right: 0;
  transform: translateY(50%);
  width: 24px;
  height: 48px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-right: none;
  border-radius: 8px 0 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all $transition-normal;
  z-index: 10;

  &:hover {
    background: rgba(#10b981, 0.1);
    border-color: rgba(#10b981, 0.2);
  }

  .toggle-arrow {
    font-size: 12px;
    color: $text-muted;
    font-weight: $font-weight-bold;
    user-select: none;
    transition: color $transition-fast;
  }

  &:hover .toggle-arrow {
    color: #10b981;
  }
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 $spacing-sm $spacing-md $spacing-sm;
  margin-bottom: $spacing-sm;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.brand-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: 50%;
  box-shadow: 0 0 12px rgba(#10b981, 0.5);
}

.sidebar-brand span {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $text-muted;
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;

  &:not(.el-menu--collapse) {
    width: 100%;
  }
}

.menu-item {
  border-radius: $radius-sm !important;
  margin-bottom: 6px;
  height: 48px !important;
  line-height: 48px !important;
  transition: all $transition-normal;

  &:hover {
    background: rgba(#10b981, 0.06) !important;
    transform: translateX(4px);
  }

  &.is-active {
    background: linear-gradient(135deg, rgba(#10b981, 0.12) 0%, rgba(#10b981, 0.06) 100%) !important;
    color: #10b981 !important;
  }
}

.collapsed .menu-item:hover {
  transform: none;
  background: rgba(#10b981, 0.08) !important;
}

.menu-icon {
  font-size: 18px !important;
  margin-right: 10px !important;
}

.menu-icon-svg {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  flex-shrink: 0;

  path {
    transition: fill $transition-fast;
  }
}

.menu-item:hover .menu-icon-svg path,
.menu-item.is-active .menu-icon-svg path {
  fill: #10b981;
}

/* 停车状态卡片 */
.parking-status-card {
  margin-top: auto;
  padding: $spacing-md;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  border-radius: $radius-md;
  border: 1px solid rgba(#10b981, 0.15);
}

.status-header {
  margin-bottom: 8px;
}

.status-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $text-muted;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.status-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(#94a3b8, 0.1);

  &.active {
    background: rgba(#10b981, 0.1);
  }
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;

  .active & {
    background: #10b981;
    animation: statusPulse 2s ease-in-out infinite;
  }
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.status-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: #64748b;

  .active & {
    color: #059669;
  }
}

.status-car {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: $font-size-sm;
  color: #334155;
  font-weight: $font-weight-medium;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
}

.content-inner {
  animation: contentFadeIn 0.5s ease-out;
}

@keyframes contentFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 滚动条 */
.content::-webkit-scrollbar { width: 8px; }
.content::-webkit-scrollbar-track { background: transparent; }
.content::-webkit-scrollbar-thumb { background: rgba(0, 0, 0, 0.1); border-radius: 4px; }
.content::-webkit-scrollbar-thumb:hover { background: rgba(0, 0, 0, 0.2); }

.sidebar::-webkit-scrollbar { width: 6px; }
.sidebar::-webkit-scrollbar-track { background: transparent; }
.sidebar::-webkit-scrollbar-thumb { background: rgba(0, 0, 0, 0.08); border-radius: 3px; }

@media (max-width: $breakpoint-md) {
  .header { padding: 0 $spacing-md; }
  .sidebar {
    width: 200px;
    &.collapsed { width: 64px; }
  }
  .content { padding: $spacing-md; }
  .logo-text { display: none; }
}
</style>
