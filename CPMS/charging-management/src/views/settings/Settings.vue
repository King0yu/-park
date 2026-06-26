<template>
  <div class="settings-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><Setting /></el-icon>
        <h2>停车场系统配置</h2>
      </div>
      <el-tag v-if="!isSuperAdmin" type="info" effect="plain">只读模式</el-tag>
      <el-tag v-else type="success" effect="plain">可编辑</el-tag>
    </div>

    <!-- 系统信息展示 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>当前配置概览</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="系统名称">{{ configForm.systemName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版权标语">{{ configForm.copyright || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 系统配置表单 -->
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Edit /></el-icon>
          <span>配置编辑</span>
        </div>
      </template>
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="100px"
        class="settings-form"
      >
        <el-form-item label="系统名称" prop="systemName">
          <el-input
            v-model="configForm.systemName"
            placeholder="请输入停车场系统名称"
            clearable
            :disabled="!isSuperAdmin"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="版权标语" prop="copyright">
          <el-input
            v-model="configForm.copyright"
            placeholder="请输入版权标语"
            clearable
            :disabled="!isSuperAdmin"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="isSuperAdmin">
          <el-button type="primary" :loading="saveLoading" @click="handleSave">
            <el-icon><Check /></el-icon> 保存配置
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
        <el-form-item v-else>
          <el-alert
            title="仅超级管理员可修改系统配置"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, InfoFilled, Edit, Check, RefreshRight } from '@element-plus/icons-vue'
import { getParkConfig, updateParkConfig } from '@/api/system.js'

const configFormRef = ref(null)
const saveLoading = ref(false)
const originalConfig = reactive({
  systemName: '',
  copyright: ''
})

const configForm = reactive({
  systemName: '',
  copyright: ''
})

const currentRole = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return Number(user.role)
    } catch (e) {
      console.error('解析用户角色失败:', e)
    }
  }
  return null
})

const isSuperAdmin = computed(() => currentRole.value === 0)

const configRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { max: 50, message: '系统名称长度不能超过50个字符', trigger: 'blur' }
  ],
  copyright: [
    { required: true, message: '请输入版权标语', trigger: 'blur' },
    { max: 100, message: '版权标语长度不能超过100个字符', trigger: 'blur' }
  ]
}

const loadConfig = async () => {
  try {
    const res = await getParkConfig()
    const data = res.data || {}
    configForm.systemName = data.systemName || ''
    configForm.copyright = data.copyright || ''
    originalConfig.systemName = configForm.systemName
    originalConfig.copyright = configForm.copyright
  } catch (error) {
    console.error('加载系统配置失败:', error)
  }
}

const handleSave = async () => {
  if (!configFormRef.value) return

  try {
    await configFormRef.value.validate()
  } catch (e) {
    return
  }

  saveLoading.value = true
  try {
    await updateParkConfig({
      systemName: configForm.systemName,
      copyright: configForm.copyright
    })
    ElMessage.success('停车场系统配置保存成功')
    originalConfig.systemName = configForm.systemName
    originalConfig.copyright = configForm.copyright
  } catch (error) {
    console.error('保存系统配置失败:', error)
  } finally {
    saveLoading.value = false
  }
}

const handleReset = () => {
  configForm.systemName = originalConfig.systemName
  configForm.copyright = originalConfig.copyright
  configFormRef.value?.clearValidate()
}

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
@use '../../styles/variables.scss' as *;

.settings-container {
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

.settings-form {
  max-width: 560px;
}
</style>
