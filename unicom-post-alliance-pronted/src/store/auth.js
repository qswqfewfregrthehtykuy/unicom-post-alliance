import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: null,
        roles: [] // 存储当前用户的角色代码列表，如 ['PROVINCE']
    }),

    actions: {
        // 登录处理
        async handleLogin(loginForm) {
            try {
                const res = await login(loginForm)
                // 后端返回 Result<LoginResponse>，res.data 包含 token, roles 等
                const loginData = res.data

                this.token = loginData.token
                this.roles = loginData.roles
                this.userInfo = loginData

                localStorage.setItem('token', loginData.token)
                return loginData
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 获取/刷新用户信息
        async fetchUserInfo() {
            try {
                const res = await getUserInfo()
                this.userInfo = res.data
                this.roles = res.data.roles
                return res.data
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 退出登录
        async handleLogout() {
            try {
                await logout()
            } finally {
                // 无论后端成功与否，前端本地都做清除
                this.token = ''
                this.userInfo = null
                this.roles = []
                localStorage.clear()
            }
        }
    }
})