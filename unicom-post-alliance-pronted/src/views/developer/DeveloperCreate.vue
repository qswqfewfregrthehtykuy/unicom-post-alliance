<template>
  <div class="create-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>直接创建发展人</span>
          <el-tag type="warning" size="small">跳过审核流程，直接生效</el-tag>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="发展人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="applicantPhone">
          <el-input v-model="form.applicantPhone" placeholder="用于登录账号" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18位身份证号码" />
        </el-form-item>

        <el-form-item label="发展人类型" prop="developerType">
          <el-select v-model="form.developerType" placeholder="请选择类型">
            <el-option label="社会加盟商（自由店铺）" value="FREE_SHOP" />
            <el-option label="自营网点人员" value="SELF_EMPLOYED" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属地市" prop="cityId">
          <el-select v-model="form.cityId" placeholder="请选择地市" @change="onCityChange" filterable>
            <el-option v-for="city in cityOptions" :key="city.id" :label="city.name" :value="city.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属区县" prop="districtId">
          <el-select v-model="form.districtId" placeholder="请选择区县" @change="onDistrictChange" filterable>
            <el-option v-for="dist in districtOptions" :key="dist.id" :label="dist.name" :value="dist.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="归属网点" prop="outletId">
          <el-select v-model="form.outletId" placeholder="请选择归属网点" filterable>
            <el-option v-for="outlet in outletOptions" :key="outlet.id" :label="outlet.name" :value="outlet.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="店铺名称">
          <el-input v-model="form.shopName" placeholder="如：XX邮政商盟合作店" />
        </el-form-item>

        <el-form-item label="店铺地址">
          <el-input v-model="form.shopAddress" placeholder="详细地址" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认创建
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 创建结果对话框（显示临时密码） -->
    <el-dialog v-model="resultVisible" title="创建成功" width="450px" :close-on-click-modal="false">
      <el-alert type="success" :closable="false" show-icon>
        <template #title>发展人账号已创建，请告知发展人以下信息</template>
      </el-alert>
      <el-descriptions :column="1" border style="margin-top: 16px">
        <el-descriptions-item label="登录账号">{{ resultData.username }}</el-descriptions-item>
        <el-descriptions-item label="初始密码">
          <el-tag type="danger" size="large">{{ resultData.tempPassword }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">{{ form.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="角色">发展人（ROLE_DEVELOPER）</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="resultVisible = false">我已知晓</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCities, getDistrictsWithOutlets } from '@/api/auth'
import { createDeveloper } from '@/api/developer'

const formRef = ref(null)
const form = reactive({
  applicantName: '',
  applicantPhone: '',
  idCard: '',
  developerType: '',
  cityId: null,
  districtId: null,
  outletId: null,
  shopName: '',
  shopAddress: ''
})

const rules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  applicantPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  developerType: [{ required: true, message: '请选择发展人类型', trigger: 'change' }],
  cityId: [{ required: true, message: '请选择地市', trigger: 'change' }],
  districtId: [{ required: true, message: '请选择区县', trigger: 'change' }],
  outletId: [{ required: true, message: '请选择归属网点', trigger: 'change' }]
}

const cityOptions = ref([])
const districtOptions = ref([])
const outletOptions = ref([])
const districtMap = ref(new Map())
const submitting = ref(false)
const resultVisible = ref(false)
const resultData = reactive({ username: '', tempPassword: '' })

const loadCities = async () => {
  try {
    const res = await getCities()
    cityOptions.value = res.data || []
  } catch { ElMessage.error('加载地市失败') }
}

const onCityChange = async (cityId) => {
  form.districtId = null; form.outletId = null
  districtOptions.value = []; outletOptions.value = []
  if (!cityId) return
  try {
    const res = await getDistrictsWithOutlets(cityId)
    const data = res.data || []
    const dists = []; const map = new Map()
    data.forEach(item => {
      dists.push({ id: item.id, name: item.name })
      map.set(item.id, item.outlets || [])
    })
    districtOptions.value = dists
    districtMap.value = map
  } catch { ElMessage.error('加载区县失败') }
}

const onDistrictChange = (districtId) => {
  form.outletId = null
  outletOptions.value = districtId ? (districtMap.value.get(districtId) || []) : []
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await createDeveloper({ ...form })
      resultData.username = res.data?.username || form.applicantPhone
      resultData.tempPassword = res.data?.tempPassword || '***'
      resultVisible.value = true
      ElMessage.success('发展人创建成功')
      resetForm()
    } catch (err) {
      ElMessage.error(err.message || '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.cityId = null; form.districtId = null; form.outletId = null
  form.developerType = ''
  districtOptions.value = []; outletOptions.value = []
}

onMounted(() => { loadCities() })
</script>

<style scoped>
.create-container { max-width: 800px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
