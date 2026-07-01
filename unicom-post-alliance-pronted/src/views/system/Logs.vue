<template>
  <div class="log-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" label-width="80px" size="default" @keyup.enter="handleSearch">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="模块">
              <el-input v-model="queryParams.module" placeholder="请输入模块名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作动作">
              <el-input v-model="queryParams.action" placeholder="请输入操作动作" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作人ID">
              <el-input-number v-model="queryParams.userId" :min="1" placeholder="用户ID" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="目标类型">
              <el-input v-model="queryParams.targetType" placeholder="目标类型" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="操作结果">
              <el-select v-model="queryParams.result" placeholder="操作结果" clearable style="width: 100%;">
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAIL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          <el-col :span="6" style="text-align: right;">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%;">
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

// 查询参数
const queryParams = reactive({
  module: '',
  action: '',
  userId: null,
  targetType: '',
  result: '',
})

const dateRange = ref([]) // [startDate, endDate]

// 分页
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 格式化时间
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    }
    // 日期范围处理
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    } else {
      // 如果清除日期范围，删除参数（后端可空）
      delete params.startDate
      delete params.endDate
    }
    // 清理空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const res = await getLogs(params)
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      total.value = data.total || 0
      pageNo.value = data.pageNo || 1
      pageSize.value = data.pageSize || 20
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('获取操作日志失败', error)
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNo.value = 1
  fetchLogs()
}

// 重置
const handleReset = () => {
  queryParams.module = ''
  queryParams.action = ''
  queryParams.userId = null
  queryParams.targetType = ''
  queryParams.result = ''
  dateRange.value = []
  pageNo.value = 1
  fetchLogs()
}

// 分页改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchLogs()
}

const handleCurrentChange = (val) => {
  pageNo.value = val
  fetchLogs()
}

// 监听日期变化（可选）
watch(dateRange, () => {
  // 可以在此处自动搜索，但为了体验，我们留给用户点击查询
})

// 初始化加载
fetchLogs()
</script>

<style scoped>
.log-container {
  padding: 0;
}
.search-card {
  margin-bottom: 20px;
}
.search-card :deep(.el-card__body) {
  padding-bottom: 10px;
}
.table-card :deep(.el-card__body) {
  padding: 20px 20px 10px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>