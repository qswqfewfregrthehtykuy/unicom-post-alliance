<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="业务类型">
          <el-select v-model="query.businessType" clearable placeholder="全部" style="width: 120px">
            <el-option
              v-for="item in BUSINESS_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="意向状态">
          <el-select v-model="query.leadStatus" clearable placeholder="全部" style="width: 120px">
            <el-option label="待审" value="PENDING" />
            <el-option label="网点通过" value="OUTLET_APPROVED" />
            <el-option label="地市通过" value="CITY_APPROVED" />
            <el-option label="省级通过" value="PROVINCE_APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="转正状态">
          <el-select v-model="query.formalStatus" clearable placeholder="全部" style="width: 120px">
            <el-option label="未提交" value="N/A" />
            <el-option label="待审" value="PENDING" />
            <el-option label="网点通过" value="OUTLET_APPROVED" />
            <el-option label="地市通过" value="CITY_APPROVED" />
            <el-option label="省级通过" value="PROVINCE_APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="创建日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            unlink-panels
            style="width: 240px"
          />
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
          <template #default="{ row }">{{ getBusinessTypeLabel(row.businessType) }}</template>
        </el-table-column>
        <el-table-column prop="developSource" label="发展来源" width="100" align="center">
          <template #default="{ row }">{{ sourceMap[row.developSource] || row.developSource }}</template>
        </el-table-column>
        <el-table-column prop="leadStatus" label="意向状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.leadStatus)" size="small">{{ statusMap[row.leadStatus] || row.leadStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="formalStatus" label="转正状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.formalStatus)" size="small">{{ statusMap[row.formalStatus] || row.formalStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row.id)">
              <el-icon><View /></el-icon> 查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawer" title="订单详情" size="600" destroy-on-close>
      <order-detail :order-id="currentOrderId" v-if="detailDrawer" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { Search, View } from '@element-plus/icons-vue'
import { getOrderList } from '@/api/order'
import OrderDetail from '@/components/OrderDetail.vue'
import { BUSINESS_TYPES, getBusinessTypeLabel } from '@/constants/business'

const sourceMap = {
  SITE_USER: '发展人发展', OUTLET_SELF: '网点自发展', CHANNEL: '渠道合作', ONLINE: '线上推广', OTHER: '其他'
}

const tableLoading = ref(false)
const query = reactive({
  businessType: '', leadStatus: '', formalStatus: '', keyword: '',
  startDate: '', endDate: '', pageNo: 1, pageSize: 20
})
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const detailDrawer = ref(false)
const currentOrderId = ref(null)
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

watch(dateRange, (val) => {
  if (val) { query.startDate = val[0] || ''; query.endDate = val[1] || '' }
  else { query.startDate = ''; query.endDate = '' }
})

const fetchData = async () => {
  tableLoading.value = true
  try {
    const res = await getOrderList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) { /* 已由拦截器处理 */ }
  finally { tableLoading.value = false }
}

const handleSearch = () => { query.pageNo = 1; fetchData() }
const resetQuery = () => {
  query.businessType = ''; query.leadStatus = ''; query.formalStatus = ''; query.keyword = ''
  dateRange.value = []; query.startDate = ''; query.endDate = ''; query.pageNo = 1; fetchData()
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
