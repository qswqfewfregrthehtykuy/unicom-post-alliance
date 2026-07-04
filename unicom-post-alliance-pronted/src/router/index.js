import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

// 角色常量
const ROLES = {
  PROVINCE: 'ROLE_PROVINCE',
  CITY: 'ROLE_CITY',
  OUTLET: 'ROLE_OUTLET',
  DEVELOPER: 'ROLE_DEVELOPER'
}

// 快捷角色组合
const ADMIN = [ROLES.PROVINCE]                          // 超级管理员
const ADMIN_CITY = [ROLES.PROVINCE, ROLES.CITY]         // 省分+地市
const ADMIN_OUTLET = [ROLES.PROVINCE, ROLES.CITY, ROLES.OUTLET] // 管理层
const ALL_USERS = [ROLES.PROVINCE, ROLES.CITY, ROLES.OUTLET, ROLES.DEVELOPER] // 所有登录用户
const BUSINESS = [ROLES.OUTLET, ROLES.DEVELOPER]         // 业务操作角色

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('@/views/layout/Layout.vue'),
        redirect: '/dashboard',
        children: [
            // ==================== 重定向路由（用于页面刷新） ====================
            {
                path: 'redirect/:path(.*)',
                name: 'Redirect',
                meta: { title: '跳转中', requiresAuth: true, roles: ALL_USERS },
                beforeEnter: (to) => {
                  const targetPath = '/' + to.params.path
                  window.location.href = targetPath
                  return false
                }
            },

            // ==================== 首页：所有角色 ====================
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '首页', requiresAuth: true, roles: ALL_USERS }
            },

            // ==================== 个人中心：非管理员 ====================
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/Index.vue'),
                meta: { title: '个人中心', requiresAuth: true, roles: ALL_USERS }
            },

            // ==================== 账号管理：仅省分 ====================
            {
                path: 'user/list',
                name: 'UserManagement',
                component: () => import('@/views/system/UserManagement.vue'),
                meta: { title: '用户列表', requiresAuth: true, roles: ADMIN }
            },
            {
                path: 'user/role',
                name: 'RoleManagement',
                component: () => import('@/views/system/RoleManagement.vue'),
                meta: { title: '角色管理', requiresAuth: true, roles: ADMIN }
            },

            // ==================== 组织架构 ====================
            {
                path: 'org/city',
                name: 'CityManagement',
                component: () => import('@/views/org/CityManagement.vue'),
                meta: { title: '地市管理', requiresAuth: true, roles: ADMIN }
            },
            {
                path: 'org/district',
                name: 'DistrictManagement',
                component: () => import('@/views/org/DistrictManagement.vue'),
                meta: { title: '区县管理', requiresAuth: true, roles: ADMIN }
            },
            {
                path: 'org/outlet',
                name: 'OutletManagement',
                component: () => import('@/views/org/OutletManagement.vue'),
                meta: { title: '网点管理', requiresAuth: true, roles: ADMIN_CITY }
            },

            // ==================== 发展人管理 ====================
            {
                path: 'developer/apply',
                name: 'DeveloperApply',
                component: () => import('@/views/developer/Apply.vue'),
                meta: { title: '发展人申请', requiresAuth: true, roles: ALL_USERS }
            },
            {
                path: 'developer/audit',
                name: 'DeveloperAudit',
                component: () => import('@/views/developer/DeveloperManage.vue'),
                props: { mode: 'audit' },
                meta: { title: '发展人审核', requiresAuth: true, roles: ADMIN_OUTLET }
            },
            {
                path: 'developer/list',
                name: 'DeveloperList',
                component: () => import('@/views/developer/DeveloperManage.vue'),
                props: { mode: 'list' },
                meta: { title: '发展人列表', requiresAuth: true, roles: ADMIN_CITY }
            },

            // ==================== 业务发展 ====================
            {
                path: 'biz/new',
                name: 'NewOrder',
                component: () => import('@/views/biz/NewOrder.vue'),
                meta: { title: '新增业务', requiresAuth: true, roles: BUSINESS }
            },
            {
                path: 'biz/records',
                name: 'OrderList',
                component: () => import('@/views/biz/OrderList.vue'),
                meta: { title: '业务记录', requiresAuth: true, roles: ALL_USERS }
            },

            // ==================== 审核中心 ====================
            {
                path: 'audit/intent',
                name: 'LeadAudit',
                component: () => import('@/views/audit/LeadAudit.vue'),
                meta: { title: '意向单审核', requiresAuth: true, roles: ADMIN_OUTLET }
            },
            {
                path: 'audit/formal',
                name: 'FormalAudit',
                component: () => import('@/views/audit/FormalAudit.vue'),
                meta: { title: '转正审核', requiresAuth: true, roles: ADMIN_OUTLET }
            },

            // ==================== 佣金管理 ====================
            {
                path: 'commission/rules',
                name: 'CommissionRule',
                component: () => import('@/views/commission/RuleManagement.vue'),
                meta: { title: '佣金规则', requiresAuth: true, roles: ADMIN_CITY }
            },
            {
                path: 'commission/details',
                name: 'CommissionDetails',
                component: () => import('@/views/commission/CommissionDetails.vue'),
                meta: { title: '佣金明细', requiresAuth: true, roles: ALL_USERS }
            },

            // ==================== 统计分析 ====================
            {
                path: 'analysis/screen',
                name: 'AnalysisScreen',
                component: () => import('@/views/analysis/Dashboard.vue'),
                meta: { title: '数据大屏', requiresAuth: true, roles: ALL_USERS }
            },
            {
                path: 'analysis/export',
                name: 'ExportReport',
                component: () => import('@/views/analysis/Export.vue'),
                meta: { title: '导出报表', requiresAuth: true, roles: ADMIN_CITY }
            },

            // ==================== 系统管理 ====================
            {
                path: 'system/logs',
                name: 'SystemLogs',
                component: () => import('@/views/system/Logs.vue'),
                meta: { title: '操作日志', requiresAuth: true, roles: ADMIN_CITY }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由拦截守卫
router.beforeEach(async (to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 联通邮政商盟触点系统` : '联通邮政商盟触点系统'

    const token = localStorage.getItem('token')

    if (to.path === '/login') {
        if (token) {
            next('/')
        } else {
            next()
        }
    } else {
        if (to.meta.requiresAuth) {
            if (!token) {
                next('/login')
            } else {
                const authStore = useAuthStore()
                if (!authStore.userInfo) {
                    try {
                        await authStore.fetchUserInfo()
                    } catch (err) {
                        authStore.handleLogout()
                        return next('/login')
                    }
                }

                // 角色权限检查
                if (to.meta.roles && to.meta.roles.length > 0) {
                    const hasRole = to.meta.roles.some(role => authStore.roles.includes(role))
                    if (!hasRole) {
                        console.warn(`用户无权限访问 ${to.path}，当前角色: ${authStore.roles.join(',')}`)
                        return next({ path: '/dashboard', query: { redirect: 'no-permission' } })
                    }
                }

                next()
            }
        } else {
            next()
        }
    }
})

export default router