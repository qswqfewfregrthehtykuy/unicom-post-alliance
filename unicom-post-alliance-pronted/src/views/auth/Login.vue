<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-left">
        <div class="left-content">
          <div class="brand-icon">🇨🇳</div>
          <h2>联通邮政商盟触点系统</h2>
          <p class="brand-desc">高效、便捷、有温度的数字化工作台</p>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-dot"></span>
              <span>多级审核，流程透明</span>
            </div>
            <div class="feature-item">
              <span class="feature-dot"></span>
              <span>佣金管理，数据清晰</span>
            </div>
            <div class="feature-item">
              <span class="feature-dot"></span>
              <span>业务发展，高效协同</span>
            </div>
          </div>
        </div>
        <div class="bg-decoration">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="login-right">
        <div class="form-header">
          <h3>欢迎回来</h3>
          <p>请登录您的账号以继续</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          size="large"
          class="login-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-actions">
          <el-link type="primary" :underline="false" @click="openApplyDialog">
            申请成为社会加盟商发展人 →
          </el-link>
        </div>
      </div>
    </div>

    <!-- 发展人申请弹窗 -->
    <el-dialog
      title="社会加盟商 - 发展人入驻申请"
      v-model="applyDialogVisible"
      width="620px"
      :close-on-click-modal="false"
      @close="resetApplyForm"
      append-to-body
    >
      <el-alert
        title="温馨提示：本通道仅限社会加盟商（自由商铺）申请。网点内部人员请联系所属网点管理员在系统内直接开户。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="applicantName">
              <el-input v-model="applyForm.applicantName" placeholder="请输入您的真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="applicantPhone">
              <el-input v-model="applyForm.applicantPhone" placeholder="请输入常用手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="applyForm.idCard" placeholder="请输入18位身份证号" />
        </el-form-item>

        <el-form-item label="归属组织网点" prop="selectedRegionAndOutlet">
          <el-cascader
            v-model="applyForm.selectedRegionAndOutlet"
            :props="cascaderProps"
            placeholder="请选择所在地市 / 区县 / 归属网点"
            style="width: 100%"
            clearable
          />
        </el-form-item>

        <el-form-item label="商铺名称" prop="shopName">
          <el-input v-model="applyForm.shopName" placeholder="请输入您的加盟商铺/网点名称" />
        </el-form-item>

        <el-form-item label="商铺地址" prop="shopAddress">
          <el-input v-model="applyForm.shopAddress" placeholder="请输入商铺详细物理地址" />
        </el-form-item>

        <el-form-item label="申请原因" prop="applyReason">
          <el-input v-model="applyForm.applyReason" type="textarea" :rows="3" placeholder="请简述申请入驻原因..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleApplySubmit">提 交 申 请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { submitDeveloperApply, getCities, getDistrictsWithOutlets } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// --- 登录表单 ---
const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.handleLogin(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败，请检查用户名或密码')
  } finally {
    loading.value = false
  }
}

// --- 级联选择器 ---
const cascaderProps = {
  lazy: true,
  emitPath: true,
  checkStrictly: false,
  value: 'value',
  label: 'label',
  children: 'children',

  lazyLoad(node, resolve) {
    const { level, value } = node

    if (level === 0) {
      getCities()
        .then(res => {
          const list = res.data || []
          resolve(list.map(item => ({
            value: item.id,
            label: item.cityName || item.name || item.label || item.city || '',
            leaf: false
          })))
        })
        .catch(() => resolve([]))
    } else if (level === 1) {
      getDistrictsWithOutlets(value)
        .then(res => {
          const list = res.data || []
          resolve(list.map(district => ({
            value: district.id,
            label: district.name || district.districtName || district.label || '',
            outlets: district.outlets || [],
            leaf: !(district.outlets && district.outlets.length)
          })))
        })
        .catch(() => resolve([]))
    } else if (level === 2) {
      const outlets = node.data.outlets || []
      resolve(outlets.map(outlet => ({
        value: outlet.id,
        label: outlet.name || outlet.outletName || outlet.label || '',
        leaf: true
      })))
    } else {
      resolve([])
    }
  }
}

// --- 发展人申请 ---
const applyDialogVisible = ref(false)
const submitLoading = ref(false)
const applyFormRef = ref(null)

const applyForm = reactive({
  applicantName: '',
  applicantPhone: '',
  idCard: '',
  developerType: 'FREE_SHOP',
  selectedRegionAndOutlet: [],
  shopName: '',
  shopAddress: '',
  applyReason: ''
})

const applyRules = {
  applicantName: [{ required: true, message: '请输入您的姓名', trigger: 'blur' }],
  applicantPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号格式', trigger: 'blur' }
  ],
  selectedRegionAndOutlet: [{ required: true, message: '请选择归属的组织和网点', trigger: 'change' }],
  shopName: [{ required: true, message: '请输入商铺名称', trigger: 'blur' }],
  shopAddress: [{ required: true, message: '请输入商铺地址', trigger: 'blur' }]
}

const openApplyDialog = () => {
  applyDialogVisible.value = true
}

const resetApplyForm = () => {
  if (applyFormRef.value) {
    applyFormRef.value.resetFields()
  }
}

const handleApplySubmit = () => {
  applyFormRef.value.validate((valid) => {
    if (!valid) return

    submitLoading.value = true

    const [cityId, districtId, outletId] = applyForm.selectedRegionAndOutlet

    const submitPayload = {
      applicantName: applyForm.applicantName,
      applicantPhone: applyForm.applicantPhone,
      idCard: applyForm.idCard,
      developerType: applyForm.developerType,
      cityId: cityId,
      districtId: districtId,
      outletId: outletId,
      shopName: applyForm.shopName,
      shopAddress: applyForm.shopAddress,
      applyReason: applyForm.applyReason
    }

    submitDeveloperApply(submitPayload)
      .then((res) => {
        const applyNo = res.data?.applyNo || '-'
        ElMessageBox.alert(
          `申请提交成功！您的入驻申请单号为: <b>${applyNo}</b>。<br/>我们将会在 <b>1-3个工作日</b> 内完成多级审核，初审密码将以短信形式发送至您的手机，请注意查收。`,
          '提交成功',
          {
            dangerouslyUseHTMLString: true,
            type: 'success',
            confirmButtonText: '我知道了'
          }
        )
        applyDialogVisible.value = false
      })
      .finally(() => {
        submitLoading.value = false
      })
  })
}
</script>

<style scoped>
/* ========== 容器 ========== */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8edf2 100%);
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.06) 0%, transparent 100%);
  pointer-events: none;
}

/* ========== 卡片 ========== */
.login-card {
  display: flex;
  width: 900px;
  min-height: 520px;
  background: #ffffff;
  border-radius: var(--radius-xl);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ========== 左侧品牌区 ========== */
.login-left {
  position: relative;
  width: 420px;
  background: linear-gradient(145deg, #1a2639 0%, #2c3e50 40%, #34495e 100%);
  color: #ffffff;
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.left-content {
  position: relative;
  z-index: 2;
}

.brand-icon {
  font-size: 3rem;
  margin-bottom: 24px;
  display: block;
}

.left-content h2 {
  font-size: 1.8rem;
  margin-bottom: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.3;
}

.brand-desc {
  font-size: 1rem;
  opacity: 0.75;
  line-height: 1.6;
  margin-bottom: 32px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  opacity: 0.85;
}

.feature-dot {
  width: 6px;
  height: 6px;
  background-color: rgba(64, 158, 255, 0.8);
  border-radius: 50%;
  flex-shrink: 0;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.circle {
  position: absolute;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
}

.circle-1 {
  width: 320px;
  height: 320px;
  top: -120px;
  left: -120px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  right: -60px;
}

/* ========== 右侧登录区 ========== */
.login-right {
  flex: 1;
  padding: 60px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 36px;
}

.form-header h3 {
  font-size: 1.5rem;
  color: var(--color-text-primary);
  margin-bottom: 6px;
  font-weight: 700;
}

.form-header p {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
}

.login-form :deep(.el-form-item__label) {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-regular);
  padding-bottom: 4px;
}

.login-form :deep(.el-input__wrapper) {
  height: 44px;
  border-radius: var(--radius-base) !important;
  background-color: #f9fafb;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-base) !important;
  margin-top: 8px;
  letter-spacing: 2px;
}

.login-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* ========== 响应式 ========== */
@media screen and (max-width: 992px) {
  .login-card {
    width: 90%;
    flex-direction: column;
  }

  .login-left {
    width: 100%;
    padding: 40px 32px;
  }

  .left-content h2 {
    font-size: 1.4rem;
  }

  .brand-desc {
    margin-bottom: 20px;
  }

  .feature-list {
    gap: 8px;
  }

  .login-right {
    padding: 36px 32px;
  }
}

@media screen and (max-width: 576px) {
  .login-card {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }

  .login-left {
    padding: 30px 24px;
  }

  .login-right {
    padding: 24px 20px;
  }
}
</style>
