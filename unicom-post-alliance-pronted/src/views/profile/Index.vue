<template>
  <div class="profile-container">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">👤 个人信息</span>
        </div>
      </template>

      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="用户名">
          {{ userInfo.username || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ userInfo.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userInfo.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="role in userInfo.roles" :key="role" size="small" type="success" effect="plain" style="margin-right: 5px;">
            {{ roleLabel(role) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据权限范围">
          {{ scopeLabel(userInfo.dataScopeType) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录时间">
          {{ userInfo.lastLoginAt || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="password-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">🔒 修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        style="max-width: 500px;"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少8位，含大小写字母、数字、特殊符号" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleChangePassword">
            确认修改
          </el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发展人信息卡片 -->
    <el-card v-if="isDeveloper" class="developer-card" shadow="hover" v-loading="devLoading">
      <template #header>
        <div class="card-header">
          <span class="card-title">📋 发展人信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="所属网点">
          {{ developerInfo.outletName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="加入日期">
          {{ developerInfo.joinDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="发展人类型">
          <el-tag size="small" type="warning">{{ developerInfo.level || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商铺名称">
          {{ developerInfo.shopName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="累计发展数">
          {{ developerInfo.totalDeveloped || 0 }} 户
        </el-descriptions-item>
        <el-descriptions-item label="累计佣金">
          ¥{{ formatMoney(developerInfo.totalCommission) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { changePassword } from '@/api/auth'

const authStore = useAuthStore()
const passwordFormRef = ref(null)
const submitting = ref(false)

// 用户信息
const userInfo = computed(() => authStore.userInfo || {})

// 是否为发展人
const isDeveloper = computed(() => authStore.roles.includes('ROLE_DEVELOPER'))

// 发展人信息（从 API 获取）
const devLoading = ref(false)
const developerInfo = reactive({
  outletName: '',
  level: '',
  shopName: '',
  joinDate: '',
  totalDeveloped: 0,
  totalCommission: '0.00'
})

const formatMoney = (val) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])/,
      message: '需包含大小写字母、数字、特殊符号',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 修改密码
const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    resetPasswordForm()
    // 退出登录
    setTimeout(() => {
      authStore.handleLogout()
      window.location.href = '/login'
    }, 1500)
  } catch (err) {
    ElMessage.error(err.message || '密码修改失败')
  } finally {
    submitting.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

// 角色标签
const roleLabel = (role) => {
  const map = {
    'ROLE_PROVINCE': '省分管理员',
    'ROLE_CITY': '地市管理员',
    'ROLE_OUTLET': '网点管理员',
    'ROLE_DEVELOPER': '发展人'
  }
  return map[role] || role
}

// 数据权限标签
const scopeLabel = (scope) => {
  const map = {
    'ALL': '全省',
    'CITY': '本市',
    'OUTLET': '本网点',
    'SELF': '仅自己'
  }
  return map[scope] || scope || '-'
}

// 获取发展人信息
const fetchDeveloperInfo = async () => {
  if (!isDeveloper.value) return
  devLoading.value = true
  try {
    const { getDeveloperProfile } = await import('@/api/developer')
    const res = await getDeveloperProfile()
    if (res.data) {
      Object.assign(developerInfo, res.data)
    }
  } catch (err) {
    console.warn('获取发展人信息失败:', err)
  } finally {
    devLoading.value = false
  }
}

onMounted(() => {
  fetchDeveloperInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}
</style>