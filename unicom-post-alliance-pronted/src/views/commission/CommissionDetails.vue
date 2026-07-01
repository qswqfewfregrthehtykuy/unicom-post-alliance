<template>
  <div class="commission-details-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-total">
          <div class="card-content">
            <div class="card-label">佣金总额</div>
            <div class="card-value">¥ {{ formatMoney(summary.totalAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-lead">
          <div class="card-content">
            <div class="card-label">意向阶段总额</div>
            <div class="card-value">¥ {{ formatMoney(summary.leadTotalAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-formal">
          <div class="card-content">
            <div class="card-label">正式阶段总额</div>
            <div class="card-value">¥ {{ formatMoney(summary.formalTotalAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-settled">
          <div class="card-content">
            <div class="card-label">已结算金额</div>
            <div class="card-value">¥ {{ formatMoney(summary.settledAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-pending">
          <div class="card-content">
            <div class="card-label">待结算金额</div>
            <div class="card-value">¥ {{ formatMoney(summary.pendingAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="summary-card card-frozen">
          <div class="card-content">
            <div class="card-label">冻结金额</div>
            <div class="card-value">¥ {{ formatMoney(summary.frozenAmount) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="日期范围">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="onDateRangeChange"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option label="待结算" value="PENDING" />
            <el-option label="已结算" value="SETTLED" />
            <el-option label="冻结" value="FROZEN" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款方类型">
          <el-select v-model="queryParams.payeeType" placeholder="全部类型" clearable>
            <el-option label="发展人" value="DEVELOPER" />
            <el-option label="网点" value="OUTLET" />
            <el-option label="地市" value="CITY" />
            <el-option label="省级" value="PROVINCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="queryParams.phase" placeholder="全部阶段" clearable>
            <el-option label="意向" value="LEAD" />
            <el-option label="正式" value="FORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="订单号/收款方名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          border
      >
        <el-table-column prop="id" label="明细ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
        <el-table-column prop="commissionPhase" label="阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="row.commissionPhase === 'LEAD' ? 'warning' : 'success'">
              {{ row.commissionPhase === 'LEAD' ? '意向' : '正式' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payeeType" label="收款方类型" width="120">
          <template #default="{ row }">
            {{ getPayeeTypeLabel(row.payeeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="payeeName" label="收款方名称" show-overflow-tooltip />
        <el-table-column prop="businessTypeSnapshot" label="业务类型" show-overflow-tooltip />
        <el-table-column prop="developSourceSnapshot" label="发展来源" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥ {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="ratio" label="分成比例" width="100">
          <template #default="{ row }">
            {{ row.ratio ? (row.ratio * 100).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="实得金额" width="120">
          <template #default="{ row }">
            ¥ {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settleAt" label="结算时间" width="170" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="queryParams.pageNo"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSearch"
            @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getCommissionList, getSummary, exportCommission } from '@/api/commission'

// 查询参数
const queryParams = reactive({
  startDate: '',
  endDate: '',
  status: '',
  payeeType: '',
  phase: '',
  keyword: '',
  pageNo: 1,
  pageSize: 20
})

const dateRange = ref([])

const onDateRangeChange = (val) => {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

// 汇总数据
const summary = reactive({
  totalAmount: 0,
  leadTotalAmount: 0,
  formalTotalAmount: 0,
  settledAmount: 0,
  pendingAmount: 0,
  frozenAmount: 0,
  orderCount: 0,
  detailCount: 0,
  breakdown: { byPayee: [], byPhase: [] }
})

// 列表数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const exportLoading = ref(false)

// 工具：金额格式化
const formatMoney = (val) => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

// 状态映射
const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待结算',
    'SETTLED': '已结算',
    'FROZEN': '冻结'
  }
  return map[status] || status
}
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'SETTLED': 'success',
    'FROZEN': 'danger'
  }
  return map[status] || ''
}
const getPayeeTypeLabel = (type) => {
  const map = {
    'DEVELOPER': '发展人',
    'OUTLET': '网点',
    'CITY': '地市',
    'PROVINCE': '省级',
    'PLATFORM': '平台留存'
  }
  return map[type] || type
}

// 加载汇总
const loadSummary = async () => {
  try {
    const params = { ...queryParams }
    delete params.pageNo
    delete params.pageSize
    const res = await getSummary(params)
    Object.assign(summary, res.data)
  } catch (err) {
    ElMessage.error(err.message || '获取汇总数据失败')
  }
}

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await getCommissionList(params)
    // 🔴 关键修改：后端返回的列表字段是 'list'，不是 'records'
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (err) {
    ElMessage.error(err.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  loadList()
  loadSummary()
}

const resetQuery = () => {
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.status = ''
  queryParams.payeeType = ''
  queryParams.phase = ''
  queryParams.keyword = ''
  queryParams.pageNo = 1
  dateRange.value = []
  handleSearch()
}

// 导出
const handleExport = async () => {
  exportLoading.value = true
  try {
    const params = { ...queryParams }
    delete params.pageNo
    delete params.pageSize
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const response = await exportCommission(params)
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    const filename = `佣金明细_${new Date().toISOString().slice(0,10)}.xlsx`
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (err) {
    ElMessage.error(err.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadSummary()
  loadList()
})
</script>

<style scoped>
/* 样式不变，略（沿用之前的样式） */
.commission-details-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.summary-row {
  margin-bottom: 10px;
}
.summary-card .card-content {
  padding: 5px 0;
}
.summary-card .card-label {
  font-size: 0.85rem;
  color: #64748b;
}
.summary-card .card-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1e293b;
}
.card-total .card-value { color: #3b82f6; }
.card-lead .card-value { color: #f59e0b; }
.card-formal .card-value { color: #10b981; }
.card-settled .card-value { color: #10b981; }
.card-pending .card-value { color: #f59e0b; }
.card-frozen .card-value { color: #ef4444; }
.filter-card {
  flex-shrink: 0;
}
.table-card {
  flex: 1;
}
.pagination-wrap {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style>