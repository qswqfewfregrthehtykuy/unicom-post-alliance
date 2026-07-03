<template>
  <div class="apply-container">
    <el-card shadow="hover" header="📝 发展人申请">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="applicantPhone">
          <el-input v-model="form.applicantPhone" placeholder="用于登录和联系" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18位身份证号码" />
        </el-form-item>

        <el-form-item label="所属地市" prop="cityId">
          <el-select v-model="form.cityId" placeholder="请选择地市" @change="onCityChange" filterable>
            <el-option
                v-for="city in cityOptions"
                :key="city.id"
                :label="city.name"
                :value="city.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属区县" prop="districtId">
          <el-select v-model="form.districtId" placeholder="请选择区县" @change="onDistrictChange" filterable>
            <el-option
                v-for="dist in districtOptions"
                :key="dist.id"
                :label="dist.name"
                :value="dist.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属网点" prop="outletId">
          <el-select v-model="form.outletId" placeholder="请选择网点" filterable>
            <el-option
                v-for="outlet in outletOptions"
                :key="outlet.id"
                :label="outlet.name"
                :value="outlet.id"
            />
          </el-select>
        </el-form-item>

        <!-- 已移除发展人类型选择，固定为自营网点人员 -->

        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="如：XX邮政商盟合作店" />
        </el-form-item>

        <el-form-item label="店铺地址" prop="shopAddress">
          <el-input v-model="form.shopAddress" placeholder="详细地址" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCities, getDistrictsWithOutlets } from '@/api/auth'
import { submitApply } from '@/api/developer'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  applicantName: '',
  applicantPhone: '',
  idCard: '',
  cityId: null,
  districtId: null,
  outletId: null,
  // 固定为自营网点人员，不再由用户选择
  shopName: '',
  shopAddress: ''
})

const cityOptions = ref([])
const districtOptions = ref([])
const outletOptions = ref([])
const districtMap = ref(new Map())

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
  cityId: [{ required: true, message: '请选择地市', trigger: 'change' }],
  districtId: [{ required: true, message: '请选择区县', trigger: 'change' }],
  outletId: [{ required: true, message: '请选择网点', trigger: 'change' }]
}

const loadCities = async () => {
  try {
    const res = await getCities()
    cityOptions.value = res.data || []
  } catch {
    ElMessage.error('加载地市列表失败')
  }
}

const onCityChange = async (cityId) => {
  form.districtId = null
  form.outletId = null
  districtOptions.value = []
  outletOptions.value = []
  if (!cityId) return
  try {
    const res = await getDistrictsWithOutlets(cityId)
    const data = res.data || []
    const dists = []
    const map = new Map()
    data.forEach(item => {
      dists.push({ id: item.id, name: item.name })
      map.set(item.id, item.outlets || [])
    })
    districtOptions.value = dists
    districtMap.value = map
  } catch {
    ElMessage.error('加载区县及网点失败')
  }
}

const onDistrictChange = (districtId) => {
  form.outletId = null
  if (!districtId) {
    outletOptions.value = []
    return
  }
  outletOptions.value = districtMap.value.get(districtId) || []
}

const submitting = ref(false)
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 强制指定发展人类型为自营网点人员
      const submitData = {
        ...form,
        developerType: 'SELF_EMPLOYED'
      }
      await submitApply(submitData)
      ElMessage.success('申请提交成功，请等待审核')
      router.push('/developer/audit')
    } catch (err) {
      ElMessage.error(err.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.cityId = null
  form.districtId = null
  form.outletId = null
  districtOptions.value = []
  outletOptions.value = []
}

onMounted(() => {
  loadCities()
})
</script>

<style scoped>
.apply-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>