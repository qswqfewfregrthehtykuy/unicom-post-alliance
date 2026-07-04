<template>
  <div class="org-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>区县管理</span>
          <div class="header-actions">
            <el-select
                v-model="selectedCityId"
                placeholder="请选择地市"
                @change="handleCityChange"
                clearable
                filterable
            >
              <el-option
                  v-for="city in cityList"
                  :key="city.id"
                  :label="city.orgName"
                  :value="city.id"
              />
            </el-select>
            <el-button type="primary" @click="handleAdd">新建区县</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orgCode" label="编码" width="120" />
        <el-table-column prop="orgName" label="名称" width="180" />
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 对话框同地市，但 orgLevel/orgType 改为 DISTRICT -->
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
        <el-form-item label="所属地市" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择地市" filterable>
            <el-option
                v-for="city in cityList"
                :key="city.id"
                :label="city.orgName"
                :value="city.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="编码" prop="orgCode">
          <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
        </el-form-item>
        <el-form-item label="名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织层级" prop="orgLevel">
          <el-input-number v-model="form.orgLevel" :min="1" :max="4" disabled />
          <span class="form-tip">区县级固定为 3</span>
        </el-form-item>
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" placeholder="请选择类型" disabled>
            <el-option label="区县" value="DISTRICT" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leaderName">
          <el-input v-model="form.leaderName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChildren, createOrg, updateOrg, deleteOrg, getOrgTree } from '@/api/org.js'

// 所有地市列表（用于下拉）
const cityList = ref([])
// 当前选中的地市ID
const selectedCityId = ref(null)

const tableData = ref([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建区县')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  orgCode: '',
  orgName: '',
  orgLevel: 3,
  orgType: 'DISTRICT',
  parentId: null,
  leaderName: '',
  leaderPhone: '',
  address: '',
  sortOrder: 0,
  remark: ''
})

const rules = {
  parentId: [{ required: true, message: '请选择所属地市', trigger: 'change' }],
  orgCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  orgName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  orgLevel: [{ required: true, message: '请选择层级', trigger: 'change' }],
  orgType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// 获取所有地市（省级下的一级子节点）
const fetchCities = async () => {
  const res = await getOrgTree(false)
  if (res.data) {
    const root = res.data
    // 通过 children 获取地市列表（假设树已包含）
    // 但树可能没有展开，可以用 getChildren 获取
    const childrenRes = await getChildren(root.id, 1, 999)
    cityList.value = childrenRes.data.records || []
    if (cityList.value.length) {
      selectedCityId.value = cityList.value[0].id
    }
  }
}

// 获取区县列表
const fetchData = async () => {
  if (!selectedCityId.value) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await getChildren(selectedCityId.value, pageNo.value, pageSize.value)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleCityChange = () => {
  pageNo.value = 1
  fetchData()
}

const resetForm = () => {
  form.id = null
  form.orgCode = ''
  form.orgName = ''
  form.orgLevel = 3
  form.orgType = 'DISTRICT'
  form.parentId = selectedCityId.value
  form.leaderName = ''
  form.leaderPhone = ''
  form.address = ''
  form.sortOrder = 0
  form.remark = ''
  formRef.value?.resetFields()
}

const handleAdd = () => {
  if (!selectedCityId.value) {
    ElMessage.warning('请先选择一个地市')
    return
  }
  dialogTitle.value = '新建区县'
  resetForm()
  form.parentId = selectedCityId.value
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑区县'
  Object.assign(form, row)
  // 确保 parentId 是有效的
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateOrg(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createOrg(form)
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

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除区县“${row.orgName}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteOrg(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchCities().then(() => {
    if (selectedCityId.value) fetchData()
  })
})
</script>

<style scoped>
.org-management .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>