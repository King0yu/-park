<template>
  <div class="user-list-container">
    <!-- 权限提示 - 普通用户显示 -->
    <div v-if="!hasPermission" class="no-permission">
      <div class="no-permission-content">
        <el-icon class="no-permission-icon"><Lock /></el-icon>
        <h2>您暂无权限访问此页面</h2>
        <p>请联系管理员获取相应权限</p>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
      </div>
    </div>

    <template v-else>
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>用户管理</h2>
        <span class="role-tag super-admin" v-if="currentUserRole === 0">超级管理员</span>
        <span class="role-tag admin" v-if="currentUserRole === 1">管理员</span>
      </div>

      <!-- 搜索和操作区域 -->
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <!-- 管理员看不到角色筛选和超级管理员选项 -->
          <el-form-item v-if="currentUserRole === 0" label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable class="search-select" style="width: 160px">
              <el-option label="超级管理员(总管理)" :value="0" />
              <el-option label="管理员(物业)" :value="1" />
              <el-option label="普通用户(车主)" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="search-select" style="width: 130px">
              <el-option label="禁用" :value="0" />
              <el-option label="启用" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格区域 -->
      <el-card class="table-card">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增用户
            </el-button>
            <el-button
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
          </div>
          <div class="toolbar-right">
            <el-button circle @click="fetchData">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 用户表格 -->
        <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          stripe
          border
          class="user-table"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center" sortable />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="role" label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.role === 0" type="danger" size="small">超级管理员</el-tag>
              <el-tag v-else-if="row.role === 1" type="warning" size="small">管理员</el-tag>
              <el-tag v-else type="success" size="small">普通用户</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" sortable>
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon> 删除
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

      <!-- 新增/编辑用户弹窗 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          class="user-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              :disabled="isEdit"
            />
          </el-form-item>
          <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
            <el-input
              v-model="formData.password"
              type="password"
              :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
              show-password
            />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          <!-- 管理员新增用户时，角色只能选普通用户 -->
          <el-form-item label="角色" prop="role">
            <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
              <el-option v-if="currentUserRole === 0" label="超级管理员(总管理)" :value="0" />
              <el-option v-if="currentUserRole === 0" label="管理员(物业)" :value="1" />
              <el-option label="普通用户(车主)" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const BASE_URL = '/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const formRef = ref(null)

const currentUserId = ref(null)
const currentUserRole = ref(null)

const hasPermission = computed(() => {
  return currentUserRole.value !== 2
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const searchForm = reactive({
  username: '',
  name: '',
  role: null,
  status: null
})

const formData = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
  role: 2,
  status: 1
})

const isEdit = computed(() => !!formData.id)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '用户名只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

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

const formatDate = (date) => {
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

const fetchData = async () => {
  if (!hasPermission.value) {
    return
  }

  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.role !== null) params.role = searchForm.role
    if (searchForm.status !== null) params.status = searchForm.status

    const response = await axios.get(`${BASE_URL}/user/list`, {
      params,
      headers: {
        'X-User-Id': currentUserId.value,
        'X-User-Role': currentUserRole.value
      }
    })

    if (response.data.code === 200) {
      const pageData = response.data.data
      tableData.value = pageData.records || pageData.list || []
      pagination.total = pageData.total || 0
    } else if (response.data.code === 403) {
      ElMessage.error(response.data.msg || '您暂无权限访问此页面')
    } else {
      ElMessage.error(response.data.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    if (error.response?.data?.code === 403) {
      ElMessage.error(error.response.data.msg || '您暂无权限访问此页面')
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.name = ''
  searchForm.role = null
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchData()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  resetForm()
  if (currentUserRole.value === 1) {
    formData.role = 2
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    name: row.name,
    phone: row.phone,
    email: row.email,
    role: row.role,
    status: row.status
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formData.id = null
  formData.username = ''
  formData.password = ''
  formData.name = ''
  formData.phone = ''
  formData.email = ''
  formData.role = 2
  formData.status = 1
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleDialogClose = () => {
  resetForm()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const submitData = { ...formData }
      if (isEdit.value && !submitData.password) {
        delete submitData.password
      }

      const headers = {
        'X-User-Id': currentUserId.value,
        'X-User-Role': currentUserRole.value
      }

      let response
      if (isEdit.value) {
        response = await axios.put(`${BASE_URL}/user/edit`, submitData, { headers })
      } else {
        response = await axios.post(`${BASE_URL}/user/add`, submitData, { headers })
      }

      if (response.data.code === 200) {
        ElMessage.success(response.data.msg || (isEdit.value ? '更新成功' : '添加成功'))
        dialogVisible.value = false
        fetchData()
      } else if (response.data.code === 403) {
        ElMessage.error(response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(response.data.msg || '操作失败')
      }
    } catch (error) {
      console.error('操作失败:', error)
      if (error.response?.data?.code === 403) {
        ElMessage.error(error.response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
      }
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`${BASE_URL}/user/${row.id}`, {
        headers: {
          'X-User-Id': currentUserId.value,
          'X-User-Role': currentUserRole.value
        }
      })
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else if (response.data.code === 403) {
        ElMessage.error(response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(response.data.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      if (error.response?.data?.code === 403) {
        ElMessage.error(error.response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
      }
    }
  }).catch(() => {})
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) return

  const usernames = selectedRows.value.map(row => row.username).join('、')
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？\n${usernames}`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      const response = await axios.delete(`${BASE_URL}/user/batch`, {
        data: ids,
        headers: {
          'X-User-Id': currentUserId.value,
          'X-User-Role': currentUserRole.value
        }
      })
      if (response.data.code === 200) {
        ElMessage.success('批量删除成功')
        fetchData()
      } else if (response.data.code === 403) {
        ElMessage.error(response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(response.data.msg || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      if (error.response?.data?.code === 403) {
        ElMessage.error(error.response.data.msg || '您没有权限执行此操作')
      } else {
        ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
      }
    }
  }).catch(() => {})
}

const goBack = () => {
  router.back()
}

const dialogVisible = ref(false)

onMounted(() => {
  initUserInfo()
  if (hasPermission.value) {
    fetchData()
  }
})
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;
@use '../../styles/mixins.scss' as *;

.user-list-container {
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
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.page-header h2 {
  margin: 0;
  font-size: $font-size-4xl;
  font-weight: $font-weight-extrabold;
  color: $text-primary;
  letter-spacing: -0.5px;
  @include gradient-text(linear-gradient(135deg, $primary-color 0%, $primary-dark 100%));
}

.role-tag {
  padding: 6px 14px;
  color: white;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.3px;
  box-shadow: 0 4px 12px rgba($danger-color, 0.25);
}

.role-tag.super-admin {
  @include gradient-danger;
}

.role-tag.admin {
  @include gradient-warning;
  box-shadow: 0 4px 12px rgba($warning-color, 0.25);
}

.search-card {
  margin-bottom: $spacing-lg;
  border-radius: $radius-lg;
  border: none;
  box-shadow: $shadow-md;
  overflow: hidden;
}

.search-card :deep(.el-card__body) {
  padding: $spacing-lg $spacing-lg $spacing-sm $spacing-lg;
}

.search-form {
  margin-bottom: -10px;
}

.search-select {
  width: 150px;
}

.search-form :deep(.el-input__wrapper) {
  border-radius: $radius-sm;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06);
  transition: all $transition-normal;
}

.search-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba($primary-color, 0.3);
}

.search-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px rgba($primary-color, 0.4) !important;
}

.search-form :deep(.el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06);
}

.search-form :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba($primary-color, 0.3);
}

.table-card {
  border-radius: $radius-lg;
  border: none;
  box-shadow: $shadow-md;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  background: linear-gradient(135deg, rgba($bg-secondary, 0.8) 0%, rgba($bg-tertiary, 0.6) 100%);
}

.toolbar-left {
  display: flex;
  gap: $spacing-sm;
}

.toolbar-right {
  display: flex;
  gap: $spacing-sm;
}

.toolbar :deep(.el-button) {
  border-radius: $radius-sm;
  font-weight: $font-weight-medium;
  padding: 10px 18px;
  height: auto;
  transition: all $transition-normal;
}

.toolbar :deep(.el-button--primary) {
  @include gradient-primary;
  border: none;
  box-shadow: 0 4px 12px rgba($primary-color, 0.25);
}

.toolbar :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($primary-color, 0.35);
}

.toolbar :deep(.el-button--danger) {
  @include gradient-danger;
  border: none;
  box-shadow: 0 4px 12px rgba($danger-color, 0.2);
}

.toolbar :deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($danger-color, 0.3);
}

.user-table {
  width: 100%;
}

.user-table :deep(.el-table__header) {
  font-weight: $font-weight-bold;
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
}

.user-table :deep(.el-table__header th) {
  color: #475569;
  font-size: $font-size-sm;
  padding: $spacing-md 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.user-table :deep(.el-table__body tr) {
  transition: all $transition-fast;
}

.user-table :deep(.el-table__body tr:hover) {
  background: rgba($primary-color, 0.03) !important;
}

.user-table :deep(.el-table__row--striped) {
  background: rgba($bg-secondary, 0.6);
}

.user-table :deep(.el-table__cell) {
  padding: 14px 0;
  color: #475569;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.user-table :deep(.el-tag) {
  border-radius: $radius-full;
  padding: 0 12px;
  font-weight: $font-weight-medium;
  font-size: $font-size-xs;
  height: 26px;
  line-height: 26px;
  border: none;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  background: linear-gradient(135deg, rgba($bg-secondary, 0.6) 0%, rgba($bg-tertiary, 0.4) 100%);
}

.pagination-wrapper :deep(.el-pagination.is-background .btn-next),
.pagination-wrapper :deep(.el-pagination.is-background .btn-prev),
.pagination-wrapper :deep(.el-pagination.is-background .el-pager li) {
  border-radius: $radius-xs;
  margin: 0 4px;
  font-weight: $font-weight-medium;
  transition: all $transition-fast;
}

.pagination-wrapper :deep(.el-pagination.is-background .el-pager li.is-active) {
  @include gradient-primary;
  box-shadow: 0 4px 12px rgba($primary-color, 0.3);
}

.user-form {
  padding: 10px 10px 0 10px;
}

.user-form :deep(.el-input__wrapper),
.user-form :deep(.el-select .el-input__wrapper) {
  border-radius: $radius-sm;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
  transition: all $transition-normal;
}

.user-form :deep(.el-input__wrapper:hover),
.user-form :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba($primary-color, 0.3);
}

.user-form :deep(.el-input__wrapper.is-focus),
.user-form :deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px rgba($primary-color, 0.4) !important;
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
  color: white !important;
  border: none;
  box-shadow: 0 4px 12px rgba($primary-color, 0.25);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba($primary-color, 0.35);
}

:deep(.el-button .el-icon) {
  margin: 0;
}

@media (max-width: $breakpoint-md) {
  .user-list-container {
    padding: 0;
  }

  .page-header h2 {
    font-size: $font-size-2xl;
  }

  .search-form :deep(.el-form-item) {
    display: block;
    margin-bottom: $spacing-md;
  }

  .toolbar {
    flex-direction: column;
    gap: $spacing-sm;
    align-items: flex-start;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>