import request from '@/utils/request'

// 获取组织树（包含网点可选）
export function getOrgTree(includeOutlet = true) {
    return request({
        url: '/api/v1/org/tree',
        method: 'get',
        params: { includeOutlet }
    })
}

// 获取某组织下的子节点（分页）
export function getChildren(orgId, pageNo = 1, pageSize = 20) {
    return request({
        url: `/api/v1/org/${orgId}/children`,
        method: 'get',
        params: { pageNo, pageSize }
    })
}

// 创建组织
export function createOrg(data) {
    return request({
        url: '/api/v1/org',
        method: 'post',
        data
    })
}

// 更新组织
export function updateOrg(orgId, data) {
    return request({
        url: `/api/v1/org/${orgId}`,
        method: 'put',
        data
    })
}

// 删除组织
export function deleteOrg(orgId) {
    return request({
        url: `/api/v1/org/${orgId}`,
        method: 'delete'
    })
}