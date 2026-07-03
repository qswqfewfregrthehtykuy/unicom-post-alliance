<template>
  <div class="org-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>地市管理</span>
          <el-button type="primary" @click="handleAdd">新建地市</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orgCode" label="编码" width="120" />
        <el-table-column prop="orgName" label="名称" width="180" />
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="leaderPhone" label="电话" width="140" />
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
        <el-form-item label="编码" prop="orgCode">
          <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
        </el-form-item>
        <el-form-item label="名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织层级" prop="orgLevel">
          <el-input-number v-model="form.orgLevel" :min="1" :max="4" disabled />
          <span class="form-tip">地市级固定为 2</span>
        </el-form-item>
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" placeholder="请选择类型" disabled>
            <el-option label="地市" value="CITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leaderName">
          <el-input v-model="form.leaderName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="leaderPhone">
          <el-input v-model="form.leaderPhone" placeholder="请输入电话" />
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

// 省级ID（从树中获取）
const provinceId = ref(null)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建地市')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  orgCode: '',
  orgName: '',
  orgLevel: 2,
  orgType: 'CITY',
  parentId: null,
  leaderName: '',
  leaderPhone: '',
  address: '',
  sortOrder: 0,
  remark: ''
})

const rules = {
  orgCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  orgName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  orgLevel: [{ required: true, message: '请选择层级', trigger: 'change' }],
  orgType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// 获取省级ID
const getProvinceId = async () => {
  const res = await getOrgTree(false)
  if (res.data) {
    provinceId.value = res.data.id
    form.parentId = provinceId.value
  }
}

// 获取地市列表
const fetchData = async () => {
  if (!provinceId.value) await getProvinceId()
  loading.value = true
  try {
    const res = await getChildren(provinceId.value, pageNo.value, pageSize.value)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.orgCode = ''
  form.orgName = ''
  form.orgLevel = 2
  form.orgType = 'CITY'
  form.leaderName = ''
  form.leaderPhone = ''
  form.address = ''
  form.sortOrder = 0
  form.remark = ''
  form.parentId = provinceId.value
  formRef.value?.resetFields()
}

// 新建
const handleAdd = () => {
  dialogTitle.value = '新建地市'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑地市'
  Object.assign(form, row)
  form.parentId = provinceId.value
  dialogVisible.value = true
}

// 提交
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

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除地市“${row.orgName}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteOrg(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  getProvinceId().then(fetchData)
})
</script>

<style scoped>
.org-management .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>