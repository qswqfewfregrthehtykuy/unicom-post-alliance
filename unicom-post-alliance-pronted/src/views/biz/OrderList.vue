<template>
  <div class="order-list">
    <el-card shadow="never">
      <!-- 筛选栏 -->
      <el-form :inline="true" :model="query" class="filter-form" size="default">
        <el-form-item label="业务类型">
          <el-select v-model="query.businessType" clearable placeholder="全部">
            <el-option label="宽带" value="BROADBAND" />
            <el-option label="号卡" value="SIM_CARD" />
            <el-option label="智家" value="SMART_HOME" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向状态">
          <el-select v-model="query.leadStatus" clearable placeholder="全部">
            <el-option label="待审" value="PENDING" />
            <el-option label="网点通过" value="OUTLET_APPROVED" />
            <el-option label="地市通过" value="CITY_APPROVED" />
            <el-option label="省级通过" value="PROVINCE_APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="转正状态">
          <el-select v-model="query.formalStatus" clearable placeholder="全部">
            <el-option label="未提交" value="N/A" />
            <el-option label="待审" value="PENDING" />
            <el-option label="网点通过" value="OUTLET_APPROVED" />
            <el-option label="地市通过" value="CITY_APPROVED" />
            <el-option label="省级通过" value="PROVINCE_APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable />
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
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="120" />
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            {{ bizTypeMap[row.businessType] || row.businessType }}
          </template>
        </el-table-column>
        <el-table-column prop="developSource" label="发展来源" width="100">
          <template #default="{ row }">
            {{ sourceMap[row.developSource] || row.developSource }}
          </template>
        </el-table-column>
        <el-table-column prop="leadStatus" label="意向状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.leadStatus)">{{ statusMap[row.leadStatus] || row.leadStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="formalStatus" label="转正状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.formalStatus)">{{ statusMap[row.formalStatus] || row.formalStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
    <el-drawer v-model="detailDrawer" title="订单详情" :size="600" destroy-on-close>
      <order-detail :order-id="currentOrderId" v-if="detailDrawer" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderList } from '@/api/order'
import OrderDetail from '@/components/OrderDetail.vue'  // 自行创建，见下文

const query = reactive({
  businessType: '',
  leadStatus: '',
  formalStatus: '',
  keyword: '',
  startDate: '',
  endDate: '',
  pageNo: 1,
  pageSize: 20
})
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const detailDrawer = ref(false)
const currentOrderId = ref(null)

// 字典映射
const bizTypeMap = { BROADBAND: '宽带', SIM_CARD: '号卡', SMART_HOME: '智家' }
const sourceMap = { STORE: '门店', ONLINE: '线上', COMMUNITY: '社区' }
const statusMap = {
  PENDING: '待审',
  OUTLET_APPROVED: '网点通过',
  CITY_APPROVED: '地市通过',
  PROVINCE_APPROVED: '省级通过',
  REJECTED: '已驳回',
  'N/A': '未提交'
}
const statusTag = (val) => {
  if (val === 'REJECTED') return 'danger'
  if (val === 'PROVINCE_APPROVED') return 'success'
  if (val === 'PENDING') return 'warning'
  return 'info'
}

// 监听日期范围
watch(dateRange, (val) => {
  if (val) {
    query.startDate = val[0] || ''
    query.endDate = val[1] || ''
  } else {
    query.startDate = ''
    query.endDate = ''
  }
})

const fetchData = async () => {
  try {
    const res = await getOrderList(query)
    // 数据在 res.data 中
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    // 已由拦截器处理
  }
}

const handleSearch = () => {
  query.pageNo = 1
  fetchData()
}

const resetQuery = () => {
  query.businessType = ''
  query.leadStatus = ''
  query.formalStatus = ''
  query.keyword = ''
  dateRange.value = []
  query.startDate = ''
  query.endDate = ''
  query.pageNo = 1
  fetchData()
}

const showDetail = (id) => {
  currentOrderId.value = id
  detailDrawer.value = true
}

fetchData()
</script>

<style scoped>
.order-list {
  padding: 10px;
}
.filter-form {
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>