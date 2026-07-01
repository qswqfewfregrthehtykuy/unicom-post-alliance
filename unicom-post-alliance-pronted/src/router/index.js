import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

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
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '首页', requiresAuth: true }
            },
            {
                path: 'user/list',
                name: 'UserManagement',
                component: () => import('@/views/system/UserManagement.vue'),
                meta: { title: '用户管理', requiresAuth: true }
            },
            // 🌟 新增角色管理路由
            {
                path: 'user/role',
                name: 'RoleManagement',
                component: () => import('@/views/system/RoleManagement.vue'),
                meta: { title: '角色管理', requiresAuth: true }
            },

            {
                path: 'org/city',
                name: 'CityManagement',
                component: () => import('@/views/org/CityManagement.vue'),
                meta: { title: '地市管理', requiresAuth: true }
            },
            {
                path: 'org/district',
                name: 'DistrictManagement',
                component: () => import('@/views/org/DistrictManagement.vue'),
                meta: { title: '区县管理', requiresAuth: true }
            },
            {
                path: 'org/outlet',
                name: 'OutletManagement',
                component: () => import('@/views/org/OutletManagement.vue'),
                meta: { title: '网点管理', requiresAuth: true }
            },

            {
                path: 'developer/apply',
                name: 'DeveloperApply',
                component: () => import('@/views/developer/Apply.vue'),
                meta: { title: '发展人申请', requiresAuth: true }
            },
            {
                path: 'developer/audit',
                name: 'DeveloperAudit',
                component: () => import('@/views/developer/DeveloperManage.vue'),
                props: { mode: 'audit' },
                meta: { title: '发展人审核', requiresAuth: true }
            },
            {
                path: 'developer/list',
                name: 'DeveloperList',
                component: () => import('@/views/developer/DeveloperManage.vue'),
                props: { mode: 'list' },
                meta: { title: '发展人列表', requiresAuth: true }
            },


            {
                path: 'commission/rules',
                name: 'CommissionRule',
                component: () => import('@/views/commission/RuleManagement.vue'),
                meta: { title: '佣金规则', requiresAuth: true }
            },




            // 在 'commission' 子菜单下添加
            {
                path: 'commission/details',
                name: 'CommissionDetails',
                component: () => import('@/views/commission/CommissionDetails.vue'),
                meta: { title: '佣金明细', requiresAuth: true }
            },

            {
                path: 'biz/new',
                name: 'NewOrder',
                component: () => import('@/views/biz/NewOrder.vue'),
                meta: { title: '新增业务', requiresAuth: true }
            },
            {
                path: 'biz/records',
                name: 'OrderList',
                component: () => import('@/views/biz/OrderList.vue'),
                meta: { title: '业务记录', requiresAuth: true }
            },
            {
                path: 'audit/intent',
                name: 'LeadAudit',
                component: () => import('@/views/audit/LeadAudit.vue'),
                meta: { title: '意向单审核', requiresAuth: true }
            },
            {
                path: 'audit/formal',
                name: 'FormalAudit',
                component: () => import('@/views/audit/FormalAudit.vue'),
                meta: { title: '转正审核', requiresAuth: true }
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
    document.title = to.meta.title ? `${to.meta.title} - 邮务管理` : '邮务管理系统'

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

                if (to.meta.roles && !to.meta.roles.some(role => authStore.roles.includes(role))) {
                    return next({ path: '/dashboard', query: { redirect: 'no-permission' } })
                }

                next()
            }
        } else {
            next()
        }
    }
})

export default router