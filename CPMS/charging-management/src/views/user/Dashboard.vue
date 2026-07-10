<template>
  <div class="user-dashboard">
    <div class="page-header">
      <h2>停车看板</h2>
      <p>欢迎回来，查看您的停车状态和记录</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" :class="activeParking ? 'active' : ''">
          <el-icon :size="24"><Van /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ activeParking ? '停车中' : '未停车' }}</span>
          <span class="stat-label">当前状态</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ totalRecords }}</span>
          <span class="stat-label">停车次数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="24"><Timer /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ totalHours }}h</span>
          <span class="stat-label">累计时长</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="24"><Money /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">¥{{ formatAmount(totalAmount) }}</span>
          <span class="stat-label">累计消费</span>
        </div>
      </div>
    </div>

    <!-- 活动记录 + 快捷操作 -->
    <div class="content-grid">
      <div class="panel">
        <div class="panel-header">
          <h3>进行中的停车</h3>
          <el-button type="primary" size="small" @click="goSimulate">开始停车</el-button>
        </div>
        <div v-if="activeRecords.length > 0" class="active-list">
          <div v-for="record in activeRecords" :key="record.id" class="active-card">
            <div class="active-info">
              <div class="active-space">
                <el-icon><Van /></el-icon>
                <span>{{ record.spaceName || record.spaceCode || '车位' + record.spaceId }}</span>
              </div>
              <div class="active-meta">
                <span>{{ record.carNumber }}</span>
                <span class="meta-divider">|</span>
                <span>开始于 {{ formatTime(record.startTime) }}</span>
              </div>
            </div>
            <div class="active-actions">
              <el-button type="danger" size="small" plain @click="handleEndParking(record)">
                结束停车
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon :size="48"><Van /></el-icon>
          <p>当前没有进行中的停车</p>
          <el-button type="primary" @click="goSimulate">去停车</el-button>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>最近记录</h3>
          <el-button text type="primary" @click="goRecords">查看全部</el-button>
        </div>
        <div v-if="recentRecords.length > 0" class="record-list">
          <div v-for="record in recentRecords" :key="record.id" class="record-item">
            <div class="record-left">
              <span class="record-space">{{ record.spaceName || record.spaceCode || '车位' + record.spaceId }}</span>
              <span class="record-date">{{ formatTime(record.startTime) }}</span>
            </div>
            <div class="record-right">
              <span class="record-cost">¥{{ record.totalCost || '0' }}</span>
              <el-tag :type="record.status === 2 ? 'success' : 'info'" size="small">
                {{ statusText(record.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        <div v-else class="empty-state small">
          <p>暂无停车记录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Van, Document, Timer, Money } from '@element-plus/icons-vue'
import recordApi from '@/api/record'

const router = useRouter()

const activeParking = ref(false)
const totalRecords = ref(0)
const totalHours = ref(0)
const totalAmount = ref('0')
const activeRecords = ref([])
const recentRecords = ref([])

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const statusText = (status) => {
  const map = { 0: '待停车', 1: '停车中', 2: '已完成', 3: '已取消', 4: '异常' }
  return map[status] || '未知'
}

const formatAmount = (val) => {
  const n = typeof val === 'number' ? val : parseFloat(val) || 0
  return n.toFixed(2)
}

const loadData = async () => {
  try {
    // 获取活动记录
    const activeRes = await recordApi.getMyActiveRecords()
    if (activeRes?.code === 200) {
      activeRecords.value = activeRes.data || []
      activeParking.value = activeRecords.value.length > 0
    }

    // 获取最近记录（使用带车位名称的接口）
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await recordApi.getMyRecordsPage({ pageNum: 1, pageSize: 5 })
    if (res?.code === 200) {
      const data = res.data
      recentRecords.value = data.records || []
      totalRecords.value = data.total || 0
    }

    // 获取统计数据
    const statsRes = await recordApi.getMyStatistics()
    if (statsRes?.code === 200) {
      const data = statsRes.data
      totalRecords.value = data.totalRecords || totalRecords.value

      // 兼容两种字段名：totalParkingDuration（新）/ totalDuration（旧）
      const rawDuration = data.totalParkingDuration ?? data.totalDuration ?? 0
      // 后端返回的是小时数（含小数），转成 0.X h 显示
      totalHours.value = typeof rawDuration === 'number'
        ? rawDuration
        : parseFloat(rawDuration) || 0

      totalAmount.value = data.totalAmount || '0'
    }
  } catch (e) {
    console.error('加载数据失败:', e)
  }
}

const goSimulate = () => router.push('/user/simulate')
const goRecords = () => router.push('/user/records')

const handleEndParking = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要结束在【${record.spaceCode || record.spaceId}】的停车吗？系统将自动计算费用。`,
      '确认结束停车',
      { confirmButtonText: '确认结算', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await recordApi.autoFinishRecord(record.id)
    if (res?.code === 200) {
      ElMessage.success('停车已结束，费用已结算')
      // 通知 Layout 刷新停车状态
      window.dispatchEvent(new CustomEvent('parking-status-changed'))
      loadData()
    } else {
      ElMessage.error(res?.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;

.user-dashboard {
  .page-header {
    margin-bottom: $spacing-xl;
    h2 { font-size: $font-size-2xl; color: $text-primary; margin: 0; }
    p { color: $text-muted; margin: 4px 0 0; font-size: $font-size-base; }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(#10b981, 0.1);
  color: #10b981;

  &.active {
    background: rgba(#10b981, 0.15);
    animation: pulse 2s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(#10b981, 0.3); }
  50% { box-shadow: 0 0 0 8px rgba(#10b981, 0); }
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #334155;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-muted;
  margin-top: 2px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-lg;
}

.panel {
  background: white;
  border-radius: $radius-md;
  padding: $spacing-lg;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;

  h3 {
    font-size: $font-size-lg;
    color: $text-primary;
    margin: 0;
    font-weight: $font-weight-semibold;
  }
}

.active-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.active-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  border-radius: $radius-sm;
  border: 1px solid rgba(#10b981, 0.15);
}

.active-info {
  .active-space {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: #059669;
  }
  .active-meta {
    font-size: $font-size-sm;
    color: $text-muted;
    margin-top: 4px;
    .meta-divider { margin: 0 8px; }
  }
}

.empty-state {
  text-align: center;
  padding: $spacing-xl 0;
  color: $text-muted;

  .el-icon { margin-bottom: $spacing-sm; opacity: 0.4; }
  p { margin: $spacing-sm 0; }

  &.small { padding: $spacing-lg 0; }
}

.record-list {
  display: flex;
  flex-direction: column;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;

  & + & { border-top: 1px solid $border-color; }
}

.record-left {
  .record-space { font-weight: $font-weight-medium; color: #334155; display: block; }
  .record-date { font-size: $font-size-sm; color: $text-muted; }
}

.record-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .record-cost {
    font-weight: $font-weight-semibold;
    color: #ef4444;
  }
}

@media (max-width: $breakpoint-md) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
}
</style>
