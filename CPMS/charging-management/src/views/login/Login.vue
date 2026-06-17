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
          <rect x="5" y="20" width="90" height="35" rx="5" fill="#22c55e"/>
          <rect x="15" y="25" width="25" height="20" rx="3" fill="#ffffff"/>
          <rect x="60" y="25" width="25" height="20" rx="3" fill="#ffffff"/>
          <circle cx="15" cy="52" r="8" fill="#1e293b"/>
          <circle cx="85" cy="52" r="8" fill="#1e293b"/>
        </svg>
      </div>
      
      <div class="parking-decoration parking-space">
        <svg viewBox="0 0 80 80" fill="none">
          <rect x="5" y="5" width="70" height="70" rx="8" stroke="#22c55e" stroke-width="3" fill="none"/>
          <rect x="15" y="20" width="50" height="40" rx="4" stroke="#22c55e" stroke-width="2" stroke-dasharray="4 4"/>
          <path d="M30 30 L30 50 M50 30 L50 50" stroke="#22c55e" stroke-width="2"/>
          <circle cx="40" cy="60" r="8" fill="#22c55e"/>
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
          <a class="forgot-link" href="#">忘记密码?</a>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

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
  background: linear-gradient(135deg, $primary-50 0%, $secondary-light 50%, $primary-100 100%);
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