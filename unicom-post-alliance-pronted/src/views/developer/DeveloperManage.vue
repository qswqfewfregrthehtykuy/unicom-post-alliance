<template>
  <div class="developer-manage">
    <!-- 搜索筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="网点已批" value="OUTLET_APPROVED" />
            <el-option label="地市已批" value="CITY_APPROVED" />
            <el-option label="省级已批" value="PROVINCE_APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="地市">
          <el-select v-model="queryParams.cityId" placeholder="全部地市" clearable @change="onCityChangeForFilter">
            <el-option
                v-for="city in cityOptions"
                :key="city.id"
                :label="city.name"
                :value="city.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="网点">
          <el-select v-model="queryParams.outletId" placeholder="全部网点" clearable>
            <el-option
                v-for="outlet in filterOutletOptions"
                :key="outlet.outletId"
                :label="outlet.outletName"
                :value="outlet.outletId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="姓名/手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="hover" class="table-card">
      <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          @row-click="handleViewDetail"
          style="cursor: pointer;"
      >
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="applicantName" label="姓名" width="100" />
        <el-table-column prop="applicantPhone" label="手机号" width="120" />
        <el-table-column prop="developerType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag :type="row.developerType === 'FREE_SHOP' ? 'primary' : 'warning'">
              {{ row.developerType === 'FREE_SHOP' ? '社会加盟商' : '自营网点人员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="店铺名称" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click.stop="handleViewDetail(row)">查看</el-button>
            <template v-if="mode === 'audit' && canAudit(row)">
              <el-button size="small" type="success" @click.stop="handleAudit(row, 'APPROVED')">通过</el-button>
              <el-button size="small" type="danger" @click.stop="handleAudit(row, 'REJECTED')">拒绝</el-button>
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
            @size-change="handleSearch"
            @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px" destroy-on-close>
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailData.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailData.applicantPhone }}</el-descriptions-item>
          <el-descriptions-item label="身份证">{{ detailData.idCard }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ detailData.developerType === 'FREE_SHOP' ? '社会加盟商' : '自营网点人员' }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺名称">{{ detailData.shopName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="店铺地址">{{ detailData.shopAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属地市">{{ detailData.cityId }}</el-descriptions-item>
          <el-descriptions-item label="区县">{{ detailData.districtId }}</el-descriptions-item>
          <el-descriptions-item label="网点">{{ detailData.outletId }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detailData.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detailData.reviewerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detailData.reviewAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">{{ detailData.reviewRemark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 审核日志 -->
        <div class="audit-log" v-if="detailData.auditLog && detailData.auditLog.length">
          <h4>审核流程</h4>
          <el-timeline>
            <el-timeline-item
                v-for="(log, idx) in detailData.auditLog"
                :key="idx"
                :timestamp="log.auditTime || '待审核'"
                placement="top"
                :type="log.auditStatus === 'APPROVED' ? 'success' : (log.auditStatus === 'REJECTED' ? 'danger' : 'info')"
            >
              {{ log.auditLevel }} 审核：{{ log.auditStatus === 'APPROVED' ? '通过' : (log.auditStatus === 'REJECTED' ? '拒绝' : '待审核') }}
              <span v-if="log.auditRemark">（{{ log.auditRemark }}）</span>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 审核操作（详情内） -->
        <div class="detail-actions" v-if="mode === 'audit' && canAudit(detailData)">
          <el-divider>审核操作</el-divider>
          <el-input
              v-model="auditRemark"
              placeholder="审核备注（可选）"
              style="width: 300px; margin-right: 10px;"
          />
          <el-button type="success" @click="handleAuditFromDetail('APPROVED')">通过</el-button>
          <el-button type="danger" @click="handleAuditFromDetail('REJECTED')">拒绝</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getCities, getDistrictsWithOutlets } from '@/api/auth'
import { getApplyList, getApplyDetail, auditApply, rejectApply } from '@/api/developer'

const props = defineProps({
  mode: {
    type: String,
    default: 'audit'   // 'audit' 或 'list'
  }
})

const authStore = useAuthStore()

// 当前角色（兼容带或不带 ROLE_ 前缀）
const currentRole = computed(() => {
  const roles = authStore.roles || []
  if (!roles.length) return ''
  let role = roles[0]
  // 如果以 'ROLE_' 开头，去掉前缀
  if (role.startsWith('ROLE_')) {
    role = role.replace('ROLE_', '')
  }
  return role
})

// 查询参数
const queryParams = reactive({
  status: '',
  cityId: null,
  outletId: null,
  keyword: '',
  pageNo: 1,
  pageSize: 20
})

const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 筛选下拉
const cityOptions = ref([])
const filterOutletOptions = ref([])

const loadCities = async () => {
  try {
    const res = await getCities()
    cityOptions.value = res.data || []
  } catch {
    ElMessage.error('加载地市列表失败')
  }
}

const onCityChangeForFilter = async (cityId) => {
  queryParams.outletId = null
  filterOutletOptions.value = []
  if (!cityId) return
  try {
    const res = await getDistrictsWithOutlets(cityId)
    const data = res.data || []
    const allOutlets = []
    data.forEach(item => {
      if (item.outlets) {
        allOutlets.push(...item.outlets)
      }
    })
    filterOutletOptions.value = allOutlets
  } catch {
    ElMessage.error('加载网点列表失败')
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await getApplyList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (err) {
    ElMessage.error(err.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchList()
}

const resetQuery = () => {
  queryParams.status = ''
  queryParams.cityId = null
  queryParams.outletId = null
  queryParams.keyword = ''
  queryParams.pageNo = 1
  filterOutletOptions.value = []
  handleSearch()
}

// 状态映射
const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待审核',
    'OUTLET_APPROVED': '网点已批',
    'CITY_APPROVED': '地市已批',
    'PROVINCE_APPROVED': '省级已批',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'OUTLET_APPROVED': 'info',
    'CITY_APPROVED': 'primary',
    'PROVINCE_APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || ''
}

// 判断当前用户是否可以审核某条记录（角色代码已兼容）
const canAudit = (row) => {
  if (!row || !row.status) return false
  const role = currentRole.value
  if (row.status === 'PENDING' && role === 'OUTLET') return true
  if (row.status === 'OUTLET_APPROVED' && role === 'CITY') return true
  if (row.status === 'CITY_APPROVED' && role === 'PROVINCE') return true
  return false
}

// 获取审核级别
const getAuditLevel = () => {
  const role = currentRole.value
  if (role === 'OUTLET') return 'OUTLET'
  if (role === 'CITY') return 'CITY'
  if (role === 'PROVINCE') return 'PROVINCE'
  return null
}

// 执行审核
const doAudit = async (row, action) => {
  if (!canAudit(row)) {
    ElMessage.warning('您没有权限审核此申请')
    return
  }
  const isReject = action === 'REJECTED'
  const confirmMsg = isReject ? '确认拒绝该申请吗？' : '确认审核通过吗？'
  try {
    await ElMessageBox.confirm(confirmMsg, '审核确认', { type: 'warning' })
  } catch {
    return
  }

  try {
    if (isReject) {
      await rejectApply(row.id, { rejectReason: auditRemark.value || '无' })
      ElMessage.success('拒绝成功')
    } else {
      const level = getAuditLevel()
      if (!level) {
        ElMessage.error('无法确定审核级别')
        return
      }
      await auditApply(row.id, {
        auditLevel: level,
        status: 'APPROVED',
        auditRemark: auditRemark.value || ''
      })
      ElMessage.success('审核通过')
    }
    detailVisible.value = false
    fetchList()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

// 列表操作
const handleAudit = (row, action) => {
  doAudit(row, action)
}

// 详情相关
const detailVisible = ref(false)
const detailData = ref(null)
const auditRemark = ref('')

const handleViewDetail = async (row) => {
  try {
    const res = await getApplyDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
    auditRemark.value = ''
  } catch (err) {
    ElMessage.error(err.message || '获取详情失败')
  }
}

const handleAuditFromDetail = (action) => {
  if (!detailData.value) return
  doAudit(detailData.value, action)
}

onMounted(() => {
  loadCities()
  fetchList()
})
</script>

<style scoped>
.developer-manage {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
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
.detail-content {
  padding: 10px 0;
}
.audit-log {
  margin-top: 20px;
}
.detail-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>