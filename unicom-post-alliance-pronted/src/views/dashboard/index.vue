<template>
  <div class="dashboard-container">
    <!-- ====== 欢迎横幅 ====== -->
    <el-card shadow="hover" class="welcome-card">
      <div class="welcome-content">
        <h2>👋 欢迎回来，{{ dashboardData.displayName || displayName }}</h2>
        <p>
          当前角色：<el-tag size="small" type="success" effect="plain">{{ dashboardData.roleName || roleLabel }}</el-tag>
          ｜数据范围：{{ dashboardData.scopeName || scopeLabel }}
        </p>
      </div>
    </el-card>

    <!-- ====== 加载/错误状态 ====== -->
    <el-row v-if="loading" :gutter="20" class="card-row">
      <el-col :span="24" style="text-align:center; padding: 60px 0;">
        <p style="color: #94a3b8;">正在加载数据...</p>
      </el-col>
    </el-row>
    <el-row v-else-if="errorMsg" :gutter="20" class="card-row">
      <el-col :span="24">
        <el-alert :title="errorMsg" type="warning" show-icon :closable="false" center />
      </el-col>
    </el-row>

    <!-- ====== 省分管理员 ====== -->
    <template v-if="!loading && isProvince">
      <el-row :gutter="20" class="card-row">
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-blue">
            <div class="card-content">
              <div class="card-label">今日新增发展人</div>
              <div class="card-value">{{ fmt(dashboardData.todayNewDevelopers) }} <span class="unit">人</span></div>
            </div>
            <div class="card-footer">较昨日 <span class="trend-up">{{ dashboardData.developerTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-green">
            <div class="card-content">
              <div class="card-label">本月累计业务</div>
              <div class="card-value">{{ fmt(dashboardData.monthOrderCount) }} <span class="unit">单</span></div>
            </div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.orderTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-orange">
            <div class="card-content">
              <div class="card-label">今日发放佣金</div>
              <div class="card-value"><span class="currency">￥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div>
            </div>
            <div class="card-footer">较昨日 <span class="trend-up">{{ dashboardData.commissionTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-purple">
            <div class="card-content">
              <div class="card-label">全省在线网点</div>
              <div class="card-value">{{ fmt(dashboardData.outletCount) }} <span class="unit">家</span></div>
            </div>
            <div class="card-footer">数据实时更新</div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ====== 地市管理员 ====== -->
    <template v-if="!loading && isCity">
      <el-row :gutter="20" class="card-row">
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-blue">
            <div class="card-content"><div class="card-label">本市发展人总数</div><div class="card-value">{{ fmt(dashboardData.todayNewDevelopers) }} <span class="unit">人</span></div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.developerTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-green">
            <div class="card-content"><div class="card-label">本月本市业务量</div><div class="card-value">{{ fmt(dashboardData.monthOrderCount) }} <span class="unit">单</span></div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.orderTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-orange">
            <div class="card-content"><div class="card-label">本月发放佣金</div><div class="card-value"><span class="currency">￥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.commissionTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="data-card card-purple">
            <div class="card-content"><div class="card-label">本市合作网点</div><div class="card-value">{{ fmt(dashboardData.outletCount) }} <span class="unit">家</span></div></div>
            <div class="card-footer">数据实时更新</div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ====== 网点管理员 ====== -->
    <template v-if="!loading && isOutlet">
      <el-row :gutter="20" class="card-row">
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-blue">
            <div class="card-content"><div class="card-label">本网点发展人</div><div class="card-value">{{ fmt(dashboardData.todayNewDevelopers) }} <span class="unit">人</span></div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.developerTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-green">
            <div class="card-content"><div class="card-label">本月网点业务量</div><div class="card-value">{{ fmt(dashboardData.monthOrderCount) }} <span class="unit">单</span></div></div>
            <div class="card-footer">待审核 <span class="trend-warn">{{ fmt(dashboardData.outletCount) }}笔</span></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-orange">
            <div class="card-content"><div class="card-label">本月发放佣金</div><div class="card-value"><span class="currency">￥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.commissionTrend || '--' }}</span></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ====== 发展人 ====== -->
    <template v-if="!loading && isDeveloper">
      <el-row :gutter="20" class="card-row">
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-blue">
            <div class="card-content"><div class="card-label">我的累计发展</div><div class="card-value">{{ fmt(dashboardData.todayNewDevelopers) }} <span class="unit">户</span></div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.developerTrend || '--' }}</span></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-green">
            <div class="card-content"><div class="card-label">本月业务量</div><div class="card-value">{{ fmt(dashboardData.monthOrderCount) }} <span class="unit">单</span></div></div>
            <div class="card-footer">待审核 <span class="trend-warn">{{ fmt(dashboardData.outletCount) }}笔</span></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="data-card card-orange">
            <div class="card-content"><div class="card-label">累计佣金收入</div><div class="card-value"><span class="currency">￥</span>{{ fmtMoney(dashboardData.todayCommission) }}</div></div>
            <div class="card-footer">趋势 <span class="trend-up">{{ dashboardData.commissionTrend || '--' }}</span></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ====== 图表区：管理层 ====== -->
    <el-row v-if="!loading && isAdminLike" :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover" :header="'📈 业务发展趋势 (' + (dashboardData.scopeName || scopeLabel) + ')'">
          <div id="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" header="📊 核心业务类型占比">
          <div id="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发展人/网点：简化趋势 -->
    <el-row v-if="!loading && isBusinessRole" :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" header="📈 业务发展统计">
          <div id="myTrendChart" class="chart-box-wide"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- TOP10 排行：管理层 -->
    <el-row v-if="!loading && isAdminLike" :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" header="🏆 合作网点发展效能 TOP 10">
          <div id="barChart" style="height:380px;width:100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ====== 审核动态流：管理层 ====== -->
    <el-card v-if="!loading && isAdminLike" shadow="hover" class="table-card" header="⏳ 实时动态审核流监控">
      <el-table :data="recentAuditData" stripe style="width:100%" v-loading="auditLoading">
        <el-table-column prop="applyNo" label="申请单号" width="160" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="developerType" label="发展人类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.developerType === 'FREE_SHOP' ? 'primary' : 'warning'">
              {{ row.developerType === 'FREE_SHOP' ? '社会加盟商' : '自营网点人员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outletName" label="归属网点" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column prop="status" label="当前审核状态" width="140">
          <template #default="{ row }">
            <el-badge is-dot :type="badgeType(row.status)">
              <span class="status-text">{{ row.status }}</span>
            </el-badge>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getDashboard, getBusinessBreakdown, getRecentAudits, getTrend, getRanking } from '@/api/statistics'
import * as echarts from 'echarts'

const authStore = useAuthStore()
const loading = ref(true)
const errorMsg = ref('')
const auditLoading = ref(false)

const dashboardData = reactive({
  todayNewDevelopers: 0, monthOrderCount: 0, todayCommission: 0, outletCount: 0,
  developerTrend: '', orderTrend: '', commissionTrend: '', roleName: '', scopeName: '', displayName: ''
})

// 业务分布数据（来自 API）
const businessBreakdown = ref([])
// 审核动态数据（来自 API）
const recentAuditData = ref([])
// 趋势数据（来自 API）
const trendData = ref([])
// 排行数据（来自 API）
const rankingData = ref([])

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
const scopeLabel = computed(() => isProvince.value ? '全省' : isCity.value ? '本市' : isOutlet.value ? '本网点' : '个人')

const fmt = v => v == null ? '0' : Number(v).toLocaleString()
const fmtMoney = v => v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
const badgeType = s => {
  if (!s) return 'primary'
  if (s.includes('终审') || s.includes('通过')) return 'success'
  if (s.includes('等待') || s.includes('复审')) return 'warning'
  if (s.includes('驳回') || s.includes('拒绝')) return 'danger'
  return 'primary'
}

// ===== 数据加载 =====
const fetchAllData = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    // 并行请求所有数据
    const [dashRes, breakdownRes] = await Promise.all([
      getDashboard().catch(() => ({ data: null })),
      getBusinessBreakdown().catch(() => ({ data: [] }))
    ])
    if (dashRes.data) Object.assign(dashboardData, dashRes.data)
    businessBreakdown.value = breakdownRes.data || []

    // 审核动态（仅管理层）
    if (isAdminLike.value) {
      auditLoading.value = true
      const auditRes = await getRecentAudits().catch(() => ({ data: [] }))
      recentAuditData.value = auditRes.data || []
      auditLoading.value = false
    }

    // 趋势数据
    if (isAdminLike.value || isBusinessRole.value) {
      const today = new Date().toISOString().slice(0, 10)
      const weekAgo = new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10)
      const trendRes = await getTrend({
        granularity: 'DAY', metric: 'DEVELOPMENT_COUNT',
        startDate: weekAgo, endDate: today
      }).catch(() => ({ data: { list: [] } }))
      trendData.value = (trendRes.data && trendRes.data.list) ? trendRes.data.list : []
    }

    // 排行
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
    const c = echarts.init(trendEl)
    const hasTrendData = trendData.value.length > 0
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false,
        data: hasTrendData ? trendData.value.map(d => d.dateTime) : ['06-15','06-17','06-19','06-21','06-23','06-25','06-27','今日']
      },
      yAxis: { type: 'value' },
      series: [{
        name: '交付量', type: 'line', smooth: true,
        data: hasTrendData ? trendData.value.map(d => d.value) : [820,932,901,1234,1290,1530,1820,2135],
        itemStyle: { color: '#2cc094' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[
          { offset:0, color:'rgba(44,192,148,0.4)' },{ offset:1, color:'rgba(44,192,148,0)' }
        ])}
      }]
    })
  }

  // 饼图 - 使用真实业务分布数据
  const pieEl = document.getElementById('pieChart')
  if (pieEl) {
    const c = echarts.init(pieEl)
    const hasData = businessBreakdown.value.length > 0
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0', left: 'center' },
      series: [{
        name: '业务划分', type: 'pie', radius: ['40%','70%'],
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: hasData
          ? businessBreakdown.value.map(d => ({ value: d.count, name: d.businessName }))
          : [{ value:1048,name:'联通号卡发展'},{ value:735,name:'邮政宽带融合'},{ value:580,name:'渠道商盟代理'},{ value:484,name:'触点增值权益'}]
      }]
    })
  }

  // 排行柱状图 - 使用真实排行数据
  const barEl = document.getElementById('barChart')
  if (barEl) {
    const c = echarts.init(barEl)
    const hasData = rankingData.value.length > 0
    c.setOption({
      tooltip: { trigger:'axis', axisPointer:{ type:'shadow' }},
      grid: { left:'4%', right:'6%', bottom:'3%', containLabel:true },
      xAxis: { type:'value', boundaryGap:[0,0.01] },
      yAxis: { type:'category',
        data: hasData
          ? rankingData.value.map(d => d.dimensionName).reverse()
          : ['高新代理点','红谷滩营业厅','莲塘邮政所','西湖合作店','浔阳代办点','青山湖一店','章贡联合厅','月湖代理点','信州触点厅','八一广场网点'].reverse()
      },
      series: [{
        name:'本月产能分', type:'bar',
        data: hasData
          ? rankingData.value.map(d => d.developmentCount).reverse()
          : [120,145,168,192,210,254,290,312,380,563].reverse(),
        itemStyle:{ color: new echarts.graphic.LinearGradient(1,0,0,0,[{ offset:0, color:'#2cc094' },{ offset:1, color:'#a7f3d0' }]), borderRadius:[0,4,4,0] }
      }]
    })
  }
}

const renderBusinessChart = () => {
  const el = document.getElementById('myTrendChart')
  if (!el) return
  const c = echarts.init(el)
  const hasData = trendData.value.length > 0
  const dates = hasData ? trendData.value.map(d => d.dateTime) : ['06-15','06-17','06-19','06-21','06-23','06-25','06-27','今日']
  const vals = hasData ? trendData.value.map(d => d.value) : [3,4,2,5,4,6,5,7]
  c.setOption({
    tooltip: { trigger:'axis' },
    grid: { left:'3%', right:'4%', bottom:'3%', containLabel:true },
    xAxis: { type:'category', boundaryGap:false, data: dates },
    yAxis: { type:'value' },
    series: [{
      name:'业务量', type:'line', smooth:true, data: vals,
      itemStyle:{ color:'#2cc094' },
      areaStyle:{ color: new echarts.graphic.LinearGradient(0,0,0,1,[{ offset:0, color:'rgba(44,192,148,0.4)' },{ offset:1, color:'rgba(44,192,148,0)' }])}
    }]
  })
}

onMounted(fetchAllData)
</script>

<style scoped>
.dashboard-container { display:flex; flex-direction:column; gap:20px; }
.welcome-card { border:none; border-radius:12px; background:linear-gradient(135deg,#f8fafc,#e8f4f8); }
.welcome-content h2 { margin:0 0 8px 0; color:#1e293b; }
.welcome-content p { margin:0; color:#64748b; display:flex; align-items:center; gap:8px; }

.card-row .data-card { color:#fff; border:none; border-radius:12px; }
.card-blue { background:linear-gradient(135deg,#3a7bd5,#3a6073); }
.card-green { background:linear-gradient(135deg,#11998e,#38ef7d); }
.card-orange { background:linear-gradient(135deg,#f12711,#f5af19); }
.card-purple { background:linear-gradient(135deg,#7117ea,#ea6060); }
.card-content { padding:5px 0; }
.card-label { font-size:0.9rem; opacity:0.85; }
.card-value { font-size:1.85rem; font-weight:bold; margin-top:8px; }
.card-value .unit { font-size:0.9rem; font-weight:normal; }
.card-value .currency { font-size:1.3rem; }
.card-footer { margin-top:15px; font-size:0.8rem; padding-top:10px; border-top:1px solid rgba(255,255,255,0.15); opacity:0.9; }
.trend-up { font-weight:bold; color:#fff; }
.trend-warn { font-weight:bold; color:#fde047; }

.chart-box { height:280px; width:100%; }
.chart-box-wide { height:320px; width:100%; }
.table-card { margin-bottom:10px; }
.status-text { margin-left:8px; font-size:0.85rem; color:#475569; }
</style>