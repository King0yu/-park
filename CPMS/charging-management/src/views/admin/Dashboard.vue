<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h2>数据大屏</h2>
      <p>停车场运营数据概览</p>
    </div>

    <!-- 核心统计 -->
    <div class="stats-grid">
      <div class="stat-card blue">
        <div class="stat-icon">
          <el-icon :size="28"><Van /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ stats.totalSpaces }}</span>
          <span class="stat-label">总车位数</span>
        </div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ stats.freeSpaces }}</span>
          <span class="stat-label">空闲车位</span>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="stat-icon">
          <el-icon :size="28"><Timer /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ stats.occupiedSpaces }}</span>
          <span class="stat-label">占用车位</span>
        </div>
      </div>
      <div class="stat-card red">
        <div class="stat-icon">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ stats.todayOrders }}</span>
          <span class="stat-label">今日订单</span>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="stat-icon">
          <el-icon :size="28"><Money /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">¥{{ stats.todayAmount }}</span>
          <span class="stat-label">今日营收</span>
        </div>
      </div>
    </div>

    <!-- 加载/错误状态 -->
    <el-alert
      v-if="loadError"
      :title="`数据加载失败: ${loadError}`"
      type="warning"
      show-icon
      :closable="false"
      class="load-alert"
    >
      <template #default>
        <div>
          <div>{{ loadError }}</div>
          <div class="alert-hint">可能是后端尚未重启或接口未生效，请检查 Maven 重启日志</div>
        </div>
      </template>
    </el-alert>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>占用率</h3>
        </div>
        <div class="chart-body">
          <div class="progress-ring">
            <svg viewBox="0 0 120 120">
              <circle cx="60" cy="60" r="50" fill="none" stroke="#f0f0f0" stroke-width="10"/>
              <circle
                cx="60" cy="60" r="50" fill="none"
                :stroke="occupancyColor"
                stroke-width="10"
                stroke-linecap="round"
                :stroke-dasharray="occupancyDasharray"
                stroke-dashoffset="0"
                transform="rotate(-90 60 60)"
                class="progress-arc"
              />
            </svg>
            <div class="progress-text">
              <span class="progress-value">{{ occupancyRate }}%</span>
              <span class="progress-label">占用率</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>停车位状态分布</h3>
        </div>
        <div class="chart-body">
          <div class="bar-chart">
            <div class="bar-item">
              <span class="bar-label">空闲</span>
              <div class="bar-track">
                <div class="bar-fill green" :style="{ width: freePercent + '%' }"></div>
              </div>
              <span class="bar-value">{{ stats.freeSpaces }}</span>
            </div>
            <div class="bar-item">
              <span class="bar-label">占用</span>
              <div class="bar-track">
                <div class="bar-fill orange" :style="{ width: occupiedPercent + '%' }"></div>
              </div>
              <span class="bar-value">{{ stats.occupiedSpaces }}</span>
            </div>
            <div class="bar-item">
              <span class="bar-label">故障/维护</span>
              <div class="bar-track">
                <div class="bar-fill red" :style="{ width: faultPercent + '%' }"></div>
              </div>
              <span class="bar-value">{{ stats.faultSpaces }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <div class="action-card" @click="$router.push('/admin/users')">
          <el-icon :size="24"><User /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="action-card" @click="$router.push('/admin/devices')">
          <el-icon :size="24"><Grid /></el-icon>
          <span>设备管理</span>
        </div>
        <div class="action-card" @click="$router.push('/admin/orders')">
          <el-icon :size="24"><Document /></el-icon>
          <span>订单管理</span>
        </div>
        <div class="action-card" @click="$router.push('/admin/simulate')">
          <el-icon :size="24"><Van /></el-icon>
          <span>模拟停车</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Van, CircleCheck, Timer, Document, User, Grid, Money } from '@element-plus/icons-vue'
import parkApi from '@/api/park'
import recordApi from '@/api/record'

const stats = ref({
  totalSpaces: 0,
  freeSpaces: 0,
  occupiedSpaces: 0,
  faultSpaces: 0,
  todayOrders: 0,
  todayAmount: 0
})

const loading = ref(false)
const loadError = ref('')

const occupancyRate = computed(() => {
  if (stats.value.totalSpaces === 0) return 0
  return Math.round((stats.value.occupiedSpaces / stats.value.totalSpaces) * 100)
})

const occupancyColor = computed(() => {
  if (occupancyRate.value > 80) return '#ef4444'
  if (occupancyRate.value > 50) return '#f59e0b'
  return '#10b981'
})

const circumference = 2 * Math.PI * 50
const occupancyDasharray = computed(() => {
  const dash = (occupancyRate.value / 100) * circumference
  return `${dash} ${circumference}`
})

const freePercent = computed(() => {
  if (stats.value.totalSpaces === 0) return 0
  return Math.round((stats.value.freeSpaces / stats.value.totalSpaces) * 100)
})
const occupiedPercent = computed(() => {
  if (stats.value.totalSpaces === 0) return 0
  return Math.round((stats.value.occupiedSpaces / stats.value.totalSpaces) * 100)
})
const faultPercent = computed(() => {
  if (stats.value.totalSpaces === 0) return 0
  return Math.round((stats.value.faultSpaces / stats.value.totalSpaces) * 100)
})

const loadData = async () => {
  loading.value = true
  loadError.value = ''
  const errs = []

  // 1) 车位统计
  try {
    const spaceRes = await parkApi.spaceApi.getSpaceStatistics()
    if (spaceRes?.code === 200 && spaceRes.data) {
      stats.value.totalSpaces = spaceRes.data.total || 0
      stats.value.freeSpaces = spaceRes.data.free || 0
      stats.value.occupiedSpaces = spaceRes.data.occupied || 0
      stats.value.faultSpaces = spaceRes.data.fault || 0
    } else {
      errs.push(`车位统计: ${spaceRes?.msg || '返回空'}`)
    }
  } catch (e) {
    errs.push(`车位统计: ${e.message || e}`)
    // Fallback：用列表接口补位
    try {
      const listRes = await parkApi.spaceApi.getSpaceList({ pageNum: 1, pageSize: 1000 })
      if (listRes?.code === 200) {
        const records = listRes.data?.records || []
        stats.value.totalSpaces = records.length
        stats.value.freeSpaces = records.filter(r => r.status === 0).length
        stats.value.occupiedSpaces = records.filter(r => r.status === 1).length
        stats.value.faultSpaces = records.filter(r => [2, 3].includes(r.status)).length
        errs.length = 0  // fallback 成功，清空错误
      }
    } catch (e2) {
      errs.push(`车位列表fallback也失败: ${e2.message || e2}`)
    }
  }

  // 2) 今日订单
  try {
    const orderRes = await recordApi.getTodayStatistics()
    if (orderRes?.code === 200 && orderRes.data) {
      stats.value.todayOrders = orderRes.data.todayOrders || 0
      stats.value.todayAmount = orderRes.data.todayAmount || 0
    } else {
      errs.push(`今日订单: ${orderRes?.msg || '返回空'}`)
    }
  } catch (e) {
    errs.push(`今日订单: ${e.message || e}`)
  }

  if (errs.length > 0) {
    loadError.value = errs.join('；')
  }
  loading.value = false
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;

.admin-dashboard {
  .page-header {
    margin-bottom: $spacing-xl;
    h2 { font-size: $font-size-2xl; color: $text-primary; margin: 0; }
    p { color: $text-muted; margin: 4px 0 0; font-size: $font-size-base; }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.stat-card {
  background: white;
  border-radius: $radius-md;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all $transition-normal;
  border-left: 3px solid transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  &.blue  { border-left-color: #1677ff; .stat-icon { background: rgba(#1677ff, 0.1); color: #1677ff; } }
  &.green { border-left-color: #10b981; .stat-icon { background: rgba(#10b981, 0.1); color: #10b981; } }
  &.orange{ border-left-color: #f59e0b; .stat-icon { background: rgba(#f59e0b, 0.1); color: #f59e0b; } }
  &.red   { border-left-color: #ef4444; .stat-icon { background: rgba(#ef4444, 0.1); color: #ef4444; } }
  &.purple{ border-left-color: #8b5cf6; .stat-icon { background: rgba(#8b5cf6, 0.1); color: #8b5cf6; } }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: #334155;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-muted;
  margin-top: 2px;
}

/* 图表 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.chart-card {
  background: white;
  border-radius: $radius-md;
  padding: $spacing-lg;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.chart-header {
  margin-bottom: $spacing-md;
  h3 { font-size: $font-size-lg; color: $text-primary; margin: 0; font-weight: $font-weight-semibold; }
}

.chart-body {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 环形进度 */
.progress-ring {
  position: relative;
  width: 140px;
  height: 140px;

  svg { width: 100%; height: 100%; }
}

.progress-arc {
  transition: stroke-dasharray 1s ease-out;
}

.progress-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.progress-value {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: #334155;
}

.progress-label {
  font-size: $font-size-sm;
  color: $text-muted;
}

/* 条形图 */
.bar-chart {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.bar-label {
  width: 70px;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: right;
}

.bar-track {
  flex: 1;
  height: 12px;
  background: #f1f5f9;
  border-radius: 6px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 1s ease-out;

  &.green  { background: linear-gradient(90deg, #10b981, #34d399); }
  &.orange { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
  &.red    { background: linear-gradient(90deg, #ef4444, #f87171); }
}

.bar-value {
  width: 36px;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: #334155;
}

/* 快捷操作 */
.quick-actions {
  background: white;
  border-radius: $radius-md;
  padding: $spacing-lg;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  h3 {
    font-size: $font-size-lg;
    color: $text-primary;
    margin: 0 0 $spacing-md;
    font-weight: $font-weight-semibold;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-lg;
  border-radius: $radius-sm;
  background: #f8fafc;
  cursor: pointer;
  transition: all $transition-normal;
  color: $text-secondary;

  &:hover {
    background: rgba($primary-color, 0.06);
    color: $primary-color;
    transform: translateY(-2px);
  }
}

@media (max-width: $breakpoint-md) {
  .stats-grid, .action-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}

.load-alert {
  margin-bottom: $spacing-lg;

  .alert-hint {
    font-size: $font-size-sm;
    color: $text-muted;
    margin-top: 4px;
  }
}</style>
