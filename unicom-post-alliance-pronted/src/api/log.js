import request from '@/utils/request'

/**
 * 分页查询操作日志
 * @param {Object} params - 查询参数
 * @param {string} [params.module] - 模块
 * @param {string} [params.action] - 操作动作
 * @param {number} [params.userId] - 操作人ID
 * @param {string} [params.targetType] - 目标类型
 * @param {string} [params.result] - 操作结果
 * @param {string} [params.startDate] - 开始日期 yyyy-MM-dd
 * @param {string} [params.endDate] - 结束日期 yyyy-MM-dd
 * @param {number} [params.pageNo=1] - 页码
 * @param {number} [params.pageSize=20] - 每页条数
 */
export function getLogs(params) {
    return request({
        url: '/api/v1/logs',
        method: 'get',
        params
    })
}