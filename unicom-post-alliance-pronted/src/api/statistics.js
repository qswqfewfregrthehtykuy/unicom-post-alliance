import request from '@/utils/request'

/**
 * 8.1 发展量统计
 * @param {Object} params - 查询参数
 * @param {string} params.dimension - CITY|DISTRICT|OUTLET|DEVELOPER|DATE
 * @param {number} [params.cityId] - 地市ID（维度为CITY时可选）
 * @param {number} [params.outletId] - 网点ID
 * @param {string} [params.businessType] - 业务类型
 * @param {string} params.startDate - yyyy-MM-dd
 * @param {string} params.endDate - yyyy-MM-dd
 */
export function getDevelopmentStatistics(params) {
    return request({
        url: '/api/v1/statistics/development',
        method: 'get',
        params
    })
}

/**
 * 8.2 佣金统计汇总
 * 参数同 getDevelopmentStatistics
 */
export function getCommissionStatistics(params) {
    return request({
        url: '/api/v1/statistics/commission',
        method: 'get',
        params
    })
}

/**
 * 8.3 发展排行榜
 * @param {Object} params
 * @param {string} params.rankType - OUTLET|DEVELOPER
 * @param {string} [params.businessType] - 业务类型
 * @param {string} params.startDate - yyyy-MM-dd
 * @param {string} params.endDate - yyyy-MM-dd
 * @param {number} [params.limit=10] - 排名数量
 */
export function getRanking(params) {
    return request({
        url: '/api/v1/statistics/ranking',
        method: 'get',
        params
    })
}

/**
 * 8.4 时间趋势数据
 * @param {Object} params
 * @param {string} params.granularity - DAY|WEEK|MONTH
 * @param {string} params.metric - DEVELOPMENT_COUNT|COMMISSION_AMOUNT
 * @param {string} params.startDate - yyyy-MM-dd
 * @param {string} params.endDate - yyyy-MM-dd
 * @param {string} [params.businessType] - 业务类型
 */
export function getTrend(params) {
    return request({
        url: '/api/v1/statistics/trend',
        method: 'get',
        params
    })
}