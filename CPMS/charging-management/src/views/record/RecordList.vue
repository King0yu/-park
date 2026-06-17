<template>
  <div class="order-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>停车记录管理</h2>
      <span class="role-tag" v-if="currentUserRole === 0">超级管理员(总管理)</span>
      <span class="role-tag admin" v-else-if="currentUserRole === 1">管理员(物业)</span>
      <span class="role-tag user" v-else>普通用户(车主)</span>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="记录编号">
          <el-input
            v-model="searchForm.recordNo"
            placeholder="请输入记录编号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <!-- 管理员和超级管理员可以按用户筛选 -->
        <el-form-item v-if="isAdmin" label="用户">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="停车位">
          <el-input
            v-model="searchForm.spaceCode"
            placeholder="请输入停车位编码"
            clearable
          />
        </el-form-item>
        <el-form-item label="记录状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            class="search-select"
            clearable
          >
            <el-option label="全部" value="" />
            <el-option label="待停车" value="0" />
            <el-option label="停车中" value="1" />
            <el-option label="已完成" value="2" />
            <el-option label="已取消" value="3" />
            <el-option label="异常结束" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button
            type="danger"
            :disabled="selectedRows.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button circle @click="fetchData">
            刷新
          </el-button>
        </div>
      </div>

      <!-- 停车记录表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        stripe
        border
        class="order-table"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="recordNo" label="记录编号" min-width="140" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="areaName" label="停车区域" min-width="150" />
        <el-table-column prop="spaceCode" label="停车位编码" width="100" />
        <el-table-column prop="spaceName" label="停车位名称" min-width="120" />
        <el-table-column prop="carNumber" label="车牌号" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="170">
          <template #default="{ row }">
            {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="parkingDuration" label="停车时长(小时)" width="120" align="right">
          <template #default="{ row }">
            {{ row.parkingDuration ? row.parkingDuration.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="停车费用(元)" width="110" align="right">
          <template #default="{ row }">
            {{ row.totalCost ? row.totalCost.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="停车位状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'primary'" size="small">
              {{ row.payStatus === 1 ? '空闲' : '已占用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button
              v-if="canFinishOrder(row)"
              link
              type="success"
              @click="handleFinishOrder(row)"
            >
              结束停车
            </el-button>
            <el-button
              v-if="canCancelOrder(row)"
              link
              type="warning"
              @click="handleCancelOrder(row)"
            >
              取消记录
            </el-button>
            <el-button
              v-if="canDeleteOrder(row)"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 停车记录详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="停车记录详情"
      width="600px"
      @close="handleDetailClose"
    >
      <div v-if="detailOrder" class="order-detail">
        <el-descriptions title="停车信息" :column="2">
          <el-descriptions-item label="记录编号">{{ detailOrder.recordNo }}</el-descriptions-item>
          <el-descriptions-item label="记录状态">
            <el-tag :type="getStatusType(detailOrder.status)" size="small">
              {{ getStatusText(detailOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ detailOrder.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="detailOrder.payStatus === 1 ? 'success' : 'warning'" size="small">
              {{ detailOrder.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="停车区域">{{ detailOrder.areaName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="停车位编码">{{ detailOrder.spaceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="停车位名称">{{ detailOrder.spaceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="停车位状态">
            <el-tag :type="detailOrder.payStatus === 1 ? 'success' : 'primary'" size="small">
              {{ detailOrder.payStatus === 1 ? '空闲' : '已占用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(detailOrder.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ detailOrder.endTime ? formatDateTime(detailOrder.endTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="停车时长(小时)">{{ detailOrder.parkingDuration ? detailOrder.parkingDuration.toFixed(2) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="单价(元/小时)">{{ detailOrder.unitPrice ? detailOrder.unitPrice.toFixed(2) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="停车总费用(元)" :span="2">
            <span class="total-amount">{{ detailOrder.totalCost ? detailOrder.totalCost.toFixed(2) : '0.00' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-descriptions title="其他信息" :column="2" v-if="detailOrder.remark">
          <el-descriptions-item label="备注" :span="2">{{ detailOrder.remark }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 结束停车弹窗 -->
    <el-dialog
      v-model="finishDialogVisible"
      title="结束停车"
      width="500px"
      @close="handleFinishClose"
    >
      <div v-if="calculatedOrder" class="finish-summary">
        <h4 class="summary-title">停车费用结算</h4>
        <div class="summary-grid">
          <div class="summary-item">
            <span class="summary-label">停车时长</span>
            <span class="summary-value">{{ calculatedOrder.duration }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">停车时长(小时)</span>
            <span class="summary-value highlight">{{ calculatedOrder.electricity }} 小时</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">单价</span>
            <span class="summary-value">{{ calculatedOrder.unitPrice }} 元/小时</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">总费用</span>
            <span class="summary-value highlight">{{ calculatedOrder.totalCost }} 元</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">开始时间</span>
            <span class="summary-value">{{ calculatedOrder.startTime || '-' }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">结束时间</span>
            <span class="summary-value highlight">{{ calculatedOrder.endTime || '-' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="loading-text">
        <el-spinner />
        <p>正在计算停车费用...</p>
      </div>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="finishLoading"
          :disabled="!calculatedOrder"
          @click="handleFinishSubmit"
        >
          确认结束停车
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 使用全局注册的图标，不需要单独导入
import recordApi from '@/api/record.js'

// 响应式数据
const loading = ref(false)
const finishLoading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const finishFormRef = ref(null)

// 当前登录用户信息
const currentUserId = ref(null)
const currentUserRole = ref(null)

// 自动计算的订单结算信息
const calculatedOrder = ref(null)

// 是否为管理员（超级管理员或管理员）
const isAdmin = computed(() => {
  return currentUserRole.value === 0 || currentUserRole.value === 1
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  recordNo: '',
  username: '',
  spaceCode: '',
  status: ''
})

// 详情订单
const detailOrder = ref(null)
const detailDialogVisible = ref(false)

// 结束充电相关
const finishDialogVisible = ref(false)
const currentFinishOrderId = ref(null)

// 初始化用户信息
const initUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      currentUserId.value = userInfo.id
      currentUserRole.value = userInfo.role
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

// 获取状态文字
const getStatusText = (status) => {
  // 将状态转换为数字，确保与映射表的键类型一致
  const statusNum = Number(status)
  const statusMap = {
    0: '待停车',
    1: '停车中',
    2: '已完成',
    3: '已取消',
    4: '异常结束'
  }
  return statusMap[statusNum] || '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  // 将状态转换为数字，确保与映射表的键类型一致
  const statusNum = Number(status)
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return typeMap[statusNum] || 'default'
}

// 判断是否可以结束记录
const canFinishOrder = (row) => {
  // 只有待停车或停车中的记录才能结束
  if (row.status !== 0 && row.status !== 1) return false

  // 管理员/超级管理员可以结束任何记录
  if (currentUserRole.value === 0 || currentUserRole.value === 1) return true

  // 普通用户只能结束自己的记录
  return currentUserId.value === row.userId
}

// 判断是否可以取消记录
const canCancelOrder = (row) => {
  // 只有待停车的记录才能取消
  if (row.status !== 0) return false

  // 管理员/超级管理员可以取消任何记录
  if (currentUserRole.value === 0 || currentUserRole.value === 1) return true

  // 普通用户只能取消自己的记录
  return currentUserId.value === row.userId
}

// 判断是否可以删除记录
const canDeleteOrder = (row) => {
  // 已删除的记录不能再次删除
  if (row.isDeleted === 1) return false

  // 管理员/超级管理员可以删除任何未删除的记录
  if (currentUserRole.value === 0 || currentUserRole.value === 1) return true

  // 普通用户只能删除自己的已完成、已取消或异常结束的记录
  if (currentUserId.value === row.userId && (row.status === 2 || row.status === 3 || row.status === 4)) {
    return true
  }

  return false
}

// 表格行样式：已删除记录灰色显示
const tableRowClassName = ({ row }) => {
  if (row.isDeleted === 1) {
    return 'deleted-row'
  }
  return ''
}

// 获取订单列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.recordNo) params.recordNo = searchForm.recordNo
    if (searchForm.username && isAdmin.value) params.username = searchForm.username
    if (searchForm.spaceCode) params.spaceCode = searchForm.spaceCode
    // 将状态值转换为数字类型，确保后端能正确接收
    if (searchForm.status !== '' && searchForm.status !== undefined && searchForm.status !== null) {
      params.status = parseInt(searchForm.status, 10)
    }

    console.log('订单列表查询参数:', params)
    console.log('当前用户:', { userId: currentUserId.value, role: currentUserRole.value, isAdmin: isAdmin.value })

    // 普通用户只能查看自己的订单
    if (!isAdmin.value && currentUserId.value) {
      params.userId = currentUserId.value
    }

    const response = await recordApi.getRecordList(params)
    
    console.log('订单列表响应:', response)
    const pageData = response.data
    console.log('订单数据:', pageData)
    tableData.value = pageData.records || pageData.list || []
    pagination.total = pageData.total || 0
    console.log('表格数据:', tableData.value)
    
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  console.log('点击查询按钮')
  console.log('searchForm.status 的值:', searchForm.status, '类型:', typeof searchForm.status)
  pagination.pageNum = 1
  fetchData()
}

// 状态变化处理
const handleStatusChange = (value) => {
  console.log('状态选择变化，新值:', value, '类型:', typeof value)
  pagination.pageNum = 1
  fetchData()
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.username = ''
  searchForm.pileCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  fetchData()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchData()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchData()
}

// 表格选择改变
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await recordApi.getOrderById(row.id)
    detailOrder.value = response.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取停车记录详情失败:', error)
    ElMessage.error(error.message || '获取停车记录详情失败')
  }
}

// 关闭详情弹窗
const handleDetailClose = () => {
  detailOrder.value = null
}

// 打开结束停车弹窗
const handleFinishOrder = async (row) => {
  currentFinishOrderId.value = row.id
  calculatedOrder.value = null
  finishDialogVisible.value = true

  // 获取记录详情用于计算
  try {
    const response = await recordApi.getRecordById(row.id)
    if (response.code === 200 && response.data) {
      const order = response.data
      // 计算停车时长
      const startTime = new Date(order.startTime || order.createTime)
      const endTime = new Date()
      const durationMs = endTime.getTime() - startTime.getTime()
      const hours = durationMs / (1000 * 60 * 60)
      const minutes = Math.floor((hours % 1) * 60)

      // 不满1小时向上取整
      const parkingHours = Math.ceil(hours) || 1

      // 单价5元/小时
      const unitPrice = 5

      // 计算总费用 = 停车时长(小时) × 单价
      const totalCost = (parkingHours * unitPrice).toFixed(2)

      // 格式化时间
      const startTimeStr = formatDateTime(startTime)
      const endTimeStr = formatDateTime(endTime)

      calculatedOrder.value = {
        duration: hours >= 1
          ? `${Math.floor(hours)}小时${minutes}分钟`
          : `${minutes}分钟`,
        electricity: parkingHours,
        unitPrice: unitPrice,
        totalCost: totalCost,
        startTime: startTimeStr,
        endTime: endTimeStr
      }
    }
  } catch (error) {
    console.error('获取记录详情失败:', error)
    ElMessage.error('获取记录详情失败')
  }
}

// 关闭结束停车弹窗
const handleFinishClose = () => {
  currentFinishOrderId.value = null
  calculatedOrder.value = null
}

// 提交结束停车
const handleFinishSubmit = async () => {
  if (!currentFinishOrderId.value) return

  finishLoading.value = true
  try {
    const response = await recordApi.autoFinishRecord(currentFinishOrderId.value)

    if (response.code === 200) {
      ElMessage.success('停车结束成功')
      finishDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('结束停车失败:', error)
    ElMessage.error(error.message || '结束停车失败')
  } finally {
    finishLoading.value = false
  }
}

// 取消记录
const handleCancelOrder = (row) => {
  ElMessageBox.confirm(`确定要取消记录"${row.recordNo}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await recordApi.cancelRecord(row.id)
      if (response.code === 200) {
        ElMessage.success('记录已取消')
        fetchData()
      } else {
        ElMessage.error(response.msg || '取消失败')
      }
    } catch (error) {
      console.error('取消记录失败:', error)
      ElMessage.error(error.message || '取消记录失败')
    }
  }).catch(() => {})
}

// 删除记录
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除记录"${row.recordNo}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await recordApi.deleteRecord(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(response.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除记录失败:', error)
      ElMessage.error(error.message || '删除记录失败')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) return

  const recordNos = selectedRows.value.map(row => row.recordNo).join('、')
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个记录吗？\n${recordNos}`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      for (const row of selectedRows.value) {
        await recordApi.deleteRecord(row.id)
      }
      ElMessage.success('批量删除成功')
      fetchData()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  }).catch(() => {})
}

// 组件挂载时初始化
onMounted(() => {
  initUserInfo()
  fetchData()
})
</script>

<style scoped>
.order-list-container {
  padding: 0;
  min-height: 100%;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -0.5px;
}

.role-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.25);
}

.role-tag.admin {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);
}

.role-tag.user {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.25);
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.search-card :deep(.el-card__body) {
  padding: 24px 24px 14px 24px;
}

.search-form {
  margin-bottom: -10px;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover),
.search-form :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(34, 197, 94, 0.3);
}

.search-form :deep(.el-input__wrapper.is-focus),
.search-form :deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.15), 0 0 0 1px rgba(34, 197, 94, 0.4) !important;
}

.search-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
  padding: 10px 20px;
  height: auto;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.25);
}

.search-form :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(34, 197, 94, 0.35);
}

.search-select {
  width: 150px;
}

.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.8) 0%, rgba(241, 245, 249, 0.6) 100%);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.toolbar :deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
  padding: 10px 20px;
  height: auto;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.toolbar :deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.3);
}

.order-table {
  width: 100%;
}

.order-table :deep(.el-table__header) {
  font-weight: 700;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.order-table :deep(.el-table__header th) {
  color: #475569;
  font-size: 13px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.order-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.order-table :deep(.el-table__body tr:hover) {
  background: rgba(34, 197, 94, 0.03);
}

.order-table :deep(.el-table__body tr.deleted-row) {
  background: #f5f5f5;
  color: #999;
}

.order-table :deep(.el-table__body tr.deleted-row:hover) {
  background: #ebebeb;
}

.order-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.order-table :deep(.el-table__cell) {
  padding: 14px 0;
  color: #475569;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.order-table :deep(.el-tag) {
  border-radius: 20px;
  padding: 0 12px;
  font-weight: 500;
  font-size: 12px;
  height: 26px;
  line-height: 26px;
  border: none;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.6) 0%, rgba(241, 245, 249, 0.4) 100%);
}

.pagination-wrapper :deep(.el-pagination.is-background .btn-next),
.pagination-wrapper :deep(.el-pagination.is-background .btn-prev),
.pagination-wrapper :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.pagination-wrapper :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.order-detail :deep(.el-descriptions__title) {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1e293b;
}

.total-amount {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 24px 28px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

:deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 18px;
  color: #1e293b;
}

:deep(.el-dialog__body) {
  padding: 28px 28px 20px 28px;
}

:deep(.el-dialog__footer) {
  padding: 20px 28px 28px 28px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: transparent !important;
  color: #9333ea !important;
  border: none;
}

:deep(.el-button--primary:hover) {
  color: #7c3aed !important;
  text-decoration: underline;
}

:deep(.el-button .el-icon) {
  margin: 0;
}

@media (max-width: 768px) {
  .order-list-container {
    padding: 0;
  }

  .search-form :deep(.el-form-item) {
    display: block;
    margin-bottom: 15px;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}

/* 结束充电弹窗样式 */
.finish-summary {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  padding: 24px;
}

.summary-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.summary-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.summary-value.highlight {
  font-size: 18px;
  font-weight: 700;
  color: #9333ea;
}

.loading-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 12px;
}

.loading-text p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}
</style>