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