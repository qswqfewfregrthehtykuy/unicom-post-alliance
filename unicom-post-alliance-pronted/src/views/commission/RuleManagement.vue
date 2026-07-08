<template>
  <div class="rule-management">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable>
            <el-option
                v-for="item in businessTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发展来源">
          <el-select v-model="queryParams.developSource" placeholder="全部" clearable>
            <el-option
                v-for="item in developSourceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="佣金阶段">
          <el-select v-model="queryParams.commissionPhase" placeholder="全部" clearable>
            <el-option label="意向段" value="LEAD" />
            <el-option label="转正段" value="FORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="success" @click="openCreateDialog" v-if="isProvinceAdmin">
          <span class="btn-icon">➕</span> 新建规则
        </el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="规则ID" width="80" align="center" />
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="businessType" label="业务类型" width="150">
          <template #default="{ row }">
            {{ getBusinessTypeLabel(row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="developSource" label="发展来源" width="150">
          <template #default="{ row }">
            {{ getDevelopSourceLabel(row.developSource) }}
          </template>
        </el-table-column>
        <el-table-column prop="commissionPhase" label="佣金阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="row.commissionPhase === 'LEAD' ? 'primary' : 'warning'">
              {{ row.commissionPhase === 'LEAD' ? '意向段' : '转正段' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="佣金总额" width="120" align="right">
          <template #default="{ row }">￥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="分配比例" min-width="200">
          <template #default="{ row }">
            <span>网点 {{ (row.outletRatio * 100).toFixed(2) }}%</span>
            <span style="margin:0 6px;">/</span>
            <span>发展人 {{ (row.developerRatio * 100).toFixed(2) }}%</span>
            <span style="margin:0 6px;">/</span>
            <span>平台 {{ (row.platformRatio * 100).toFixed(2) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="expiryDate" label="失效日期" width="120">
          <template #default="{ row }">{{ row.expiryDate || '永久' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <template v-if="isProvinceAdmin">
              <el-button
                  type="warning"
                  link
                  :disabled="row.status === 1"
                  @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                  v-if="row.status === 1"
                  type="danger"
                  link
                  @click="handleDisable(row)"
              >
                停用
              </el-button>
              <el-button
                  v-else
                  type="success"
                  link
                  @click="handleEnable(row)"
              >
                启用
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="queryParams.pageNo"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchList"
            @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="660px"
        destroy-on-close
        @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          label-position="right"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-select v-model="form.businessType" placeholder="请选择业务类型" style="width: 100%">
                <el-option
                    v-for="item in businessTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发展来源" prop="developSource">
              <el-select v-model="form.developSource" placeholder="请选择发展来源" style="width: 100%">
                <el-option
                    v-for="item in developSourceOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="佣金阶段" prop="commissionPhase">
          <el-radio-group v-model="form.commissionPhase">
            <el-radio label="LEAD">意向段</el-radio>
            <el-radio label="FORMAL">转正段</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="佣金总额" prop="totalAmount">
          <el-input-number
              v-model="form.totalAmount"
              :precision="2"
              :step="10"
              :min="0"
              placeholder="请输入总金额"
              style="width: 100%"
          />
        </el-form-item>
        <el-divider content-position="left">分配比例（三者之和必须等于 1.0000）</el-divider>
        <el-form-item label="网点比例" prop="outletRatio">
          <el-input-number
              v-model="form.outletRatio"
              :precision="4"
              :step="0.01"
              :min="0"
              :max="1"
              placeholder="0.0000"
              style="width: 240px"
          />
          <span style="margin-left: 8px; color: #909399;">范围 0~1，例如 0.6000 表示 60%</span>
        </el-form-item>
        <el-form-item label="发展人比例" prop="developerRatio">
          <el-input-number
              v-model="form.developerRatio"
              :precision="4"
              :step="0.01"
              :min="0"
              :max="1"
              placeholder="0.0000"
              style="width: 240px"
          />
          <span style="margin-left: 8px; color: #909399;">范围 0~1，例如 0.3000 表示 30%</span>
        </el-form-item>
        <el-form-item label="平台比例" prop="platformRatio">
          <el-input-number
              v-model="form.platformRatio"
              :precision="4"
              :step="0.01"
              :min="0"
              :max="1"
              placeholder="0.0000"
              style="width: 240px"
          />
          <span style="margin-left: 8px; color: #909399;">范围 0~1，例如 0.1000 表示 10%</span>
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
              v-model="form.effectiveDate"
              type="date"
              placeholder="选择生效日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="失效日期" prop="expiryDate">
          <el-date-picker
              v-model="form.expiryDate"
              type="date"
              placeholder="可选，留空表示永久"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="规则详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="规则ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
            {{ detailData.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规则名称" :span="2">{{ detailData.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ getBusinessTypeLabel(detailData.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="发展来源">{{ getDevelopSourceLabel(detailData.developSource) }}</el-descriptions-item>
        <el-descriptions-item label="佣金阶段">
          {{ detailData.commissionPhase === 'LEAD' ? '意向段' : '转正段' }}
        </el-descriptions-item>
        <el-descriptions-item label="佣金总额">￥{{ detailData.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="网点比例">{{ (detailData.outletRatio * 100).toFixed(2) }}%</el-descriptions-item>
        <el-descriptions-item label="发展人比例">{{ (detailData.developerRatio * 100).toFixed(2) }}%</el-descriptions-item>
        <el-descriptions-item label="平台比例">{{ (detailData.platformRatio * 100).toFixed(2) }}%</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.expiryDate || '永久' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import {
  getCommissionRuleList,
  getCommissionRuleDetail,
  createCommissionRule,
  updateCommissionRule,
  disableCommissionRule,
  enableCommissionRule
} from '@/api/commission'

const authStore = useAuthStore()

// 判断是否为省级管理员
const isProvinceAdmin = computed(() => {
  const roles = authStore.roles || []
  return roles.some(r => r === 'ROLE_PROVINCE' || r === 'PROVINCE')
})

// ========== 下拉选项定义 ==========
import { BUSINESS_TYPES, getBusinessTypeLabel, DEVELOP_SOURCES, getDevelopSourceLabel } from '@/constants/business'

const businessTypeOptions = BUSINESS_TYPES
const developSourceOptions = DEVELOP_SOURCES

// ========== 查询参数 ==========
const queryParams = reactive({
  businessType: '',
  developSource: '',
  commissionPhase: '',
  status: null,
  pageNo: 1,
  pageSize: 20
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// ========== 表单 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('新建规则')
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  ruleName: '',
  businessType: '',
  developSource: '',
  commissionPhase: 'LEAD',
  totalAmount: 0,
  outletRatio: 0,
  developerRatio: 0,
  platformRatio: 0,
  effectiveDate: '',
  expiryDate: '',
  remark: ''
})

// 详情
const detailVisible = ref(false)
const detailData = ref(null)

// 表单校验规则
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  developSource: [{ required: true, message: '请选择发展来源', trigger: 'change' }],
  commissionPhase: [{ required: true, message: '请选择佣金阶段', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入佣金总额', trigger: 'blur' }],
  outletRatio: [
    { required: true, message: '请输入网点比例', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const sum = (form.outletRatio || 0) + (form.developerRatio || 0) + (form.platformRatio || 0)
        if (Math.abs(sum - 1) > 0.0001) {
          callback(new Error('三项比例之和必须等于 1.0000'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  developerRatio: [{ required: true, message: '请输入发展人比例', trigger: 'blur' }],
  platformRatio: [{ required: true, message: '请输入平台比例', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// ========== API 交互 ==========
const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] === null || params[k] === undefined) {
        delete params[k]
      }
    })
    const res = await getCommissionRuleList(params)
    const pageData = res.data || {}
    tableData.value = pageData.list || []
    total.value = pageData.total || 0
  } catch (err) {
    ElMessage.error('加载规则列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchList()
}

const resetSearch = () => {
  queryParams.businessType = ''
  queryParams.developSource = ''
  queryParams.commissionPhase = ''
  queryParams.status = null
  queryParams.pageNo = 1
  fetchList()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.ruleName = ''
  form.businessType = ''
  form.developSource = ''
  form.commissionPhase = 'LEAD'
  form.totalAmount = 0
  form.outletRatio = 0
  form.developerRatio = 0
  form.platformRatio = 0
  form.effectiveDate = ''
  form.expiryDate = ''
  form.remark = ''
  formRef.value?.resetFields()
}

// 新建
const openCreateDialog = () => {
  dialogTitle.value = '新建佣金规则'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const openEditDialog = (row) => {
  dialogTitle.value = '编辑佣金规则'
  Object.assign(form, {
    id: row.id,
    ruleName: row.ruleName,
    businessType: row.businessType,
    developSource: row.developSource,
    commissionPhase: row.commissionPhase,
    totalAmount: row.totalAmount,
    outletRatio: row.outletRatio,
    developerRatio: row.developerRatio,
    platformRatio: row.platformRatio,
    effectiveDate: row.effectiveDate,
    expiryDate: row.expiryDate,
    remark: row.remark
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const data = { ...form }
    delete data.id
    if (form.id) {
      await updateCommissionRule(form.id, data)
      ElMessage.success('规则更新成功')
    } else {
      await createCommissionRule(data)
      ElMessage.success('规则创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await getCommissionRuleDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch {
    ElMessage.error('获取详情失败')
  }
}

// 停用
const handleDisable = (row) => {
  ElMessageBox.confirm(`确定要停用规则“${row.ruleName}”吗？`, '确认停用', {
    type: 'warning'
  })
      .then(async () => {
        await disableCommissionRule(row.id)
        ElMessage.success('规则已停用')
        fetchList()
      })
      .catch(() => {})
}

// 启用
const handleEnable = (row) => {
  ElMessageBox.confirm(`确定要启用规则“${row.ruleName}”吗？`, '确认启用', {
    type: 'info'
  })
      .then(async () => {
        await enableCommissionRule(row.id)
        ElMessage.success('规则已启用')
        fetchList()
      })
      .catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.rule-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card {
  flex-shrink: 0;
}
.action-bar {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
}
.table-card {
  flex: 1;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.btn-icon {
  margin-right: 4px;
}
</style>