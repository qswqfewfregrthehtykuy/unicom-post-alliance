import request from '@/utils/request'

// 分页查询网点
export function getOutletList(params) {
    return request({
        url: '/api/v1/outlets',
        method: 'get',
        params
    })
}

// 查询网点详情
export function getOutletDetail(outletId) {
    return request({
        url: `/api/v1/outlets/${outletId}`,
        method: 'get'
    })
}

// 创建网点
export function createOutlet(data) {
    return request({
        url: '/api/v1/outlets',
        method: 'post',
        data
    })
}

// 更新网点
export function updateOutlet(outletId, data) {
    return request({
        url: `/api/v1/outlets/${outletId}`,
        method: 'put',
        data
    })
}

// 更新网点状态
export function updateOutletStatus(outletId, status) {
    return request({
        url: `/api/v1/outlets/${outletId}/status`,
        method: 'put',
        params: { status }
    })
}

// 删除网点（软删除）
export function deleteOutlet(outletId) {
    return request({
        url: `/api/v1/outlets/${outletId}`,
        method: 'delete'
    })
}