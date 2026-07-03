/**
 * 将日期（Date 或 ISO 字符串）格式化为 yyyy-MM-dd
 * @param {Date|string} date
 * @returns {string}
 */
export function formatDate(date) {
    if (!date) return ''
    const d = new Date(date)
    // 如果转换失败，返回空
    if (isNaN(d.getTime())) return ''
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

/**
 * 格式化日期范围数组
 * @param {Array} dateRange - [start, end]
 * @returns {{ startDate: string, endDate: string }}
 */
export function formatDateRange(dateRange) {
    if (!dateRange || dateRange.length < 2) {
        return { startDate: '', endDate: '' }
    }
    // endDate + 1 天，使 BETWEEN 能覆盖结束日全天数据（created_at 为 DATETIME 类型）
    const endDate = new Date(dateRange[1])
    endDate.setDate(endDate.getDate() + 1)
    return {
        startDate: formatDate(dateRange[0]),
        endDate: formatDate(endDate)
    }
}