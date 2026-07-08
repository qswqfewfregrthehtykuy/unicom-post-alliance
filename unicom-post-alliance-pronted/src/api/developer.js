import request from '@/utils/request'

export function submitApply(data) {
    return request({
        url: '/api/v1/developer-applies',
        method: 'post',
        data
    })
}

export function getApplyList(params) {
    return request({
        url: '/api/v1/developer-applies',
        method: 'get',
        params
    })
}

export function getApplyDetail(applyId) {
    return request({
        url: `/api/v1/developer-applies/${applyId}`,
        method: 'get'
    })
}

export function auditApply(applyId, data) {
    return request({
        url: `/api/v1/developer-applies/${applyId}/audit`,
        method: 'put',
        data
    })
}

export function rejectApply(applyId, data) {
    return request({
        url: `/api/v1/developer-applies/${applyId}/reject`,
        method: 'put',
        data
    })
}

/**
 * 获取当前发展人的个人资料（含网点、等级、累计数据）
 */
export function getDeveloperProfile() {
    return request({
        url: '/api/v1/developer/profile',
        method: 'get'
    })
}

/**
 * 管理员直接创建发展人（绕过审核流程）
 * @param {Object} data 发展人信息
 */
export function createDeveloper(data) {
    return request({
        url: '/api/v1/developer-applies/create',
        method: 'post',
        data
    })
}

/**
 * 获取发展人列表（供录单时选择发展人）
 * @param {Object} params - { outletId }
 */
export function getDeveloperList(params) {
    return request({
        url: '/api/v1/developer/list',
        method: 'get',
        params
    })
}