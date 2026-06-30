<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="card-row">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-blue">
          <div class="card-content">
            <div class="card-label">今日新增发展人</div>
            <div class="card-value">52 <span class="unit">人</span></div>
          </div>
          <div class="card-footer">较昨日 <span class="trend-up">↑ 12%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-green">
          <div class="card-content">
            <div class="card-label">本月累计业务</div>
            <div class="card-value">2,135 <span class="unit">单</span></div>
          </div>
          <div class="card-footer">达成率 <span class="trend-up">85.4%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-orange">
          <div class="card-content">
            <div class="card-label">今日发放佣金</div>
            <div class="card-value"><span class="currency">￥</span>16,320</div>
          </div>
          <div class="card-footer">较昨日 <span class="trend-up">↑ 8.5%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-purple">
          <div class="card-content">
            <div class="card-label">全省在线网点</div>
            <div class="card-value">216 <span class="unit">家</span></div>
          </div>
          <div class="card-footer">网点活跃度 <span class="trend-normal">94.2%</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover" header="📈 业务发展趋势 (近半月)">
          <div id="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" header="📊 核心业务类型占比">
          <div id="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="map-rank-row">
      <el-col :span="14">
        <el-card shadow="hover" class="map-card">
          <template #header>
            <div class="map-header">
              <span>🗺️ 江西省多级触点业务地图 (点击地市联动展示)</span>
              <el-tag type="success" effect="dark">当前选中: {{ currentCityInfo.name }}</el-tag>
            </div>
          </template>
          <el-row>
            <el-col :span="15">
              <div id="jxMapChart" style="height: 400px; width: 100%;"></div>
            </el-col>
            <el-col :span="9">
              <div class="city-detail-panel">
                <h4>🎯 {{ currentCityInfo.name }}数据指标</h4>
                <div class="detail-item">
                  <span class="label">业务交付总量:</span>
                  <el-statistic :value="currentCityInfo.bizCount" />
                </div>
                <div class="detail-item">
                  <span class="label">活跃发展人:</span>
                  <el-statistic :value="currentCityInfo.devCount" />
                </div>
                <div class="detail-item">
                  <span class="label">合作网点数量:</span>
                  <el-statistic :value="currentCityInfo.outletCount" />
                </div>
                <p class="map-hint">*提示：在左侧地图上点击不同区块，此处数据将实时重载</p>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="hover" header="🏆 全省合作网点发展效能 TOP 10">
          <div id="barChart" style="height: 455px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="table-card" header="⏳ 触点系统实时动态审核流监控">
      <el-table :data="recentAuditData" stripe style="width: 100%">
        <el-table-column prop="applyNo" label="申请单号" width="160" />
        <el-table-column prop="name" label="申请人" width="100" />
        <el-table-column prop="type" label="发展人类型" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'FREE_SHOP' ? 'primary' : 'warning'">
              {{ scope.row.type === 'FREE_SHOP' ? '社会加盟商' : '自营网点人员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="归属区域/网点" show-overflow-tooltip />
        <el-table-column prop="time" label="提交申请时间" width="170" />
        <el-table-column prop="status" label="当前审核级状态" width="140">
          <template #default="scope">
            <el-badge is-dot :type="getBadgeType(scope.row.status)">
              <span class="status-text">{{ scope.row.status }}</span>
            </el-badge>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'

// 3. 地图点击多级联动状态数据
const currentCityInfo = reactive({
  name: '全江西省',
  bizCount: 2135,
  devCount: 684,
  outletCount: 216
})

// 地市字典假数据模拟点击切换
const cityMockData = {
  '南昌市': { name: '南昌市', bizCount: 563, devCount: 216, outletCount: 34 },
  '九江市': { name: '九江市', bizCount: 320, devCount: 110, outletCount: 22 },
  '赣州市': { name: '赣州市', bizCount: 412, devCount: 145, outletCount: 45 },
  '上饶市': { name: '上饶市', bizCount: 210, devCount: 82, outletCount: 31 },
  '吉安市': { name: '吉安市', bizCount: 185, devCount: 54, outletCount: 26 }
}

// 4. 底部最新审核表格监控数据
const recentAuditData = ref([
  { applyNo: 'SQ202606290001', name: '张志强', type: 'FREE_SHOP', region: '南昌市东湖区八一广场邮政商盟合作触点网点', time: '2026-06-29 09:12', status: '网点一审通过' },
  { applyNo: 'SQ202606290002', name: '李美琳', type: 'FREE_SHOP', region: '九江市浔阳区火车站邮政触点合作店', time: '2026-06-29 08:45', status: '等待地市复审' },
  { applyNo: 'SQ202606280015', name: '王和平', type: 'FREE_SHOP', region: '赣州市章贡区长征大道商盟联合服务点', time: '2026-06-28 17:30', status: '省级终审完毕' },
  { applyNo: 'SQ202606280012', name: '赵国庆', type: 'FREE_SHOP', region: '南昌市青山湖区江西师大邮政商盟触点营业厅', time: '2026-06-28 15:20', status: '驳回重填' }
])

const getBadgeType = (status) => {
  if (status.includes('终审') || status.includes('通过')) return 'success'
  if (status.includes('等待') || status.includes('复审')) return 'warning'
  if (status.includes('驳回')) return 'danger'
  return 'primary'
}

onMounted(() => {
  // --- 图表 A: 近半月业务趋势折线图 ---
  const trendChart = echarts.init(document.getElementById('trendChart'))
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: ['06-15', '06-17', '06-19', '06-21', '06-23', '06-25', '06-27', '今日'] },
    yAxis: { type: 'value' },
    series: [{
      name: '交付量', type: 'line', smooth: true,
      data: [820, 932, 901, 1234, 1290, 1530, 1820, 2135],
      itemStyle: { color: '#2cc094' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(44,192,148,0.4)' }, { offset: 1, color: 'rgba(44,192,148,0)' }]) }
    }]
  })

  // --- 图表 B: 业务类型占比饼图 ---
  const pieChart = echarts.init(document.getElementById('pieChart'))
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0', left: 'center' },
    series: [{
      name: '业务划分', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      data: [
        { value: 1048, name: '联通号卡发展' },
        { value: 735, name: '邮政宽带融合' },
        { value: 580, name: '渠道商盟代理' },
        { value: 484, name: '触点增值权益' }
      ]
    }]
  })

  // --- 图表 C: 江西省地图散点仿真组件 ---
  // 这里采用标准仿Echarts-Map经纬度气泡模式，无需外部大体积GeoJson依赖，确保100%首屏渲染成功率
  const jxMapChart = echarts.init(document.getElementById('jxMapChart'))
  jxMapChart.setOption({
    backgroundColor: '#f8fafc',
    title: { text: '江西省触点分布热度图', left: 'center', textStyle: { fontSize: 13, color: '#64748b' } },
    tooltip: { trigger: 'item' },
    xAxis: { show: false, min: 0, max: 100 },
    yAxis: { show: false, min: 0, max: 100 },
    series: [
      {
        name: '业务触点热度',
        type: 'scatter',
        symbolSize: function (val) { return val[2] / 4; },
        data: [
          [50, 75, 216, '南昌市'],
          [48, 88, 120, '九江市'],
          [52, 28, 180, '赣州市'],
          [78, 68, 140, '上饶市'],
          [42, 45, 110, '吉安市']
        ],
        label: {
          show: true,
          formatter: function (param) { return param.data[3]; },
          position: 'insideTop',
          color: '#1e293b',
          fontWeight: 'bold'
        },
        itemStyle: {
          color: '#38bdf8',
          shadowBlur: 10,
          shadowColor: 'rgba(56, 189, 248, 0.5)'
        }
      }
    ]
  })

  // 核心点击联动事件监听
  jxMapChart.on('click', function (params) {
    const cityName = params.data[3]
    if (cityMockData[cityName]) {
      Object.assign(currentCityInfo, cityMockData[cityName])
    }
  })

  // --- 图表 D: TOP10 网点效能柱状图 ---
  const barChart = echarts.init(document.getElementById('barChart'))
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '4%', right: '6%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', boundaryGap: [0, 0.01] },
    yAxis: { type: 'category', data: ['高新代理点', '红谷滩营业厅', '莲塘邮政所', '西湖合作店', '浔阳代办点', '青山湖一店', '章贡联合厅', '月湖代理点', '信州触点厅', '八一广场网点'].reverse() },
    series: [{
      name: '本月产能分',
      type: 'bar',
      data: [120, 145, 168, 192, 210, 254, 290, 312, 380, 563].reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: '#2cc094' }, { offset: 1, color: '#a7f3d0' }]),
        borderRadius: [0, 4, 4, 0]
      }
    }]
  })

  // 监听窗口缩放，图表自适应响应式
  window.addEventListener('resize', () => {
    trendChart.resize()
    pieChart.resize()
    jxMapChart.resize()
    barChart.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 顶部统计卡片美化 */
.card-row .data-card {
  color: #ffffff;
  border: none;
  border-radius: 12px;
}
.card-blue { background: linear-gradient(135deg, #3a7bd5, #3a6073); }
.card-green { background: linear-gradient(135deg, #11998e, #38ef7d); }
.card-orange { background: linear-gradient(135deg, #f12711, #f5af19); }
.card-purple { background: linear-gradient(135deg, #7117ea, #ea6060); }

.card-content {
  padding: 5px 0;
}
.card-label {
  font-size: 0.9rem;
  opacity: 0.85;
}
.card-value {
  font-size: 1.85rem;
  font-weight: bold;
  margin-top: 8px;
}
.card-value .unit { font-size: 0.9rem; font-weight: normal; }
.card-value .currency { font-size: 1.3rem; }

.card-footer {
  margin-top: 15px;
  font-size: 0.8rem;
  padding-top: 10px;
  border-top: 1px solid rgba(255,255,255,0.15);
  opacity: 0.9;
}
.trend-up { font-weight: bold; color: #ffffff; }
.trend-normal { font-weight: bold; }

/* 图表容器 */
.chart-box {
  height: 280px;
  width: 100%;
}

/* 地图联动面板区 */
.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.city-detail-panel {
  background-color: #f8fafc;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  height: 360px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.city-detail-panel h4 {
  margin-top: 0;
  color: #334155;
  border-bottom: 2px solid #2cc094;
  padding-bottom: 8px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-item .label {
  font-size: 0.85rem;
  color: #64748b;
}
.map-hint {
  font-size: 0.75rem;
  color: #94a3b8;
  margin: 0;
  line-height: 1.4;
}

/* 底部监控列表 */
.table-card {
  margin-bottom: 10px;
}
.status-text {
  margin-left: 8px;
  font-size: 0.85rem;
  color: #475569;
}
</style>