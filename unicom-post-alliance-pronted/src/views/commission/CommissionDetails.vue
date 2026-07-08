<template>
  <div class="page-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">佣金总额</div>
          <div class="summary-value text-blue">¥ {{ formatMoney(summary.totalAmount) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">意向阶段总额</div>
          <div class="summary-value text-orange">¥ {{ formatMoney(summary.leadTotalAmount) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">正式阶段总额</div>
          <div class="summary-value text-green">¥ {{ formatMoney(summary.formalTotalAmount) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">已结算金额</div>
          <div class="summary-value text-green">¥ {{ formatMoney(summary.settledAmount) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">待结算金额</div>
          <div class="summary-value text-orange">¥ {{ formatMoney(summary.pendingAmount) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="summary-card">
          <div class="summary-label">冻结金额</div>
          <div class="summary-value text-red">¥ {{ formatMoney(summary.frozenAmount) }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 查询筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline class="search-form">
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
            <el-option label="平台留存" value="PLATFORM" />
          </el-select>
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="queryParams.commissionPhase" placeholder="全部阶段" clearable>
            <el-option label="意向" value="LEAD" />
            <el-option label="正式" value="FORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="订单号/收款方名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" :icon="Download" @click="handleExport" :loading="exportLoading">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="明细ID" width="80" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
        <el-table-column prop="commissionPhase" label="阶段" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.commissionPhase === 'LEAD' ? 'warning' : 'success'" size="small">
              {{ row.commissionPhase === 'LEAD' ? '意向' : '正式' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payeeType" label="收款方类型" width="120" align="center">
          <template #default="{ row }">{{ getPayeeTypeLabel(row.payeeType) }}</template>
        </el-table-column>
        <el-table-column prop="payeeName" label="收款方名称" show-overflow-tooltip min-width="120" />
        <el-table-column prop="businessTypeSnapshot" label="业务类型" min-width="100" show-overflow-tooltip />
        <el-table-column prop="developSourceSnapshot" label="发展来源" min-width="100" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">¥ {{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="ratio" label="分成比例" width="100" align="center">
          <template #default="{ row }">{{ row.ratio ? (row.ratio * 100).toFixed(2) + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="实得金额" width="120" align="right">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settleAt" label="结算时间" width="170" align="center" />
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { getCommissionList, getSummary, exportCommission } from '@/api/commission'

const queryParams = reactive({
  startDate: '', endDate: '', status: '', payeeType: '', commissionPhase: '',
  keyword: '', pageNo: 1, pageSize: 20
})

const dateRange = ref([])

const onDateRangeChange = (val) => {
  if (val && val.length === 2) { queryParams.startDate = val[0]; queryParams.endDate = val[1] }
  else { queryParams.startDate = ''; queryParams.endDate = '' }
}

const summary = reactive({
  totalAmount: 0, leadTotalAmount: 0, formalTotalAmount: 0,
  settledAmount: 0, pendingAmount: 0, frozenAmount: 0,
  orderCount: 0, detailCount: 0, breakdown: { byPayee: [], byPhase: [] }
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const exportLoading = ref(false)

const formatMoney = (val) => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

const getStatusLabel = (status) => {
  const map = { 'PENDING': '待结算', 'SETTLED': '已结算', 'FROZEN': '冻结' }
  return map[status] || status
}
const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'SETTLED': 'success', 'FROZEN': 'danger' }
  return map[status] || ''
}
const getPayeeTypeLabel = (type) => {
  const map = { 'DEVELOPER': '发展人', 'OUTLET': '网点', 'CITY': '地市', 'PROVINCE': '省级', 'PLATFORM': '平台留存' }
  return map[type] || type
}

const loadSummary = async () => {
  try {
    const params = { ...queryParams }
    delete params.pageNo; delete params.pageSize
    const res = await getSummary(params)
    Object.assign(summary, res.data)
  } catch (err) { ElMessage.error(err.message || '获取汇总数据失败') }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) delete params[key]
    })
    const res = await getCommissionList(params)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (err) { ElMessage.error(err.message || '加载列表失败') }
  finally { loading.value = false }
}

const handleSearch = () => { queryParams.pageNo = 1; loadList(); loadSummary() }
const handlePageChange = () => { loadList() }
const handleSizeChange = () => { queryParams.pageNo = 1; loadList() }
const resetQuery = () => {
  queryParams.startDate = ''; queryParams.endDate = ''; queryParams.status = ''
  queryParams.payeeType = ''; queryParams.commissionPhase = ''; queryParams.keyword = ''
  queryParams.pageNo = 1; dateRange.value = []; handleSearch()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const params = { ...queryParams }
    delete params.pageNo; delete params.pageSize
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) delete params[key]
    })
    const response = await exportCommission(params)
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `佣金明细_${new Date().toISOString().slice(0,10)}.xlsx`
    document.body.appendChild(link); link.click(); document.body.removeChild(link)
    window.URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (err) { ElMessage.error(err.message || '导出失败') }
  finally { exportLoading.value = false }
}

onMounted(() => { loadSummary(); loadList() })
</script>

<style scoped>
.summary-row {
  margin-bottom: 0;
}

.summary-row .el-col {
  margin-bottom: 16px;
}

.summary-card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-lighter);
  box-shadow: var(--shadow-card);
  padding: 20px 24px;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.summary-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.text-blue { color: #409EFF !important; }
.text-orange { color: #E6A23C !important; }
.text-green { color: #67C23A !important; }
.text-red { color: #F56C6C !important; }

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
