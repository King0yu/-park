<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><User /></el-icon>
        <h2>车主/管理员信息</h2>
      </div>
      <span class="role-tag" :class="roleClass">{{ roleText }}</span>
    </div>

    <!-- 用户信息卡片 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>账户概览</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(userInfo.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 基本信息修改 -->
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Edit /></el-icon>
          <span>基本信息维护</span>
        </div>
      </template>
      <el-form
        ref="infoFormRef"
        :model="infoForm"
        :rules="infoRules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="infoForm.username"
            placeholder="请输入用户名"
            clearable
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="infoForm.phone"
            placeholder="请输入手机号"
            clearable
            maxlength="11"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">
            <el-icon><Check /></el-icon> 保存信息
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>账户安全设置</span>
        </div>
      </template>
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="pwdLoading" @click="handleUpdatePwd">
            <el-icon><Refresh /></el-icon> 修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Document, Edit, Lock, Check, Refresh } from '@element-plus/icons-vue'
import { getCurrentUser, updateInfo, updatePwd } from '@/api/profile.js'

const userInfo = reactive({
  id: null,
  username: '',
  phone: '',
  role: null,
  createTime: null
})

const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = reactive({
  username: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleMap = {
  0: { text: '超级管理员（总管理）', class: 'super-admin' },
  1: { text: '管理员（物业）', class: 'admin' },
  2: { text: '普通用户（车主）', class: 'user' }
}

const roleText = computed(() => {
  return roleMap[userInfo.role]?.text || '未知角色'
})

const roleClass = computed(() => {
  return roleMap[userInfo.role]?.class || ''
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
    return
  }
  callback()
}

const validateConfirmPwd = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const infoRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ]
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    const data = res.data || {}
    Object.assign(userInfo, data)
    infoForm.username = data.username || ''
    infoForm.phone = data.phone || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const handleUpdateInfo = async () => {
  if (!infoFormRef.value) return

  try {
    await infoFormRef.value.validate()
  } catch (e) {
    return
  }

  infoLoading.value = true
  try {
    await updateInfo({
      username: infoForm.username,
      phone: infoForm.phone
    })
    ElMessage.success('个人信息更新成功')
    // 同步更新本地缓存中的显示名称
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        const localUser = JSON.parse(stored)
        localUser.username = infoForm.username
        localUser.phone = infoForm.phone
        localStorage.setItem('userInfo', JSON.stringify(localUser))
      } catch (e) {
        console.error('同步本地用户信息失败:', e)
      }
    }
    loadUserInfo()
  } catch (error) {
    console.error('更新个人信息失败:', error)
  } finally {
    infoLoading.value = false
  }
}

const handleUpdatePwd = async () => {
  if (!pwdFormRef.value) return

  try {
    await pwdFormRef.value.validate()
  } catch (e) {
    return
  }

  pwdLoading.value = true
  try {
    await updatePwd({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
      confirmPassword: pwdForm.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdFormRef.value.resetFields()
    // 清除登录态并跳转登录页
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    window.location.href = '/login'
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;

.profile-container {
  max-width: 960px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  .page-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    h2 {
      margin: 0;
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }
  }

  .title-icon {
    font-size: $font-size-2xl;
    color: $primary-color;
  }
}

.role-tag {
  padding: 4px 12px;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  &.super-admin {
    background: rgba($danger-color, 0.1);
    color: $danger-color;
  }

  &.admin {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.user {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
}

.info-card,
.form-card {
  margin-bottom: $spacing-lg;
  border-radius: $radius-md;
}

.card-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.profile-form {
  max-width: 480px;
}
</style>
