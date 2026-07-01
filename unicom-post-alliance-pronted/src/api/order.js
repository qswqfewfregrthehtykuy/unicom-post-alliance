import request from '@/utils/request'

// 提交订单
export function submitOrder(data) {
    return request({
        url: '/api/v1/orders',
        method: 'post',
        data
    })
}

// 查询订单列表
export function getOrderList(params) {
    return request({
        url: '/api/v1/orders',
        method: 'get',
        params
    })
}

// 查询订单详情
export function getOrderDetail(orderId) {
    return request({
        url: `/api/v1/orders/${orderId}`,
        method: 'get'
    })
}

// 意向单审核
export function leadAudit(orderId, data) {
    return request({
        url: `/api/v1/orders/${orderId}/lead-audit`,
        method: 'put',
        data
    })
}

// 转正审核
export function formalAudit(orderId, data) {
    return request({
        url: `/api/v1/orders/${orderId}/formal-audit`,
        method: 'put',
        data
    })
}

// 驳回订单
export function rejectOrder(orderId, data) {
    return request({
        url: `/api/v1/orders/${orderId}/reject`,
        method: 'put',
        data
    })
}