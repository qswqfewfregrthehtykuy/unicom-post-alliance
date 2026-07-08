<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="意向状态">
          <el-select v-model="query.leadStatus" clearable placeholder="全部">
            <el-option label="待审" value="PENDING" />
            <el-option label="网点通过" value="OUTLET_APPROVED" />
            <el-option label="地市通过" value="CITY_APPROVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="query.businessType" clearable placeholder="全部">
            <el-option
              v-for="item in BUSINESS_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" min-width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="businessType" label="业务类型" width="100" align="center">
          <template #default="{ row }">{{ bizMap[row.businessType] }}</template>
        </el-table-column>
        <el-table-column prop="leadStatus" label="意向状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.leadStatus)" size="small">{{ statusMap[row.leadStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row.id)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button link type="success" size="small" :disabled="!canAudit(row)" @click="openAudit(row)">
              <el-icon><Select /></el-icon> 审核
            </el-button>
            <el-button link type="danger" size="small" :disabled="!canAudit(row)" @click="openReject(row)">
              <el-icon><CloseBold /></el-icon> 驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialog" title="意向单审核" width="500px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核级别">
          <el-input :value="auditLevel" disabled />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialog" title="驳回意向单" width="450px" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawer" title="订单详情" size="600" destroy-on-close>
      <order-detail :order-id="currentOrderId" v-if="detailDrawer" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, View, Select, CloseBold } from '@element-plus/icons-vue'
import { getOrderList, leadAudit, rejectOrder } from '@/api/order'
import { useAuthStore } from '@/store/auth'
import OrderDetail from '@/components/OrderDetail.vue'
import { BUSINESS_TYPES, getBusinessTypeLabel } from '@/constants/business'

const authStore = useAuthStore()
const tableLoading = ref(false)

// 根据当前角色设置默认审核状态
const defaultLeadStatus = computed(() => {
  const role = authStore.roles?.[0] || ''
  if (role === 'ROLE_OUTLET') return 'PENDING'
  if (role === 'ROLE_CITY') return 'OUTLET_APPROVED'
  if (role === 'ROLE_PROVINCE') return 'CITY_APPROVED'
  return ''
})

const query = reactive({
  businessType: '', leadStatus: defaultLeadStatus.value, keyword: '', pageNo: 1, pageSize: 20
})
const tableData = ref([])
const total = ref(0)

const auditDialog = ref(false)
const auditLoading = ref(false)
const auditForm = reactive({ status: 'APPROVED', auditRemark: '' })
const currentRow = ref(null)

const rejectDialog = ref(false)
const rejectLoading = ref(false)
const rejectReason = ref('')

const detailDrawer = ref(false)
const currentOrderId = ref(null)

const bizMap = Object.fromEntries(BUSINESS_TYPES.map(item => [item.value, item.label]))
const statusMap = {
  PENDING: '待审', OUTLET_APPROVED: '网点通过', CITY_APPROVED: '地市通过',
  PROVINCE_APPROVED: '省级通过', REJECTED: '已驳回', 'N/A': '未提交'
}
const statusTag = (val) => {
  if (val === 'REJECTED') return 'danger'
  if (val === 'PROVINCE_APPROVED') return 'success'
  if (val === 'PENDING') return 'warning'
  return 'info'
}

const auditLevel = computed(() => {
  const role = authStore.roles?.[0] || ''
  if (role === 'ROLE_OUTLET') return 'OUTLET'
  if (role === 'ROLE_CITY') return 'CITY'
  if (role === 'ROLE_PROVINCE') return 'PROVINCE'
  return ''
})

const canAudit = (row) => {
  const status = row.leadStatus
  if (status === 'REJECTED' || status === 'PROVINCE_APPROVED') return false
  const level = auditLevel.value
  if (level === 'OUTLET' && status === 'PENDING') return true
  if (level === 'CITY' && status === 'OUTLET_APPROVED') return true
  if (level === 'PROVINCE' && status === 'CITY_APPROVED') return true
  return false
}

const fetchData = async () => {
  tableLoading.value = true
  try {
    const res = await getOrderList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) { /* 已拦截 */ }
  finally { tableLoading.value = false }
}

const handleSearch = () => { query.pageNo = 1; fetchData() }
const resetQuery = () => {
  query.businessType = ''; query.leadStatus = defaultLeadStatus.value; query.keyword = ''; query.pageNo = 1; fetchData()
}

const openAudit = (row) => { currentRow.value = row; auditForm.status = 'APPROVED'; auditForm.auditRemark = ''; auditDialog.value = true }

const submitAudit = async () => {
  auditLoading.value = true
  try {
    await leadAudit(currentRow.value.id, { auditLevel: auditLevel.value, status: auditForm.status, auditRemark: auditForm.auditRemark })
    ElMessage.success('审核操作成功')
    auditDialog.value = false
    fetchData()
  } finally { auditLoading.value = false }
}

const openReject = (row) => { currentRow.value = row; rejectReason.value = ''; rejectDialog.value = true }

const submitReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请填写驳回原因'); return }
  rejectLoading.value = true
  try {
    await rejectOrder(currentRow.value.id, { auditPhase: 'LEAD', rejectReason: rejectReason.value })
    ElMessage.success('驳回成功')
    rejectDialog.value = false
    fetchData()
  } finally { rejectLoading.value = false }
}

const showDetail = (id) => { currentOrderId.value = id; detailDrawer.value = true }

fetchData()
</script>

<style scoped>
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
