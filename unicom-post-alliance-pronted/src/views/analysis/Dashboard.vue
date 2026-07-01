<template>
  <div class="dashboard">
    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filter" label-width="100px">
        <el-form-item label="时间范围">
          <el-date-picker
              v-model="filter.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              @change="onSearch"
          />
        </el-form-item>
        <el-form-item label="统计维度">
          <el-select v-model="filter.dimension" @change="onSearch">
            <el-option label="地市" value="CITY" />
            <el-option label="区县" value="DISTRICT" />
            <el-option label="网点" value="OUTLET" />
            <el-option label="发展人" value="DEVELOPER" />
            <el-option label="日期" value="DATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="filter.businessType" placeholder="可选业务类型" @change="onSearch" />
        </el-form-item>
        <el-form-item label="排行类型">
          <el-select v-model="filter.rankType" @change="fetchRanking">
            <el-option label="网点排行" value="OUTLET" />
            <el-option label="发展人排行" value="DEVELOPER" />
          </el-select>
        </el-form-item>
        <el-form-item label="趋势粒度">
          <el-select v-model="filter.granularity" @change="fetchTrend">
            <el-option label="日" value="DAY" />
            <el-option label="周" value="WEEK" />
            <el-option label="月" value="MONTH" />
          </el-select>
        </el-form-item>
        <el-form-item label="趋势指标">
          <el-select v-model="filter.metric" @change="fetchTrend">
            <el-option label="发展量" value="DEVELOPMENT_COUNT" />
            <el-option label="佣金金额" value="COMMISSION_AMOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计区块 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <span>发展量统计（{{ filter.dimension }}）</span>
          </template>
          <el-table
              :data="developmentData"
              border
              stripe
              max-height="300"
              v-loading="loading.development"
          >
            <el-table-column prop="dimensionName" label="维度名称" />
            <el-table-column prop="developmentCount" label="发展量" align="center" />
            <el-table-column prop="orderCount" label="订单数" align="center" />
          </el-table>
          <div class="total-row">总计：{{ developmentTotal }} 单</div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <span>佣金统计汇总（{{ filter.dimension }}）</span>
          </template>
          <el-table
              :data="commissionData"
              border
              stripe
              max-height="300"
              v-loading="loading.commission"
          >
            <el-table-column prop="dimensionName" label="维度名称" />
            <el-table-column prop="leadAmount" label="意向佣金" align="center" />
            <el-table-column prop="formalAmount" label="转正佣金" align="center" />
            <el-table-column prop="totalAmount" label="合计" align="center" />
          </el-table>
          <div class="total-row">
            总计：意向 {{ commissionTotal.lead }} ｜ 转正 {{ commissionTotal.formal }} ｜ 合计 {{ commissionTotal.total }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <span>发展排行榜（{{ filter.rankType === 'OUTLET' ? '网点' : '发展人' }}）</span>
          </template>
          <el-table
              :data="rankingData"
              border
              stripe
              max-height="300"
              v-loading="loading.ranking"
          >
            <el-table-column label="排名" prop="rank" width="60" align="center" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="developmentCount" label="发展量" align="center" />
            <el-table-column prop="developmentTrend" label="环比趋势" align="center">
              <template #default="{ row }">
                <el-tag :type="row.developmentTrend === '上升' ? 'success' : 'danger'">
                  {{ row.developmentTrend }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <span>趋势分析（{{ filter.metric === 'DEVELOPMENT_COUNT' ? '发展量' : '佣金金额' }}）</span>
          </template>
          <div ref="trendChart" class="chart-container" v-loading="loading.trend"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import {
  getDevelopmentStatistics,
  getCommissionStatistics,
  getRanking,
  getTrend
} from '@/api/statistics'

// -------- 筛选条件 --------
const filter = reactive({
  dateRange: [new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()],
  dimension: 'CITY',
  businessType: '',
  rankType: 'OUTLET',
  granularity: 'DAY',
  metric: 'DEVELOPMENT_COUNT'
})

// -------- 数据状态 --------
const developmentData = ref([])
const developmentTotal = ref(0)
const commissionData = ref([])
const commissionTotal = reactive({ lead: 0, formal: 0, total: 0 })
const rankingData = ref([])
const trendData = ref([])

const loading = reactive({
  development: false,
  commission: false,
  ranking: false,
  trend: false
})

// ECharts 实例
const trendChart = ref(null)
let chartInstance = null

// -------- API 调用 --------
const fetchDevelopment = async () => {
  loading.development = true
  try {
    const { startDate, endDate, dimension, businessType } = getParams()
    const res = await getDevelopmentStatistics({ startDate, endDate, dimension, businessType })
    if (res.code === 200) {
      developmentData.value = res.data.list || []
      developmentTotal.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.development = false
  }
}

const fetchCommission = async () => {
  loading.commission = true
  try {
    const { startDate, endDate, dimension, businessType } = getParams()
    const res = await getCommissionStatistics({ startDate, endDate, dimension, businessType })
    if (res.code === 200) {
      commissionData.value = res.data.list || []
      commissionTotal.lead = res.data.leadAmount || 0
      commissionTotal.formal = res.data.formalAmount || 0
      commissionTotal.total = res.data.totalAmount || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.commission = false
  }
}

const fetchRanking = async () => {
  loading.ranking = true
  try {
    const { startDate, endDate } = formatDateRange(filter.dateRange)
    const params = {
      rankType: filter.rankType,
      startDate,
      endDate,
      limit: 10,
      businessType: filter.businessType || undefined
    }
    const res = await getRanking(params)
    if (res.code === 200) {
      rankingData.value = res.data.list || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.ranking = false
  }
}

const fetchTrend = async () => {
  loading.trend = true
  try {
    const { startDate, endDate } = formatDateRange(filter.dateRange)
    const params = {
      granularity: filter.granularity,
      metric: filter.metric,
      startDate,
      endDate,
      businessType: filter.businessType || undefined
    }
    const res = await getTrend(params)
    if (res.code === 200) {
      trendData.value = res.data.list || []
      renderTrendChart()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.trend = false
  }
}


import { formatDateRange } from '@/utils/date'
// -------- 辅助方法 --------
const getParams = () => {
  const { startDate, endDate } = formatDateRange(filter.dateRange)
  return {
    startDate,
    endDate,
    dimension: filter.dimension,
    businessType: filter.businessType || undefined
  }
}

// 渲染趋势图
const renderTrendChart = () => {
  if (!chartInstance) {
    chartInstance = echarts.init(trendChart.value)
  }
  const xData = trendData.value.map(item => item.timePoint)
  const yData = trendData.value.map(item => item.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: xData,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: yData,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.1 },
      lineStyle: { color: '#2cc094' },
      symbol: 'circle'
    }]
  }
  chartInstance.setOption(option)
  chartInstance.resize()
}

// -------- 事件处理 --------
const onSearch = () => {
  fetchDevelopment()
  fetchCommission()
  fetchRanking()
  fetchTrend()
}

const resetFilter = () => {
  const now = new Date()
  const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
  filter.dateRange = [formatDate(monthAgo), formatDate(now)]
  filter.dateRange = [new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()]
  filter.dimension = 'CITY'
  filter.businessType = ''
  filter.rankType = 'OUTLET'
  filter.granularity = 'DAY'
  filter.metric = 'DEVELOPMENT_COUNT'
  onSearch()
}

// 监听窗口尺寸变化
const handleResize = () => {
  if (chartInstance) chartInstance.resize()
}

// -------- 生命周期 --------
onMounted(() => {
  onSearch()
  window.addEventListener('resize', handleResize)
})

// 销毁时清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 当趋势图容器渲染完毕后初始化
watch(trendChart, (val) => {
  if (val) {
    nextTick(() => {
      if (!chartInstance) {
        chartInstance = echarts.init(val)
      }
      renderTrendChart()
    })
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.filter-card {
  margin-bottom: 20px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  height: 100%;
}
.total-row {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f4f7f6;
  border-radius: 4px;
  font-weight: 500;
  color: #2c3e50;
}
.chart-container {
  width: 100%;
  height: 280px;
}
:deep(.el-card__body) {
  padding: 16px;
}
:deep(.el-table) {
  font-size: 13px;
}
</style>