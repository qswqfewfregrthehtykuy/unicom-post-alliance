<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" label-width="80px" @keyup.enter="handleSearch">
        <el-row :gutter="16">
          <el-col :lg="6" :md="8" :sm="12" :xs="24">
            <el-form-item label="模块">
              <el-input v-model="queryParams.module" placeholder="请输入模块名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :lg="6" :md="8" :sm="12" :xs="24">
            <el-form-item label="操作动作">
              <el-input v-model="queryParams.action" placeholder="请输入操作动作" clearable />
            </el-form-item>
          </el-col>
          <el-col :lg="6" :md="8" :sm="12" :xs="24">
            <el-form-item label="操作人ID">
              <el-input-number v-model="queryParams.userId" :min="1" placeholder="用户ID" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :lg="6" :md="8" :sm="12" :xs="24">
            <el-form-item label="目标类型">
              <el-input v-model="queryParams.targetType" placeholder="目标类型" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :lg="6" :md="8" :sm="12" :xs="24">
            <el-form-item label="操作结果">
              <el-select v-model="queryParams.result" placeholder="操作结果" clearable style="width: 100%;">
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAIL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :lg="12" :md="16" :sm="12" :xs="24">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :lg="6" :md="24" style="display: flex; justify-content: flex-end;">
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%;">
        <el-table-column prop="id" label="日志ID" width="80" align="center" />
        <el-table-column prop="module" label="模块" min-width="120" show-overflow-tooltip />
        <el-table-column prop="action" label="操作动作" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userId" label="操作人ID" width="100" align="center" />
        <el-table-column prop="targetType" label="目标类型" width="120" align="center" />
        <el-table-column prop="targetId" label="目标ID" width="100" align="center" />
        <el-table-column prop="result" label="操作结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" min-width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:page-size="pageSize"
          v-model:current-page="pageNo"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { getLogs } from '@/api/log'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const queryParams = reactive({
  module: '', action: '', userId: null, targetType: '', result: '',
})

const dateRange = ref([])
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = { ...queryParams, pageNo: pageNo.value, pageSize: pageSize.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    } else { delete params.startDate; delete params.endDate }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) delete params[key]
    })
    const res = await getLogs(params)
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.list || []
      total.value = data.total || 0
      pageNo.value = data.pageNo || 1
      pageSize.value = data.pageSize || 20
    } else { ElMessage.error(res.message || '查询失败') }
  } catch (error) {
    console.error('获取操作日志失败', error)
    ElMessage.error('获取操作日志失败')
  } finally { loading.value = false }
}

const handleSearch = () => { pageNo.value = 1; fetchLogs() }
const handleReset = () => {
  queryParams.module = ''; queryParams.action = ''; queryParams.userId = null
  queryParams.targetType = ''; queryParams.result = ''; dateRange.value = []
  pageNo.value = 1; fetchLogs()
}
const handleSizeChange = (val) => { pageSize.value = val; fetchLogs() }
const handleCurrentChange = (val) => { pageNo.value = val; fetchLogs() }

watch(dateRange, () => {})

fetchLogs()
</script>

<style scoped>
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
