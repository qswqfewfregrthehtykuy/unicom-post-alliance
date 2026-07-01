import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// ---------- 日期转换工具函数 ----------
/**
 * 将日期（Date 或 ISO 字符串）格式化为 yyyy-MM-dd
 */
const formatDate = (date) => {
    if (!date) return ''
    const d = new Date(date)
    if (isNaN(d.getTime())) return ''
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

/**
 * 递归转换对象中的日期类型字段
 * - 支持 Date 对象
 * - 支持 ISO 8601 格式字符串（如 "2026-07-01T01:42:39.583Z"）
 * - 支持嵌套对象和数组
 */
const convertDates = (obj) => {
    if (!obj || typeof obj !== 'object') return obj
    // 处理数组
    if (Array.isArray(obj)) {
        return obj.map(item => convertDates(item))
    }
    // 处理普通对象
    for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            const val = obj[key]
            // 如果值是 Date 对象，直接格式化
            if (val instanceof Date) {
                obj[key] = formatDate(val)
            }
            // 如果值是字符串且符合 ISO 8601 日期时间格式，则尝试转换为 yyyy-MM-dd
            else if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(val)) {
                obj[key] = formatDate(val)
            }
            // 如果值是对象或数组，递归处理
            else if (typeof val === 'object') {
                convertDates(val)
            }
        }
    }
    return obj
}
// ------------------------------------

// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API || '', // 后端接口基础路径
    timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
    (config) => {
        // ---------- 自动转换日期 ----------
        if (config.params) {
            config.params = convertDates(config.params)
        }
        if (config.data) {
            config.data = convertDates(config.data)
        }
        // ----------------------------------

        const token = localStorage.getItem('token')
        if (token) {
            // 让每个请求携带自定义 token
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response) => {
        // 对应后端 Result 结构的解析

        // 如果响应类型为 blob，直接返回整个 response 对象
        if (response.config.responseType === 'blob') {
            return response
        }
        const res = response.data

        // 如果后端返回的 code 不是 200（或者成功标志），则进行拦截提示
        if (res.code && res.code !== 200) {
            ElMessage.error(res.msg || '系统错误')

            // 401 说明登录失效或无权限，清除本地数据并跳回登录页
            if (res.code === 401) {
                localStorage.clear()
                router.push('/login')
            }
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    (error) => {
        // 处理 HTTP 状态码错误 (对应后端 throw new BusinessException)
        if (error.response && error.response.data) {
            const bizError = error.response.data
            ElMessage.error(bizError.msg || '请求失败')
            if (error.response.status === 401) {
                localStorage.clear()
                router.push('/login')
            }
        } else {
            ElMessage.error('网络连接异常，请检查后端服务')
        }
        return Promise.reject(error)
    }
)

export default service