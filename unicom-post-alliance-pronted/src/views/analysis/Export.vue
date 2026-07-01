<template>
  <div class="export-page">
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
          />
        </el-form-item>
        <el-form-item label="统计维度">
          <el-select v-model="filter.dimension">
            <el-option label="地市" value="CITY" />
            <el-option label="区县" value="DISTRICT" />
            <el-option label="网点" value="OUTLET" />
            <el-option label="发展人" value="DEVELOPER" />
            <el-option label="日期" value="DATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="filter.businessType" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleExport('development')" :loading="loading.development">
            导出发展量统计
          </el-button>
          <el-button type="success" @click="handleExport('commission')" :loading="loading.commission">
            导出佣金统计
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 导出预览/说明 -->
    <el-card shadow="hover">
      <div class="export-tips">
        <el-alert
            title="导出说明"
            type="info"
            :closable="false"
            show-icon
        >
          <template #default>
            <ul>
              <li>导出数据基于当前筛选条件生成。</li>
              <li>发展量统计包含各维度的发展量、订单数等。</li>
              <li>佣金统计包含意向佣金、转正佣金、合计等。</li>
              <li>导出文件为 <code>.xlsx</code> 格式，可直接用 Excel 打开。</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'
import {
  getDevelopmentStatistics,
  getCommissionStatistics
} from '@/api/statistics'

// 筛选条件
const filter = reactive({
  dateRange: [
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
    new Date()
  ],
  dimension: 'CITY',
  businessType: ''
})

const loading = reactive({
  development: false,
  commission: false
})

// 获取格式化后的日期参数
const getDateParams = () => {
  const [startDate, endDate] = filter.dateRange || ['', '']
  return {
    startDate,
    endDate,
    dimension: filter.dimension,
    businessType: filter.businessType || undefined
  }
}

// 通用导出方法
const handleExport = async (type) => {
  const params = getDateParams()
  const loadingKey = type === 'development' ? 'development' : 'commission'
  const apiFn = type === 'development' ? getDevelopmentStatistics : getCommissionStatistics
  const fileName = type === 'development' ? '发展量统计' : '佣金统计'

  loading[loadingKey] = true
  try {
    const res = await apiFn(params)
    if (res.code !== 200) {
      ElMessage.error(res.msg || '数据获取失败')
      return
    }
    const list = res.data.list || []
    if (!list.length) {
      ElMessage.warning('当前条件下无数据可导出')
      return
    }

    // 将数据转换为二维数组（表头 + 数据行）
    let header, dataRows
    if (type === 'development') {
      header = ['维度名称', '发展量', '订单数']
      dataRows = list.map(item => [
        item.dimensionName || '',
        item.developmentCount ?? 0,
        item.orderCount ?? 0
      ])
    } else { // commission
      header = ['维度名称', '意向佣金', '转正佣金', '合计']
      dataRows = list.map(item => [
        item.dimensionName || '',
        item.leadAmount ?? 0,
        item.formalAmount ?? 0,
        item.totalAmount ?? 0
      ])
    }

    // 构建工作表数据
    const wsData = [header, ...dataRows]
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.aoa_to_sheet(wsData)

    // 自动列宽（简单估算）
    const colWidths = header.map((h, colIdx) => {
      let maxLen = h.length
      dataRows.forEach(row => {
        const val = String(row[colIdx] ?? '')
        if (val.length > maxLen) maxLen = val.length
      })
      return { wch: Math.min(Math.max(maxLen + 4, 12), 30) }
    })
    ws['!cols'] = colWidths

    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')

    // 生成 Excel 文件并下载
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([wbout], { type: 'application/octet-stream' })
    const now = new Date()
    const dateStr =
        now.getFullYear() +
        String(now.getMonth() + 1).padStart(2, '0') +
        String(now.getDate()).padStart(2, '0')
    saveAs(blob, `${fileName}_${dateStr}.xlsx`)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    loading[loadingKey] = false
  }
}
</script>

<style scoped>
.export-page {
  padding: 0;
}
.filter-card {
  margin-bottom: 20px;
}
.export-tips ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}
.export-tips li {
  line-height: 1.8;
}
code {
  background: #f4f4f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}
</style>