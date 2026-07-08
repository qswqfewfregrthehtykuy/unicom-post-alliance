<template>
  <div class="outlet-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>网点管理</span>
          <el-button type="primary" @click="handleAdd">新建网点</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" @keyup.enter="handleSearch">
          <el-form-item v-if="isProvince" label="所属地市">
            <el-select v-model="searchForm.cityId" placeholder="全部" clearable @change="handleSearch">
              <el-option
                  v-for="city in cityList"
                  :key="city.id"
                  :label="city.name"
                  :value="city.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="isCity" label="所属地市">
            <el-input :value="scopeCityName" disabled style="width: 150px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="网点名称/编码" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="outletCode" label="编码" width="120" />
        <el-table-column prop="outletName" label="名称" width="180" />
        <el-table-column prop="cityName" label="所属地市" width="120" />
        <el-table-column prop="districtName" label="所属区县" width="120" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="managerName" label="负责人" width="100" />
        <el-table-column prop="managerPhone" label="电话" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          @current-change="fetchData"
          @size-change="fetchData"
          layout="total, sizes, prev, pager, next"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @closed="resetForm"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="编码" prop="outletCode">
          <el-input v-model="form.outletCode" placeholder="请输入网点编码" />
        </el-form-item>
        <el-form-item label="名称" prop="outletName">
          <el-input v-model="form.outletName" placeholder="请输入网点名称" />
        </el-form-item>
        <el-form-item v-if="isProvince" label="所属地市" prop="cityId">
          <el-select v-model="form.cityId" placeholder="请选择地市" filterable @change="loadDistricts">
            <el-option
                v-for="city in cityList"
                :key="city.id"
                :label="city.name"
                :value="city.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCity" label="所属地市">
          <el-input :value="scopeCityName" disabled />
        </el-form-item>
        <el-form-item label="所属区县" prop="districtId">
          <el-select v-model="form.districtId" placeholder="请选择区县" filterable>
            <el-option
                v-for="dist in districtList"
                :key="dist.id"
                :label="dist.name"
                :value="dist.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerName">
          <el-input v-model="form.managerName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="managerPhone">
          <el-input v-model="form.managerPhone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="联盟主" prop="allianceMaster">
          <el-input v-model="form.allianceMaster" placeholder="请输入联盟主名称（可选）" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOutletList,
  createOutlet,
  updateOutlet,
  updateOutletStatus,
  deleteOutlet
} from '@/api/outlet.js'
import { getCities, getDistrictsWithOutlets } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const roles = computed(() => authStore.roles || [])
const isProvince = computed(() => roles.value.includes('ROLE_PROVINCE'))
const isCity = computed(() => roles.value.includes('ROLE_CITY'))
const userInfo = computed(() => authStore.userInfo || {})
const scopeCityId = computed(() => userInfo.value.scopeCityId)
const scopeCityName = computed(() => userInfo.value.scopeCityName || '本市')

// 地市列表
const cityList = ref([])
// 区县列表（根据选中的地市动态加载）
const districtList = ref([])

const tableData = ref([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)

const searchForm = reactive({
  cityId: null,
  keyword: ''
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建网点')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  outletCode: '',
  outletName: '',
  cityId: null,
  districtId: null,
  address: '',
  managerName: '',
  managerPhone: '',
  allianceMaster: '',
  remark: ''
})

const rules = {
  outletCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  outletName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  cityId: [{ required: true, message: '请选择地市', trigger: 'change' }],
  districtId: [{ required: true, message: '请选择区县', trigger: 'change' }],
  managerName: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

// 获取地市列表
const fetchCities = async () => {
  try {
    const res = await getCities()
    cityList.value = res.data || []
  } catch { ElMessage.error('加载地市失败') }
}

// 根据地市加载区县（从 sys_district 表查询）
const loadDistricts = async (cityId) => {
  if (!cityId) {
    districtList.value = []
    return
  }
  try {
    const res = await getDistrictsWithOutlets(cityId)
    districtList.value = res.data || []
  } catch { ElMessage.error('加载区县失败') }
}

// 获取网点列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword
    }
    // 地市管理员使用锁定的地市ID，省分管理员使用搜索条件中的地市ID
    if (isCity.value) {
      params.cityId = scopeCityId.value
    } else if (searchForm.cityId) {
      params.cityId = searchForm.cityId
    }
    const res = await getOutletList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNo.value = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.keyword = ''
  // 地市管理员保留锁定的地市
  if (isCity.value) {
    searchForm.cityId = scopeCityId.value
  } else {
    searchForm.cityId = null
  }
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.outletCode = ''
  form.outletName = ''
  form.address = ''
  form.managerName = ''
  form.managerPhone = ''
  form.allianceMaster = ''
  form.remark = ''
  // 地市管理员锁定地市
  if (isCity.value) {
    form.cityId = scopeCityId.value
    form.districtId = null
    loadDistricts(scopeCityId.value)
  } else {
    form.cityId = null
    form.districtId = null
    districtList.value = []
  }
  formRef.value?.resetFields()
}

const handleAdd = () => {
  dialogTitle.value = '新建网点'
  resetForm()
  // 地市管理员预设地市
  if (isCity.value) {
    form.cityId = scopeCityId.value
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑网点'
  Object.assign(form, row)
  // 加载该地市下的区县
  if (form.cityId) {
    loadDistricts(form.cityId)
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    // 地市管理员强制使用管辖地市
    if (isCity.value) {
      form.cityId = scopeCityId.value
    }
    if (form.id) {
      await updateOutlet(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createOutlet(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}网点“${row.outletName}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await updateOutletStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除网点“${row.outletName}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteOutlet(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(async () => {
  if (isCity.value) {
    // 地市管理员：锁定地市，只显示一个城市名供展示，加载该市下的区县
    searchForm.cityId = scopeCityId.value
    form.cityId = scopeCityId.value
    // 构造城市列表（只包含本市），供表单引用
    cityList.value = [{ id: scopeCityId.value, name: scopeCityName.value }]
    await loadDistricts(scopeCityId.value)
    fetchData()
  } else {
    await fetchCities()
    fetchData()
  }
})
</script>

<style scoped>
.outlet-management .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>