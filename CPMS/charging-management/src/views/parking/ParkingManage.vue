<template>
  <div class="device-manage">
    <!-- 权限检查 -->
    <div v-if="!hasPermission" class="no-permission">
      <div class="no-permission-content">
        <el-icon class="no-permission-icon"><Lock /></el-icon>
        <h2>您暂无权限访问此页面</h2>
        <p>请联系物业管理员或超级管理员获取相应权限</p>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
      </div>
    </div>

    <!-- 正常页面内容 -->
    <div v-else>
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>小区停车场管理</h1>
        <p>管理停车区域、停车位信息</p>
      </div>

      <!-- 标签页 -->
      <div class="tabs-container">
        <el-tabs v-model="activeTab" type="card" class="device-tabs">
          <!-- 停车区域管理 -->
          <el-tab-pane label="停车区域管理" name="area">
            <!-- 停车区域搜索栏 -->
            <div class="search-bar">
              <el-input
                v-model="areaSearchForm.areaCode"
                placeholder="区域编码"
                class="search-input"
                clearable
              />
              <el-input
                v-model="areaSearchForm.areaName"
                placeholder="区域名称"
                class="search-input"
                clearable
              />
              <el-select
                v-model="areaSearchForm.status"
                placeholder="状态"
                class="search-select"
                clearable
              >
                <el-option label="全部" :value="''" />
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
                <el-option label="维护中" :value="2" />
              </el-select>
              <el-button type="primary" @click="searchArea">查询</el-button>
              <el-button @click="resetAreaSearch">重置</el-button>
              <el-button type="success" @click="showAreaAddDialog">新增区域</el-button>
            </div>

            <!-- 停车区域表格 -->
            <el-table
              :data="areaTableData"
              :loading="areaLoading"
              border
              stripe
              class="device-table"
            >
              <el-table-column prop="areaCode" label="区域编码" />
              <el-table-column prop="areaName" label="区域名称" />
              <el-table-column prop="address" label="地址" />
              <el-table-column prop="province" label="省份" />
              <el-table-column prop="city" label="城市" />
              <el-table-column prop="totalSpaces" label="停车位数量" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getAreaStatusType(scope.row.status)">{{ getAreaStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="showAreaEditDialog(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteArea(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 停车区域分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="areaPageInfo.pageNum"
                v-model:page-size="areaPageInfo.pageSize"
                :total="areaPageInfo.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="areaPageSizeChange"
                @current-change="areaPageChange"
              />
            </div>
          </el-tab-pane>

          <!-- 停车位管理 -->
          <el-tab-pane label="停车位管理" name="space">
            <!-- 停车位搜索栏 -->
            <div class="search-bar">
              <el-select
                v-model="spaceSearchForm.areaId"
                placeholder="所属区域"
                class="search-select"
                clearable
              >
                <el-option label="全部" :value="''" />
                <el-option
                  v-for="area in areaOptions"
                  :key="area.id"
                  :label="area.areaName"
                  :value="area.id"
                />
              </el-select>
              <el-input
                v-model="spaceSearchForm.spaceCode"
                placeholder="停车位编码"
                class="search-input"
                clearable
              />
              <el-input
                v-model="spaceSearchForm.spaceName"
                placeholder="停车位名称"
                class="search-input"
                clearable
              />
              <el-select
                v-model="spaceSearchForm.status"
                placeholder="状态"
                class="search-select"
                clearable
              >
                <el-option label="全部" :value="''" />
                <el-option label="空闲" :value="0" />
                <el-option label="已占用" :value="1" />
                <el-option label="故障" :value="2" />
                <el-option label="维护" :value="3" />
              </el-select>
              <el-button type="primary" @click="searchSpace">查询</el-button>
              <el-button @click="resetSpaceSearch">重置</el-button>
              <el-button type="success" @click="showSpaceAddDialog">新增停车位</el-button>
            </div>

            <!-- 停车位表格 -->
            <el-table
              :data="spaceTableData"
              :loading="spaceLoading"
              border
              stripe
              class="device-table"
            >
              <el-table-column prop="spaceCode" label="停车位编码" />
              <el-table-column prop="spaceName" label="停车位名称" />
              <el-table-column prop="areaId" label="所属区域">
                <template #default="scope">
                  {{ getAreaName(scope.row.areaId) }}
                </template>
              </el-table-column>
              <el-table-column prop="spaceType" label="停车位类型" />
              <el-table-column prop="areaSize" label="面积(㎡)" />
              <el-table-column prop="carNumber" label="车牌号" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getSpaceStatusType(scope.row.status)">{{ getSpaceStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="showSpaceEditDialog(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteSpace(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 停车位分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="spacePageInfo.pageNum"
                v-model:page-size="spacePageInfo.pageSize"
                :total="spacePageInfo.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="spacePageSizeChange"
                @current-change="spacePageChange"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 停车区域新增/编辑弹窗 -->
    <el-dialog
      :title="areaDialogTitle"
      v-model="areaDialogVisible"
      width="600px"
    >
      <el-form :model="areaForm" ref="areaFormRef" label-width="120px">
        <el-form-item label="区域编码" prop="areaCode">
          <el-input v-model="areaForm.areaCode" placeholder="请输入区域编码" />
        </el-form-item>
        <el-form-item label="区域名称" prop="areaName">
          <el-input v-model="areaForm.areaName" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="areaForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="areaForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="areaForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="areaForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="areaForm.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="areaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveArea">确定</el-button>
      </template>
    </el-dialog>

    <!-- 停车位新增/编辑弹窗 -->
    <el-dialog
      :title="spaceDialogTitle"
      v-model="spaceDialogVisible"
      width="600px"
    >
      <el-form :model="spaceForm" ref="spaceFormRef" label-width="120px">
        <el-form-item label="所属区域" prop="areaId">
          <el-select v-model="spaceForm.areaId" placeholder="请选择停车区域">
            <el-option
              v-for="area in areaOptions"
              :key="area.id"
              :label="area.areaName"
              :value="area.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="停车位编码" prop="spaceCode">
          <el-input v-model="spaceForm.spaceCode" placeholder="请输入停车位编码" />
        </el-form-item>
        <el-form-item label="停车位名称" prop="spaceName">
          <el-input v-model="spaceForm.spaceName" placeholder="请输入停车位名称" />
        </el-form-item>
        <el-form-item label="停车位类型" prop="spaceType">
          <el-input v-model="spaceForm.spaceType" placeholder="如：地下、地上、立体" />
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="areaSize">
          <el-input v-model.number="spaceForm.areaSize" type="number" placeholder="请输入面积" />
        </el-form-item>
        <el-form-item label="车牌号" prop="carNumber">
          <el-input v-model="spaceForm.carNumber" placeholder="请输入车牌号（业主车位绑定用）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="spaceForm.status">
            <el-option label="空闲" :value="0" />
            <el-option label="已占用" :value="1" />
            <el-option label="故障" :value="2" />
            <el-option label="维护" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="spaceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSpace">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import deviceApi from '@/api/park'
import { useRouter } from 'vue-router'

// =============== 权限控制 ===============
const hasPermission = ref(false)
const currentRole = ref(null)

const router = useRouter()

const checkPermission = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      currentRole.value = user.role
      hasPermission.value = user.role !== 2
    } catch (e) {
      console.error('解析用户信息失败:', e)
      hasPermission.value = false
    }
  } else {
    hasPermission.value = false
  }
}

const goBack = () => {
  router.back()
}

// =============== 标签页 ===============
const activeTab = ref('area')

// =============== 停车区域管理 ===============
const areaLoading = ref(false)
const areaTableData = ref([])
const areaPageInfo = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const areaSearchForm = reactive({
  areaCode: '',
  areaName: '',
  status: ''
})

// 区域弹窗
const areaDialogVisible = ref(false)
const areaDialogTitle = ref('新增区域')
const areaForm = reactive({
  id: null,
  areaCode: '',
  areaName: '',
  address: '',
  province: '',
  city: '',
  district: '',
  status: 1
})

const getAreaList = () => {
  areaLoading.value = true
  deviceApi.areaApi.getAreaList({
    pageNum: areaPageInfo.pageNum,
    pageSize: areaPageInfo.pageSize,
    areaCode: areaSearchForm.areaCode || undefined,
    areaName: areaSearchForm.areaName || undefined,
    status: areaSearchForm.status ? parseInt(areaSearchForm.status) : undefined
  }).then(res => {
    areaTableData.value = res.data.records
    areaPageInfo.total = res.data.total
  }).catch(err => {
    ElMessage.error(err.message)
  }).finally(() => {
    areaLoading.value = false
  })
}

const searchArea = () => {
  areaPageInfo.pageNum = 1
  getAreaList()
}

const resetAreaSearch = () => {
  areaSearchForm.areaCode = ''
  areaSearchForm.areaName = ''
  areaSearchForm.status = ''
  areaPageInfo.pageNum = 1
  getAreaList()
}

const areaPageSizeChange = (size) => {
  areaPageInfo.pageSize = size
  areaPageInfo.pageNum = 1
  getAreaList()
}

const areaPageChange = (page) => {
  areaPageInfo.pageNum = page
  getAreaList()
}

const showAreaAddDialog = () => {
  areaDialogTitle.value = '新增区域'
  areaForm.id = null
  areaForm.areaCode = ''
  areaForm.areaName = ''
  areaForm.address = ''
  areaForm.province = ''
  areaForm.city = ''
  areaForm.district = ''
  areaForm.status = 1
  areaDialogVisible.value = true
}

const showAreaEditDialog = (row) => {
  areaDialogTitle.value = '编辑区域'
  areaForm.id = row.id
  areaForm.areaCode = row.areaCode
  areaForm.areaName = row.areaName
  areaForm.address = row.address
  areaForm.province = row.province
  areaForm.city = row.city
  areaForm.district = row.district
  areaForm.status = row.status
  areaDialogVisible.value = true
}

const saveArea = () => {
  if (!areaForm.areaCode || !areaForm.areaName) {
    ElMessage.error('请填写必填字段')
    return
  }

  const data = { ...areaForm }
  const api = areaForm.id ? deviceApi.areaApi.updateArea : deviceApi.areaApi.addArea

  api(data).then(res => {
    ElMessage.success(areaForm.id ? '更新成功' : '新增成功')
    areaDialogVisible.value = false
    getAreaList()
    loadAreaOptions()
  }).catch(err => {
    ElMessage.error(err.message || '操作失败')
  })
}

const deleteArea = (id) => {
  ElMessageBox.confirm('确定要删除该停车区域吗？', '提示', {
    type: 'warning'
  }).then(() => {
    deviceApi.areaApi.deleteArea(id).then(res => {
      ElMessage.success('删除成功')
      getAreaList()
      loadAreaOptions()
    }).catch(err => {
      ElMessage.error(err.message)
    })
  }).catch(() => {})
}

const getAreaStatusText = (status) => {
  const map = { 0: '停用', 1: '启用', 2: '维护中' }
  return map[status] || '未知'
}

const getAreaStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

// =============== 停车位管理 ===============
const spaceLoading = ref(false)
const spaceTableData = ref([])
const spacePageInfo = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const spaceSearchForm = reactive({
  areaId: '',
  spaceCode: '',
  spaceName: '',
  status: ''
})

// 区域选项（用于下拉选择）
const areaOptions = ref([])
const loadAreaOptions = () => {
  deviceApi.areaApi.getAreaList({ pageNum: 1, pageSize: 999 }).then(res => {
    areaOptions.value = res.data.records || []
  }).catch(err => {
    console.error('加载区域列表失败:', err)
  })
}

const getAreaName = (areaId) => {
  const area = areaOptions.value.find(s => s.id === areaId)
  return area ? area.areaName : '未知区域'
}

// 停车位弹窗
const spaceDialogVisible = ref(false)
const spaceDialogTitle = ref('新增停车位')
const spaceForm = reactive({
  id: null,
  areaId: null,
  spaceCode: '',
  spaceName: '',
  spaceType: '',
  areaSize: null,
  carNumber: '',
  status: 0
})

const getSpaceList = () => {
  spaceLoading.value = true
  deviceApi.spaceApi.getSpaceList({
    pageNum: spacePageInfo.pageNum,
    pageSize: spacePageInfo.pageSize,
    areaId: spaceSearchForm.areaId ? parseInt(spaceSearchForm.areaId) : undefined,
    spaceCode: spaceSearchForm.spaceCode || undefined,
    spaceName: spaceSearchForm.spaceName || undefined,
    status: spaceSearchForm.status ? parseInt(spaceSearchForm.status) : undefined
  }).then(res => {
    spaceTableData.value = res.data.records
    spacePageInfo.total = res.data.total
  }).catch(err => {
    ElMessage.error(err.message)
  }).finally(() => {
    spaceLoading.value = false
  })
}

const searchSpace = () => {
  spacePageInfo.pageNum = 1
  getSpaceList()
}

const resetSpaceSearch = () => {
  spaceSearchForm.areaId = ''
  spaceSearchForm.spaceCode = ''
  spaceSearchForm.spaceName = ''
  spaceSearchForm.status = ''
  spacePageInfo.pageNum = 1
  getSpaceList()
}

const spacePageSizeChange = (size) => {
  spacePageInfo.pageSize = size
  spacePageInfo.pageNum = 1
  getSpaceList()
}

const spacePageChange = (page) => {
  spacePageInfo.pageNum = page
  getSpaceList()
}

const showSpaceAddDialog = () => {
  spaceDialogTitle.value = '新增停车位'
  spaceForm.id = null
  spaceForm.areaId = null
  spaceForm.spaceCode = ''
  spaceForm.spaceName = ''
  spaceForm.spaceType = ''
  spaceForm.areaSize = null
  spaceForm.carNumber = ''
  spaceForm.status = 0
  spaceDialogVisible.value = true
}

const showSpaceEditDialog = (row) => {
  spaceDialogTitle.value = '编辑停车位'
  spaceForm.id = row.id
  spaceForm.areaId = row.areaId
  spaceForm.spaceCode = row.spaceCode
  spaceForm.spaceName = row.spaceName
  spaceForm.spaceType = row.spaceType
  spaceForm.areaSize = row.areaSize
  spaceForm.carNumber = row.carNumber
  spaceForm.status = row.status
  spaceDialogVisible.value = true
}

const saveSpace = () => {
  if (!spaceForm.areaId || !spaceForm.spaceCode) {
    ElMessage.error('请填写必填字段')
    return
  }

  const data = { ...spaceForm }
  const api = spaceForm.id ? deviceApi.spaceApi.updateSpace : deviceApi.spaceApi.addSpace

  api(data).then(res => {
    ElMessage.success(spaceForm.id ? '更新成功' : '新增成功')
    spaceDialogVisible.value = false
    getSpaceList()
  }).catch(err => {
    ElMessage.error(err.message)
  })
}

const deleteSpace = (id) => {
  ElMessageBox.confirm('确定要删除该停车位吗？', '提示', {
    type: 'warning'
  }).then(() => {
    deviceApi.spaceApi.deleteSpace(id).then(res => {
      ElMessage.success('删除成功')
      getSpaceList()
    }).catch(err => {
      ElMessage.error(err.message)
    })
  }).catch(() => {})
}

const getSpaceStatusText = (status) => {
  const map = { 0: '空闲', 1: '已占用', 2: '故障', 3: '维护' }
  return map[status] || '未知'
}

const getSpaceStatusType = (status) => {
  const map = { 0: 'success', 1: 'primary', 2: 'danger', 3: 'warning' }
  return map[status] || 'info'
}

// =============== 页面初始化 ===============
onMounted(() => {
  checkPermission()
  if (hasPermission.value) {
    getAreaList()
    loadAreaOptions()
  }
})

// 切换标签页时加载对应数据
watch(activeTab, (newTab) => {
  if (!hasPermission.value) return
  switch (newTab) {
    case 'area':
      getAreaList()
      break
    case 'space':
      getSpaceList()
      break
  }
})
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;
@use '../../styles/mixins.scss' as *;

.device-manage {
  padding: 0;
  min-height: 100%;
}

.no-permission {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
}

.no-permission-content {
  text-align: center;
  padding: $spacing-2xl $spacing-xl;
  background: white;
  border-radius: $radius-xl;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  animation: noPermissionFadeIn 0.5s ease-out;
}

@keyframes noPermissionFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.no-permission-icon {
  font-size: 72px;
  color: $danger-color;
  margin: 0 auto $spacing-lg auto;
  display: block;
  animation: iconFloat 3s ease-in-out infinite;
  width: fit-content;
}

.no-permission-content :deep(.el-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.no-permission-content h2 {
  margin: 0 0 $spacing-sm 0;
  color: $text-primary;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
}

.no-permission-content p {
  margin: 0 0 $spacing-xl 0;
  color: $text-secondary;
  font-size: $font-size-md;
}

.page-header {
  margin-bottom: $spacing-lg;
}

.page-header h1 {
  font-size: $font-size-4xl;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  font-weight: $font-weight-extrabold;
  letter-spacing: -0.5px;
  @include gradient-text(linear-gradient(135deg, $primary-color 0%, $primary-dark 100%));
}

.page-header p {
  color: $text-secondary;
  font-size: $font-size-md;
}

.device-tabs {
  background: white;
  border-radius: $radius-lg;
  box-shadow: $shadow-md;
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.device-tabs :deep(.el-tabs__header) {
  padding: $spacing-lg $spacing-lg 0 $spacing-lg;
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  margin: 0;
}

.device-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.device-tabs :deep(.el-tabs__item) {
  padding: 0 $spacing-lg;
  height: 44px;
  line-height: 44px;
  font-weight: $font-weight-semibold;
  color: $text-secondary;
  font-size: $font-size-base;
  transition: all $transition-normal;
}

.device-tabs :deep(.el-tabs__item:hover) {
  color: $primary-color;
}

.device-tabs :deep(.el-tabs__item.is-active) {
  color: $primary-color;
}

.device-tabs :deep(.el-tabs__active-bar) {
  @include gradient-primary;
  height: 3px;
  border-radius: 2px;
}

.device-tabs :deep(.el-tabs__content) {
  padding: $spacing-lg;
}

.search-bar {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
  border-radius: $radius-md;
  border: 1px solid rgba(0, 0, 0, 0.03);
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 160px;
}

.search-bar :deep(.el-input__wrapper),
.search-bar :deep(.el-select .el-input__wrapper) {
  border-radius: $radius-sm;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06);
  transition: all $transition-normal;
}

.search-bar :deep(.el-input__wrapper:hover),
.search-bar :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba($primary-color, 0.3);
}

.search-bar :deep(.el-input__wrapper.is-focus),
.search-bar :deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px rgba($primary-color, 0.4) !important;
}

.search-bar :deep(.el-button) {
  border-radius: $radius-sm;
  font-weight: $font-weight-medium;
  padding: 10px 20px;
  height: auto;
  transition: all $transition-normal;
}

.search-bar :deep(.el-button--primary) {
  @include gradient-primary;
  border: none;
  box-shadow: 0 4px 12px rgba($primary-color, 0.25);
}

.search-bar :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($primary-color, 0.35);
}

.search-bar :deep(.el-button--success) {
  @include gradient-accent;
  border: none;
  box-shadow: 0 4px 12px rgba($accent-color, 0.25);
}

.search-bar :deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($accent-color, 0.35);
}

.device-table {
  background: white;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-md;
}

.device-table :deep(.el-table__header) {
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
}

.device-table :deep(.el-table__header th) {
  color: #475569;
  font-weight: $font-weight-bold;
  font-size: $font-size-sm;
  padding: $spacing-md 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.device-table :deep(.el-table__body tr) {
  transition: all $transition-fast;
}

.device-table :deep(.el-table__body tr:hover) {
  background: rgba($primary-color, 0.03) !important;
}

.device-table :deep(.el-table__row--striped) {
  background: rgba($bg-secondary, 0.6);
}

.device-table :deep(.el-table__cell) {
  padding: 14px 0;
  color: #475569;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.device-table :deep(.el-tag) {
  border-radius: $radius-full;
  padding: 0 12px;
  font-weight: $font-weight-medium;
  font-size: $font-size-xs;
  height: 26px;
  line-height: 26px;
  border: none;
}

.device-table :deep(.el-button) {
  border-radius: $radius-xs;
  font-weight: $font-weight-medium;
  transition: all $transition-fast;
}

.device-table :deep(.el-button--primary) {
  @include gradient-primary;
  border: none;
}

.device-table :deep(.el-button--danger) {
  @include gradient-danger;
  border: none;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: $spacing-md;
  background: white;
  border-radius: $radius-md;
  box-shadow: $shadow-md;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.pagination :deep(.el-pagination.is-background .btn-next),
.pagination :deep(.el-pagination.is-background .btn-prev),
.pagination :deep(.el-pagination.is-background .el-pager li) {
  border-radius: $radius-xs;
  margin: 0 4px;
  font-weight: $font-weight-medium;
  transition: all $transition-fast;
}

.pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  @include gradient-primary;
  box-shadow: 0 4px 12px rgba($primary-color, 0.3);
}

:deep(.el-dialog) {
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
  padding: $spacing-lg;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

:deep(.el-dialog__title) {
  font-weight: $font-weight-bold;
  font-size: $font-size-xl;
  color: $text-primary;
}

:deep(.el-dialog__body) {
  padding: $spacing-lg;
}

:deep(.el-dialog__footer) {
  padding: $spacing-lg;
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

:deep(.el-form-item) {
  margin-bottom: $spacing-lg;
}

:deep(.el-form-item__label) {
  font-weight: $font-weight-semibold;
  color: #334155;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: $radius-sm;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
  transition: all $transition-normal;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba($primary-color, 0.3);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px rgba($primary-color, 0.4) !important;
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: $radius-sm;
  font-weight: $font-weight-medium;
  transition: all $transition-normal;
}

:deep(.el-button--primary) {
  @include gradient-primary;
  border: none;
  box-shadow: 0 4px 12px rgba($primary-color, 0.25);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($primary-color, 0.35);
}

@media (max-width: $breakpoint-md) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .pagination {
    justify-content: center;
  }
}
</style>