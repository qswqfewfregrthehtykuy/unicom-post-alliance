<template>
  <div class="dashboard-container">
    <!-- ====== 欢迎横幅 ====== -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>👋 欢迎回来，{{ dashboardData.displayName || displayName }}</h2>
        <div class="welcome-meta">
          <el-tag size="small" effect="plain" :type="roleTagType">{{ dashboardData.roleName || roleLabel }}</el-tag>
          <span class="meta-divider">|</span>
          <span class="meta-scope">数据范围：{{ dashboardData.scopeName || scopeLabel }}</span>
        </div>
      </div>
      <div class="welcome-date">{{ todayDate }}</div>
    </div>

    <!-- ====== 加载状态 ====== -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
      <el-row :gutter="16" style="margin-top: 16px;">
        <el-col v-for="n in 4" :key="n" :span="6">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="card" style="height: 140px;" />
            </template>
          </el-skeleton>
        </el-col>
      </el-row>
    </div>

    <!-- ====== 错误状态 ====== -->
    <el-alert
      v-else-if="errorMsg"
      :title="errorMsg"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 16px;"
    />

    <!-- ====== 统计卡片区 ====== -->
    <template v-if="!loading">
      <!-- 省分管理员：4 卡片 -->
      <el-row v-if="isProvince" :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-blue">
            <div class="stat-card-body">
              <div class="stat-icon-box">
                <el-icon :size="24"><UserFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">今日新增发展人</div>
                <div class="stat-value">{{ fmt(dashboardData.todayNewDevelopers) }}<span class="stat-unit"> 人</span></div>
              </div>
            </div>
            <div class="stat-footer">
              较昨日 <span class="trend-up">↑ {{ dashboardData.developerTrend || '--' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-green">
            <div class="stat-card-body">
              <div class="stat-icon-box">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">本月累计业务</div>
                <div class="stat-value">{{ fmt(dashboardData.monthOrderCount) }}<span class="stat-unit"> 单</span></div>
              </div>
            </div>
            <div class="stat-footer">
              趋势 <span class="trend-up">↑ {{ dashboardData.orderTrend || '--' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-orange">
            <div class="stat-card-body">
              <div class="stat-icon-box">
                <el-icon :size="24"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">今日发放佣金</div>
                <div class="stat-value"><span class="stat-prefix">¥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div>
              </div>
            </div>
            <div class="stat-footer">
              较昨日 <span class="trend-up">↑ {{ dashboardData.commissionTrend || '--' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-purple">
            <div class="stat-card-body">
              <div class="stat-icon-box">
                <el-icon :size="24"><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">全省在线网点</div>
                <div class="stat-value">{{ fmt(dashboardData.outletCount) }}<span class="stat-unit"> 家</span></div>
              </div>
            </div>
            <div class="stat-footer">数据实时更新</div>
          </div>
        </el-col>
      </el-row>

      <!-- 地市管理员：4 卡片 -->
      <el-row v-if="isCity" :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-blue">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><UserFilled /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本市发展人总数</div>
                <div class="stat-value">{{ fmt(dashboardData.todayNewDevelopers) }}<span class="stat-unit"> 人</span></div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.developerTrend || '--' }}</span></div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-green">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Document /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本月本市业务量</div>
                <div class="stat-value">{{ fmt(dashboardData.monthOrderCount) }}<span class="stat-unit"> 单</span></div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.orderTrend || '--' }}</span></div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-orange">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Money /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本月发放佣金</div>
                <div class="stat-value"><span class="stat-prefix">¥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.commissionTrend || '--' }}</span></div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-card stat-purple">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><OfficeBuilding /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本市合作网点</div>
                <div class="stat-value">{{ fmt(dashboardData.outletCount) }}<span class="stat-unit"> 家</span></div>
              </div>
            </div>
            <div class="stat-footer">数据实时更新</div>
          </div>
        </el-col>
      </el-row>

      <!-- 网点管理员：3 卡片 -->
      <el-row v-if="isOutlet" :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-blue">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><UserFilled /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本网点发展人</div>
                <div class="stat-value">{{ fmt(dashboardData.todayNewDevelopers) }}<span class="stat-unit"> 人</span></div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.developerTrend || '--' }}</span></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-green">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Document /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本月网点业务量</div>
                <div class="stat-value">{{ fmt(dashboardData.monthOrderCount) }}<span class="stat-unit"> 单</span></div>
              </div>
            </div>
            <div class="stat-footer">待审核 <span class="trend-warn">{{ fmt(dashboardData.outletCount) }} 笔</span></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-orange">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Money /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本月发放佣金</div>
                <div class="stat-value"><span class="stat-prefix">¥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.commissionTrend || '--' }}</span></div>
          </div>
        </el-col>
      </el-row>

      <!-- 发展人：3 卡片 -->
      <el-row v-if="isDeveloper" :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-blue">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><UserFilled /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">我的累计发展</div>
                <div class="stat-value">{{ fmt(dashboardData.todayNewDevelopers) }}<span class="stat-unit"> 户</span></div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.developerTrend || '--' }}</span></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-green">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Document /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本月业务量</div>
                <div class="stat-value">{{ fmt(dashboardData.monthOrderCount) }}<span class="stat-unit"> 单</span></div>
              </div>
            </div>
            <div class="stat-footer">待审核 <span class="trend-warn">{{ fmt(dashboardData.outletCount) }} 笔</span></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card stat-orange">
            <div class="stat-card-body">
              <div class="stat-icon-box"><el-icon :size="24"><Money /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">累计佣金收入</div>
                <div class="stat-value"><span class="stat-prefix">¥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div>
              </div>
            </div>
            <div class="stat-footer">趋势 <span class="trend-up">↑ {{ dashboardData.commissionTrend || '--' }}</span></div>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- ====== 图表区：管理层 ====== -->
    <el-row v-if="!loading && isAdminLike" :gutter="16" class="chart-row">
      <el-col :lg="14" :md="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 业务发展趋势</span>
              <span class="card-subtitle">{{ dashboardData.scopeName || scopeLabel }}</span>
            </div>
          </template>
          <div id="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :lg="10" :md="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📊 业务类型占比</span>
            </div>
          </template>
          <div id="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发展人/网点：简化趋势 -->
    <el-row v-if="!loading && isBusinessRole" :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 业务发展统计</span>
              <span class="card-subtitle">近14天趋势</span>
            </div>
          </template>
          <div id="myTrendChart" class="chart-box-wide"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- TOP10 排行：管理层 -->
    <el-row v-if="!loading && isAdminLike" :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🏆 合作网点发展效能 TOP 10</span>
              <span class="card-subtitle">本月</span>
            </div>
          </template>
          <div id="barChart" style="height:380px;width:100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ====== 审核动态流：管理层 ====== -->
    <el-card v-if="!loading && isAdminLike" shadow="never" class="table-card" v-loading="auditLoading">
      <template #header>
        <div class="card-header">
          <span>⏳ 实时审核动态监控</span>
          <el-button text size="small" @click="fetchAllData">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <el-table :data="recentAuditData" stripe style="width:100%">
        <el-table-column prop="applyNo" label="申请单号" width="160" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="developerType" label="发展人类型" width="130">
          <template #default="{ row }">
            <el-tag :type="row.developerType === 'FREE_SHOP' ? 'primary' : 'warning'" size="small">
              {{ row.developerType === 'FREE_SHOP' ? '社会加盟商' : '自营网点人员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outletName" label="归属网点" show-overflow-tooltip min-width="140" />
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column prop="status" label="审核状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ====== 开发人员空状态 ====== -->
    <el-empty v-if="!loading && isDeveloper && !errorMsg" description="更多功能持续建设中，敬请期待" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getDashboard, getBusinessBreakdown, getRecentAudits, getTrend, getRanking } from '@/api/statistics'
import { UserFilled, Document, Money, OfficeBuilding, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const authStore = useAuthStore()
const loading = ref(true)
const errorMsg = ref('')
const auditLoading = ref(false)

const dashboardData = reactive({
  todayNewDevelopers: 0, monthOrderCount: 0, todayCommission: 0, outletCount: 0,
  developerTrend: '', orderTrend: '', commissionTrend: '', roleName: '', scopeName: '', displayName: ''
})

const businessBreakdown = ref([])
const recentAuditData = ref([])
const trendData = ref([])
const rankingData = ref([])

// ECharts 实例引用
let trendChart = null
let pieChart = null
let barChart = null
let myTrendChart = null

// 角色判断
const isProvince = computed(() => authStore.roles.includes('ROLE_PROVINCE'))
const isCity = computed(() => authStore.roles.includes('ROLE_CITY'))
const isOutlet = computed(() => authStore.roles.includes('ROLE_OUTLET'))
const isDeveloper = computed(() => authStore.roles.includes('ROLE_DEVELOPER'))
const isAdminLike = computed(() => isProvince.value || isCity.value)
const isBusinessRole = computed(() => isOutlet.value || isDeveloper.value)

const displayName = computed(() => authStore.userInfo?.realName || authStore.userInfo?.username || '用户')
const roleLabel = computed(() => {
  const m = { 'ROLE_PROVINCE': '省分管理员', 'ROLE_CITY': '地市管理员', 'ROLE_OUTLET': '网点管理员', 'ROLE_DEVELOPER': '发展人' }
  for (const r of authStore.roles) if (m[r]) return m[r]
  return '未知角色'
})
const roleTagType = computed(() => {
  const m = { 'ROLE_PROVINCE': 'danger', 'ROLE_CITY': 'warning', 'ROLE_OUTLET': 'success', 'ROLE_DEVELOPER': 'info' }
  for (const r of authStore.roles) if (m[r]) return m[r]
  return 'info'
})
const scopeLabel = computed(() => isProvince.value ? '全省' : isCity.value ? '本市' : isOutlet.value ? '本网点' : '个人')
const todayDate = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))

const fmt = v => v == null ? '0' : Number(v).toLocaleString()
const fmtMoney = v => v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
const statusTagType = (s) => {
  if (!s) return 'info'
  if (s.includes('通过') || s.includes('终审')) return 'success'
  if (s.includes('等待') || s.includes('复审')) return 'warning'
  if (s.includes('驳回') || s.includes('拒绝')) return 'danger'
  return 'info'
}

// ===== 数据加载 =====
const fetchAllData = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const [dashRes, breakdownRes] = await Promise.all([
      getDashboard().catch(() => ({ data: null })),
      getBusinessBreakdown().catch(() => ({ data: [] }))
    ])
    if (dashRes.data) Object.assign(dashboardData, dashRes.data)
    businessBreakdown.value = breakdownRes.data || []

    if (isAdminLike.value) {
      auditLoading.value = true
      const auditRes = await getRecentAudits().catch(() => ({ data: [] }))
      recentAuditData.value = auditRes.data || []
      auditLoading.value = false
    }

    if (isAdminLike.value || isBusinessRole.value) {
      const today = new Date().toISOString().slice(0, 10)
      const weekAgo = new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10)
      const trendRes = await getTrend({
        granularity: 'DAY', metric: 'DEVELOPMENT_COUNT',
        startDate: weekAgo, endDate: today
      }).catch(() => ({ data: { list: [] } }))
      trendData.value = (trendRes.data && trendRes.data.list) ? trendRes.data.list : []
    }

    if (isAdminLike.value) {
      const today = new Date().toISOString().slice(0, 10)
      const monthStart = today.slice(0, 7) + '-01'
      const rankRes = await getRanking({
        rankType: 'OUTLET', startDate: monthStart, endDate: today, limit: 10
      }).catch(() => ({ data: { list: [] } }))
      rankingData.value = (rankRes.data && rankRes.data.list) ? rankRes.data.list : []
    }
  } catch (err) {
    console.error('Dashboard 数据加载失败:', err)
    errorMsg.value = '部分数据加载失败，已显示可用数据'
  } finally {
    loading.value = false
    await nextTick()
    renderCharts()
  }
}

// ===== 图表渲染 =====
const renderCharts = () => {
  if (isAdminLike.value) renderAdminCharts()
  else if (isBusinessRole.value) renderBusinessChart()
}

const renderAdminCharts = () => {
  // 趋势图
  const trendEl = document.getElementById('trendChart')
  if (trendEl) {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendEl)
    const hasData = trendData.value.length > 0
    const fallbackDates = ['06-15','06-17','06-19','06-21','06-23','06-25','06-27','今日']
    const fallbackValues = [820,932,901,1234,1290,1530,1820,2135]
    trendChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' } },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10px', containLabel: true },
      xAxis: {
        type: 'category', boundaryGap: false,
        data: hasData ? trendData.value.map(d => d.dateTime) : fallbackDates,
        axisLine: { lineStyle: { color: '#e4e7ed' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#f2f6fc', type: 'dashed' } },
        axisLabel: { color: '#909399' }
      },
      series: [{
        name: '交付量', type: 'line', smooth: true,
        data: hasData ? trendData.value.map(d => d.value) : fallbackValues,
        symbol: 'circle', symbolSize: 6,
        lineStyle: { color: '#409EFF', width: 3 },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.25)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])
        }
      }]
    })
  }

  // 饼图
  const pieEl = document.getElementById('pieChart')
  if (pieEl) {
    if (pieChart) pieChart.dispose()
    pieChart = echarts.init(pieEl)
    const hasData = businessBreakdown.value.length > 0
    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
    pieChart.setOption({
      color: colors,
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: '0', left: 'center', textStyle: { color: '#606266' } },
      series: [{
        name: '业务划分', type: 'pie', radius: ['45%', '72%'],
        center: ['50%', '43%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: hasData
          ? businessBreakdown.value.map(d => ({ value: d.count, name: d.businessName }))
          : [{ value: 1048, name: '联通号卡发展' }, { value: 735, name: '邮政宽带融合' }, { value: 580, name: '渠道商盟代理' }, { value: 484, name: '触点增值权益' }]
      }]
    })
  }

  // 排行柱状图
  const barEl = document.getElementById('barChart')
  if (barEl) {
    if (barChart) barChart.dispose()
    barChart = echarts.init(barEl)
    const hasData = rankingData.value.length > 0
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '4%', right: '10%', bottom: '3%', top: '10px', containLabel: true },
      xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f2f6fc', type: 'dashed' } } },
      yAxis: {
        type: 'category',
        axisLabel: { color: '#606266' },
        axisLine: { show: false },
        axisTick: { show: false },
        data: hasData ? rankingData.value.map(d => d.name).reverse() : ['高新代理点','红谷滩营业厅','莲塘邮政所','西湖合作店','浔阳代办点','青山湖一店','章贡联合厅','月湖代理点','信州触点厅','八一广场网点'].reverse()
      },
      series: [{
        name: '本月产能分', type: 'bar',
        barWidth: 20,
        data: hasData ? rankingData.value.map(d => d.developmentCount).reverse() : [120,145,168,192,210,254,290,312,380,563].reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409EFF' }, { offset: 1, color: '#a0cfff' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      }]
    })
  }
}

const renderBusinessChart = () => {
  const el = document.getElementById('myTrendChart')
  if (!el) return
  if (myTrendChart) myTrendChart.dispose()
  myTrendChart = echarts.init(el)
  const hasData = trendData.value.length > 0
  const dates = hasData ? trendData.value.map(d => d.dateTime) : ['06-15','06-17','06-19','06-21','06-23','06-25','06-27','今日']
  const vals = hasData ? trendData.value.map(d => d.value) : [3,4,2,5,4,6,5,7]
  myTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10px', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dates, axisLabel: { color: '#909399' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f2f6fc', type: 'dashed' } } },
    series: [{
      name: '业务量', type: 'line', smooth: true, data: vals,
      symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#409EFF', width: 3 },
      itemStyle: { color: '#409EFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.25)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      }
    }]
  })
}

// 窗口 resize 时重绘图表
const handleChartResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  barChart?.resize()
  myTrendChart?.resize()
}

onMounted(() => {
  fetchAllData()
  window.addEventListener('resize', handleChartResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleChartResize)
  trendChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
  myTrendChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 欢迎横幅 ===== */
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-lighter);
  box-shadow: var(--shadow-card);
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.welcome-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.meta-divider {
  color: var(--color-border-base);
}

.welcome-date {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

/* ===== 加载状态 ===== */
.loading-state {
  padding: 0;
}

/* ===== 统计卡片 ===== */
.stat-row {
  margin-bottom: 0;
}

.stat-card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-lighter);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px 16px;
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-blue .stat-icon-box {
  background-color: #ecf5ff;
  color: #409EFF;
}

.stat-green .stat-icon-box {
  background-color: #f0f9eb;
  color: #67C23A;
}

.stat-orange .stat-icon-box {
  background-color: #fdf6ec;
  color: #E6A23C;
}

.stat-purple .stat-icon-box {
  background-color: #f4f0fe;
  color: #8b5cf6;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

.stat-unit {
  font-size: 0.85rem;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.stat-prefix {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-right: 2px;
}

.stat-footer {
  padding: 10px 24px;
  border-top: 1px solid var(--color-border-lighter);
  font-size: 12px;
  color: var(--color-text-secondary);
  background-color: #fafbfc;
}

.trend-up {
  color: #67C23A;
  font-weight: 600;
}

.trend-warn {
  color: #E6A23C;
  font-weight: 600;
}

/* ===== 图表区 ===== */
.chart-row {
  margin-bottom: 0;
}

.chart-row .el-col {
  margin-bottom: 16px;
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 400;
}

.chart-box {
  height: 300px;
  width: 100%;
}

.chart-box-wide {
  height: 320px;
  width: 100%;
}

/* ===== 表格卡片 ===== */
.table-card {
  margin-bottom: 0;
}

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .stat-card-body {
    padding: 16px;
  }

  .stat-value {
    font-size: 1.4rem;
  }

  .chart-box {
    height: 240px;
  }
}
</style>
