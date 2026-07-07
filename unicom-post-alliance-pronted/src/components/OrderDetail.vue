<template>
  <div v-loading="loading" class="order-detail">
    <el-descriptions :column="2" border v-if="detail">
      <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="业务类型">
        {{ bizTypeMap[detail.businessType] || detail.businessType }}
      </el-descriptions-item>
      <el-descriptions-item label="发展来源">
        {{ developSourceMap[detail.developSource] || detail.developSource }}
      </el-descriptions-item>
      <el-descriptions-item label="客户姓名">{{ detail.customerName }}</el-descriptions-item>
      <el-descriptions-item label="客户手机">{{ detail.customerPhone }}</el-descriptions-item>
      <el-descriptions-item label="身份证号">{{ detail.customerIdCard || '-' }}</el-descriptions-item>
      <el-descriptions-item label="客户地址">{{ detail.customerAddress || '-' }}</el-descriptions-item>
      <el-descriptions-item label="意向单状态">
        <el-tag :type="statusTag(detail.leadStatus)">
          {{ leadStatusMap[detail.leadStatus] || detail.leadStatus }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="转正状态">
        <el-tag :type="statusTag(detail.formalStatus)">
          {{ formalStatusMap[detail.formalStatus] || detail.formalStatus }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 审核日志（待后端支持） -->
    <div v-if="detail && (detail.leadAuditLog || detail.formalAuditLog)" style="margin-top: 20px;">
      <el-divider>审核日志</el-divider>
      <el-timeline>
        <el-timeline-item
            v-for="(log, idx) in (detail.leadAuditLog || [])"
            :key="'lead-' + idx"
            :timestamp="log.auditTime"
            placement="top"
        >
          <span v-if="log.auditStatus === 'APPROVED'" style="color:#67C23A;">✅ 通过</span>
          <span v-else style="color:#F56C6C;">❌ 拒绝</span>
          【{{ log.level }}】{{ log.auditorName }} - {{ log.auditRemark || '无备注' }}
        </el-timeline-item>
        <el-timeline-item
            v-for="(log, idx) in (detail.formalAuditLog || [])"
            :key="'formal-' + idx"
            :timestamp="log.auditTime"
            placement="top"
        >
          <span v-if="log.auditStatus === 'APPROVED'" style="color:#67C23A;">✅ 通过</span>
          <span v-else style="color:#F56C6C;">❌ 拒绝</span>
          【{{ log.level }}】{{ log.auditorName }} - {{ log.auditRemark || '无备注' }}
        </el-timeline-item>
      </el-timeline>
    </div>

    <el-empty v-if="!detail && !loading" description="暂无数据" />
  </div>
</template>

<script setup>
import { ref, watch, defineProps } from 'vue'
import { getOrderDetail } from '@/api/order'
import { BUSINESS_TYPES, getBusinessTypeLabel, DEVELOP_SOURCES, getDevelopSourceLabel } from '@/constants/business'

// 字典映射 — 从统一常量构建
const bizTypeMap = Object.fromEntries(BUSINESS_TYPES.map(item => [item.value, item.label]))
const developSourceMap = Object.fromEntries(DEVELOP_SOURCES.map(item => [item.value, item.label]))
const leadStatusMap = {
  PENDING: '待审',
  OUTLET_APPROVED: '网点通过',
  CITY_APPROVED: '地市通过',
  PROVINCE_APPROVED: '省级通过',
  REJECTED: '已驳回'
}
const formalStatusMap = {
  'N/A': '未提交',
  PENDING: '待审',
  OUTLET_APPROVED: '网点通过',
  CITY_APPROVED: '地市通过',
  PROVINCE_APPROVED: '省级通过',
  REJECTED: '已驳回'
}
const statusTag = (val) => {
  if (val === 'REJECTED') return 'danger'
  if (val === 'PROVINCE_APPROVED') return 'success'
  if (val === 'PENDING') return 'warning'
  return 'info'
}

const props = defineProps({
  orderId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const detail = ref(null)

const fetchDetail = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    const res = await getOrderDetail(props.orderId)
    console.log('详情响应:', res)  // 调试用
    // 兼容两种可能：res 是完整 Result 或 已解包为 data
    detail.value = res.data || res || null
    console.log('赋值后的 detail:', detail.value)
  } catch (error) {
    detail.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.orderId, fetchDetail, { immediate: true })
</script>

<style scoped>
.order-detail {
  padding: 10px;
}
:deep(.el-descriptions) {
  margin-bottom: 10px;
}
</style>