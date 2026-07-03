import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data { username, password }
 */
export function login(data) {
    return request({
        url: '/api/v1/auth/login',
        method: 'post',
        data
    })
}

/**
 * 用户登出
 */
export function logout() {
    return request({
        url: '/api/v1/auth/logout',
        method: 'post'
    })
}

/**
 * 获取当前登录用户信息
 */
export function getUserInfo() {
    return request({
        url: '/api/v1/auth/me',
        method: 'get'
    })
}

/**
 * 修改密码
 * @param {Object} data { oldPassword, newPassword, confirmPassword }
 */
export function changePassword(data) {
    return request({
        url: '/api/v1/auth/password',
        method: 'put',
        data
    })
}

export function submitDeveloperApply(data) {
    return request({
        url: '/api/v1/developer-applies',
        method: 'post',
        data
    })
}

/**
 * 获取所有地市列表（公开只读）
 */
export function getCities() {
    return request({
        url: '/api/v1/org/cities',
        method: 'get'
    })
}

/**
 * 根据地市ID获取区县及下属网点（公开只读）
 * @param {Number/String} cityId 地市ID
 */
export function getDistrictsWithOutlets(cityId) {
    return request({
        url: '/api/v1/org/districts/outlets',
        method: 'get',
        params: { cityId }
    })
}

/**
 * 分页获取用户列表
 */
export function getUserList(params) {
    return request({
        url: '/api/v1/users',
        method: 'get',
        params
    })
}

/**
 * 创建新用户账号
 */
export function createUser(data) {
    return request({
        url: '/api/v1/users',
        method: 'post',
        data
    })
}

/**
 * 更新用户账号信息
 */
export function updateUser(userId, data) {
    return request({
        url: `/api/v1/users/${userId}`,
        method: 'put',
        data
    })
}

/**
 * 切换账号启用/禁用状态
 */
export function updateUserStatus(userId, status) {
    return request({
        url: `/api/v1/users/${userId}/status`,
        method: 'put',
        params: { status }
    })
}

/**
 * 彻底物理/逻辑删除账号
 */
export function deleteUser(userId) {
    return request({
        url: `/api/v1/users/${userId}`,
        method: 'delete'
    })
}

/**
 * 重置用户密码（管理员操作）
 * @param {Number} userId
 * @param {Object} data { resetType: 'AUTO' | 'MANUAL', newPassword?: string }
 */
export function resetPassword(userId, data) {
    return request({
        url: `/api/v1/auth/reset-password/${userId}`,
        method: 'post',
        data
    })
}