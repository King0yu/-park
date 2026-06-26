<template>
  <div class="login-container">
    <!-- 背景 -->
    <div class="background">
      <div class="grid-pattern"></div>
      <div class="gradient gradient-1"></div>
      <div class="gradient gradient-2"></div>
      
      <!-- 停车场主题装饰 -->
      <div class="parking-decoration parking-car">
        <svg viewBox="0 0 100 60" fill="none">
          <rect x="5" y="20" width="90" height="35" rx="5" fill="#1677ff"/>
          <rect x="15" y="25" width="25" height="20" rx="3" fill="#ffffff"/>
          <rect x="60" y="25" width="25" height="20" rx="3" fill="#ffffff"/>
          <circle cx="15" cy="52" r="8" fill="#1e293b"/>
          <circle cx="85" cy="52" r="8" fill="#1e293b"/>
        </svg>
      </div>
      
      <div class="parking-decoration parking-space">
        <svg viewBox="0 0 80 80" fill="none">
          <rect x="5" y="5" width="70" height="70" rx="8" stroke="#1677ff" stroke-width="3" fill="none"/>
          <rect x="15" y="20" width="50" height="40" rx="4" stroke="#1677ff" stroke-width="2" stroke-dasharray="4 4"/>
          <path d="M30 30 L30 50 M50 30 L50 50" stroke="#1677ff" stroke-width="2"/>
          <circle cx="40" cy="60" r="8" fill="#1677ff"/>
        </svg>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo 区域 -->
      <div class="logo-section">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none">
            <rect x="6" y="4" width="12" height="16" rx="2" fill="currentColor"/>
            <path d="M8 8h8v2H8zM8 12h8v2H8z" fill="white"/>
            <circle cx="12" cy="18" r="1.5" fill="white"/>
          </svg>
          <div class="status-dot"></div>
        </div>
        <h1 class="title">小区停车场管理系统</h1>
        <p class="subtitle">便捷停车 · 智慧管理</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          <a class="forgot-link" href="#" @click.prevent="openForgotDialog">忘记密码?</a>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录 系 统</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册提示 -->
      <div class="register-hint">
        <span>还没有账户?</span>
        <a href="#" @click.prevent="goToRegister">立即注册</a>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog
      v-model="forgotDialogVisible"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="420px"
      class="forgot-password-dialog"
      @closed="resetForgotDialog"
    >
      <!-- 对话框头部 -->
      <template #header>
        <div class="dialog-header">
          <div class="dialog-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <rect x="3" y="10" width="18" height="11" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M7 10V7a5 5 0 0110 0v3" stroke="currentColor" stroke-width="2" fill="none"/>
              <circle cx="12" cy="16" r="1" fill="currentColor"/>
            </svg>
          </div>
          <span class="dialog-title">重置密码</span>
        </div>
      </template>

      <!-- 步骤条 -->
      <el-steps :active="forgotStep" align-center class="forgot-steps" finish-status="success">
        <el-step title="身份验证" />
        <el-step title="设置新密码" />
        <el-step title="完成" />
      </el-steps>

      <div class="dialog-body">
        <!-- 步骤1：身份验证 -->
        <el-form
          v-if="forgotStep === 0"
          ref="verifyFormRef"
          :model="forgotForm"
          :rules="forgotRules"
          label-position="top"
          @keyup.enter="handleVerifyIdentity"
        >
          <div class="step-hint">
            <el-icon><InfoFilled /></el-icon>
            <span>请输入您的用户名和注册时使用的手机号进行身份验证</span>
          </div>
          <el-form-item prop="username" label="用户名">
            <el-input
              v-model="forgotForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="phone" label="注册手机号">
            <el-input
              v-model="forgotForm.phone"
              placeholder="请输入注册时的手机号"
              size="large"
              prefix-icon="Phone"
              clearable
              maxlength="11"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="verifyLoading"
            class="dialog-btn"
            @click="handleVerifyIdentity"
          >
            <span v-if="!verifyLoading">验 证 身 份</span>
            <span v-else>验 证 中...</span>
          </el-button>
        </el-form>

        <!-- 步骤2：设置新密码 -->
        <el-form
          v-if="forgotStep === 1"
          ref="resetFormRef"
          :model="forgotForm"
          :rules="forgotRules"
          label-position="top"
          @keyup.enter="handleResetPassword"
        >
          <div class="step-hint success">
            <el-icon><CircleCheck /></el-icon>
            <span>身份验证通过！请设置您的新密码</span>
          </div>
          <el-form-item prop="newPassword" label="新密码">
            <el-input
              v-model="forgotForm.newPassword"
              type="password"
              placeholder="请输入新密码（6-20个字符）"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword" label="确认新密码">
            <el-input
              v-model="forgotForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="resetLoading"
            class="dialog-btn"
            @click="handleResetPassword"
          >
            <span v-if="!resetLoading">重 置 密 码</span>
            <span v-else>重 置 中...</span>
          </el-button>
        </el-form>

        <!-- 步骤3：完成 -->
        <div v-if="forgotStep === 2" class="complete-section">
          <div class="complete-icon">
            <svg viewBox="0 0 80 80" fill="none">
              <circle cx="40" cy="40" r="36" stroke="#10b981" stroke-width="3" fill="#ecfdf5"/>
              <path d="M25 40L35 50L55 30" stroke="#10b981" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="complete-title">密码重置成功！</h3>
          <p class="complete-desc">您的新密码已设置完成，请使用新密码登录系统</p>
          <el-button
            type="primary"
            size="large"
            class="dialog-btn"
            @click="backToLogin"
          >
            返 回 登 录
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, InfoFilled, CircleCheck } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

// ========== 忘记密码相关 ==========
const forgotDialogVisible = ref(false)
const forgotStep = ref(0)
const verifyLoading = ref(false)
const resetLoading = ref(false)
const verifyFormRef = ref(null)
const resetFormRef = ref(null)

const forgotForm = reactive({
  username: '',
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

const validateForgotPhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入注册手机号'))
  } else if (!reg.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度需为6-20个字符'))
  } else {
    callback()
  }
}

const validateForgotConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const forgotRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validateForgotPhone, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateForgotConfirmPassword, trigger: 'blur' }
  ]
}

const openForgotDialog = () => {
  forgotDialogVisible.value = true
}

const resetForgotDialog = () => {
  forgotStep.value = 0
  forgotForm.username = ''
  forgotForm.phone = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  verifyLoading.value = false
  resetLoading.value = false
  if (verifyFormRef.value) verifyFormRef.value.resetFields()
  if (resetFormRef.value) resetFormRef.value.resetFields()
}

const handleVerifyIdentity = async () => {
  if (!verifyFormRef.value) return
  try {
    await verifyFormRef.value.validate()
  } catch (e) {
    return
  }

  verifyLoading.value = true
  try {
    const response = await axios.post('/api/user/forgotPassword', {
      username: forgotForm.username,
      phone: forgotForm.phone,
      mode: 'verify'
    })
    
    if (response.data.code === 200) {
      forgotStep.value = 1
    } else {
      ElMessage.error(response.data.msg || '身份验证失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data?.msg || '身份验证失败')
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }
  } finally {
    verifyLoading.value = false
  }
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
  } catch (e) {
    return
  }

  resetLoading.value = true
  try {
    const response = await axios.post('/api/user/forgotPassword', {
      username: forgotForm.username,
      phone: forgotForm.phone,
      newPassword: forgotForm.newPassword
    })
    
    if (response.data.code === 200) {
      forgotStep.value = 2
    } else {
      ElMessage.error(response.data.msg || '密码重置失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data?.msg || '请求失败')
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }
  } finally {
    resetLoading.value = false
  }
}

const backToLogin = () => {
  forgotDialogVisible.value = false
  ElMessage.success('请使用新密码登录系统')
}
// ========== 忘记密码相关结束 ==========

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) {
    console.error('表单引用为空')
    return
  }

  try {
    await loginFormRef.value.validate()
  } catch (e) {
    console.log('表单验证失败')
    return
  }

  loading.value = true
  
  try {
    console.log('开始登录，请求参数:', loginForm)
    
    const response = await axios.post('/api/user/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    
    console.log('登录响应:', response.data)
    
    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.data.userInfo || {}))
      
      if (rememberMe.value) {
        localStorage.setItem('username', loginForm.username)
        localStorage.setItem('password', loginForm.password)
      }
      
      ElMessage.success('登录成功')
      router.push('/system/user')
    } else {
      ElMessage.error(response.data.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    console.error('错误响应:', error.response)
    
    if (error.response) {
      ElMessage.error(error.response.data?.msg || '请求失败')
    } else if (error.request) {
      ElMessage.error('无法连接到服务器，请检查后端服务是否启动')
    } else {
      ElMessage.error('登录失败: ' + error.message)
    }
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

const loadRememberedCredentials = () => {
  const rememberedUsername = localStorage.getItem('username')
  const rememberedPassword = localStorage.getItem('password')
  if (rememberedUsername && rememberedPassword) {
    loginForm.username = rememberedUsername
    loginForm.password = rememberedPassword
    rememberMe.value = true
  }
}

loadRememberedCredentials()
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;
@use '../../styles/mixins.scss' as *;

.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0edff 0%, #f0f7ff 100%);
  overflow: hidden;
}

.background {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(231, 233, 234, 0.5) 1px, transparent 1px),
    linear-gradient(90deg, rgba(231, 233, 234, 0.5) 1px, transparent 1px);
  background-size: 60px 60px;
  opacity: 0.6;
}

.gradient {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: float 25s ease-in-out infinite;
}

.gradient-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba($primary-color, 0.15) 0%, transparent 70%);
  top: -15%;
  left: -10%;
}

.gradient-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba($secondary-color, 0.12) 0%, transparent 70%);
  bottom: -15%;
  right: -5%;
  animation-delay: -10s;
}

// 停车场主题装饰元素
.parking-decoration {
  position: absolute;
  opacity: 0.05;
}

.parking-car {
  width: 100px;
  height: 60px;
  top: 20%;
  right: 10%;
  animation: carMove 20s linear infinite;
}

.parking-car svg {
  width: 100%;
  height: 100%;
}

.parking-space {
  width: 80px;
  height: 80px;
  bottom: 25%;
  left: 15%;
  animation: spacePulse 4s ease-in-out infinite;
}

@keyframes carMove {
  0% {
    transform: translateX(-200px) rotate(0deg);
  }
  25% {
    transform: translateX(0) translateY(-30px) rotate(0deg);
  }
  50% {
    transform: translateX(200px) rotate(0deg);
  }
  75% {
    transform: translateX(0) translateY(30px) rotate(0deg);
  }
  100% {
    transform: translateX(-200px) rotate(0deg);
  }
}

@keyframes spacePulse {
  0%, 100% {
    opacity: 0.03;
    transform: scale(1);
  }
  50% {
    opacity: 0.08;
    transform: scale(1.1);
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}

.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: $spacing-2xl $spacing-xl;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: $radius-xl;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.08),
    0 0 60px rgba($primary-color, 0.1);
  animation: cardIn 0.6s ease-out;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: $spacing-xl;
}

.logo {
  position: relative;
  display: inline-flex;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
  border-radius: $radius-lg;
  justify-content: center;
  align-items: center;
  box-shadow: 0 12px 40px rgba($primary-color, 0.3);
  margin-bottom: $spacing-md;
  transition: transform $transition-normal;
}

.logo:hover {
  transform: scale(1.05);
}

.logo svg {
  width: 36px;
  height: 36px;
  color: white;
}

.status-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 14px;
  height: 14px;
  background: $primary-color;
  border-radius: 50%;
  border: 3px solid white;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba($primary-color, 0.5); }
  50% { box-shadow: 0 0 0 8px rgba($primary-color, 0); }
}

.title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin: 0 0 $spacing-sm 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: $font-size-base;
  color: $text-muted;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: $spacing-md;
}

.login-form :deep(.el-input__wrapper) {
  padding: $spacing-md;
  border-radius: $radius-md;
  box-shadow: 0 0 0 1px $border-color;
  transition: all $transition-normal;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px $primary-color;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.2), 0 0 0 1px $primary-color;
}

.login-form :deep(.el-input__inner) {
  font-size: $font-size-md;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.forgot-link {
  font-size: $font-size-base;
  color: $text-secondary;
  text-decoration: none;
  transition: color $transition-fast;
}

.forgot-link:hover {
  color: $primary-color;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  border-radius: $radius-md;
  @include gradient-primary;
  border: none;
  box-shadow: 0 8px 24px rgba($primary-color, 0.3);
  transition: all $transition-normal;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba($primary-color, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.register-hint {
  text-align: center;
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $border-color;
  font-size: $font-size-base;
  color: $text-secondary;
}

.register-hint a {
  color: $primary-color;
  text-decoration: none;
  margin-left: 4px;
  font-weight: $font-weight-medium;
}

.register-hint a:hover {
  text-decoration: underline;
}

// ========== 忘记密码弹窗样式 ==========
.forgot-password-dialog :deep(.el-dialog) {
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.12),
    0 0 60px rgba($primary-color, 0.1);
}

.forgot-password-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px 0;
  border-bottom: none;
}

.forgot-password-dialog :deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

.forgot-password-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  border-radius: $radius-sm;
  transition: all $transition-fast;
}

.forgot-password-dialog :deep(.el-dialog__headerbtn:hover) {
  background: $bg-hover;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  border-radius: $radius-md;
  box-shadow: 0 8px 20px rgba($primary-color, 0.25);
}

.dialog-icon svg {
  width: 22px;
  height: 22px;
  color: white;
}

.dialog-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  letter-spacing: 0.5px;
}

.forgot-steps {
  margin: 20px 0 24px;
  padding: 0 8px;
}

.forgot-steps :deep(.el-step__title) {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.forgot-steps :deep(.el-step__head.is-finish .el-step__icon) {
  background: $success-color;
  border-color: $success-color;
}

.forgot-steps :deep(.el-step__head.is-process .el-step__icon) {
  background: $primary-color;
  border-color: $primary-color;
}

.dialog-body {
  min-height: 200px;
}

.step-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba($primary-color, 0.06) 0%, rgba($primary-light, 0.06) 100%);
  border-radius: $radius-sm;
  border: 1px solid rgba($primary-color, 0.12);
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;

  .el-icon {
    color: $primary-color;
    font-size: $font-size-lg;
    flex-shrink: 0;
  }

  &.success {
    background: linear-gradient(135deg, rgba($success-color, 0.06) 0%, rgba($success-light, 0.06) 100%);
    border-color: rgba($success-color, 0.15);

    .el-icon {
      color: $success-color;
    }
  }
}

.forgot-password-dialog :deep(.el-form-item__label) {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-primary;
  padding-bottom: 6px;
}

.forgot-password-dialog :deep(.el-input__wrapper) {
  border-radius: $radius-md;
  box-shadow: 0 0 0 1px $border-color;
  transition: all $transition-normal;
}

.forgot-password-dialog :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px $primary-color;
}

.forgot-password-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba($primary-color, 0.2), 0 0 0 1px $primary-color;
}

.dialog-btn {
  width: 100%;
  height: 46px;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  border-radius: $radius-md;
  margin-top: 4px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  border: none;
  box-shadow: 0 6px 20px rgba($primary-color, 0.3);
  transition: all $transition-normal;
}

.dialog-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba($primary-color, 0.4);
}

.dialog-btn:active {
  transform: translateY(0);
}

// 完成步骤样式
.complete-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0 8px;
}

.complete-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  animation: completeIn 0.5s ease-out;
}

.complete-icon svg {
  width: 100%;
  height: 100%;
}

@keyframes completeIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.complete-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin: 0 0 8px 0;
}

.complete-desc {
  font-size: $font-size-base;
  color: $text-muted;
  margin: 0 0 24px 0;
  line-height: 1.6;
}

@media (max-width: $breakpoint-xs) {
  .login-card {
    width: calc(100% - 40px);
    padding: $spacing-lg $spacing-md;
    margin: 0 $spacing-md;
  }

  .logo {
    width: 60px;
    height: 60px;
  }

  .title {
    font-size: $font-size-2xl;
  }
}
</style>