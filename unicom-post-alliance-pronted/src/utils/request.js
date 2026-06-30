import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API || '', // 后端接口基础路径
    timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
    (config) => {
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