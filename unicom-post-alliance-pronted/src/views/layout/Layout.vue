<template>
  <el-container class="layout-container">
    <!-- ========== 侧边栏 ========== -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="system-aside">
      <div class="sidebar-header">
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-area">
            <span class="logo-text">联通邮政商盟触点系统</span>
          </div>
        </transition>
        <div v-show="isCollapse" class="logo-area logo-collapsed">
        </div>
      </div>

      <el-scrollbar class="sidebar-menu-wrap">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          unique-opened
          router
          background-color="#ffffff"
          text-color="#606266"
          active-text-color="#409EFF"
        >
          <!-- 首页 -->
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <!-- 个人中心 -->
          <el-menu-item v-if="isAnyRole(['ROLE_OUTLET','ROLE_DEVELOPER','ROLE_CITY'])" index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>

          <!-- 账号管理：仅省分 -->
          <el-sub-menu v-if="isRole('ROLE_PROVINCE')" index="user">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>账号管理</span>
            </template>
            <el-menu-item index="/user/list">用户列表</el-menu-item>
            <el-menu-item index="/user/role">角色管理</el-menu-item>
          </el-sub-menu>

          <!-- 组织架构 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="org">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>组织架构</span>
            </template>
            <el-menu-item v-if="isRole('ROLE_PROVINCE')" index="/org/city">地市管理</el-menu-item>
            <el-menu-item v-if="isRole('ROLE_PROVINCE')" index="/org/district">区县管理</el-menu-item>
            <el-menu-item index="/org/outlet">网点管理</el-menu-item>
          </el-sub-menu>

          <!-- 发展人管理 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET'])" index="developer">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>发展人管理</span>
            </template>
            <el-menu-item index="/developer/create">
              <el-icon><CirclePlus /></el-icon>
              <template #title>直接创建发展人</template>
            </el-menu-item>
            <el-menu-item index="/developer/audit">发展人审核</el-menu-item>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/developer/list">发展人列表</el-menu-item>
          </el-sub-menu>

          <!-- 业务发展 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="biz">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>业务发展</span>
            </template>
            <el-menu-item v-if="isAnyRole(['ROLE_OUTLET','ROLE_DEVELOPER'])" index="/biz/new">新增业务</el-menu-item>
            <el-menu-item index="/biz/records">业务记录</el-menu-item>
          </el-sub-menu>

          <!-- 审核中心 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET'])" index="audit">
            <template #title>
              <el-icon><Checked /></el-icon>
              <span>审核中心</span>
            </template>
            <el-menu-item index="/audit/intent">意向单审核</el-menu-item>
            <el-menu-item index="/audit/formal">转正审核</el-menu-item>
          </el-sub-menu>

          <!-- 佣金管理 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="commission">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>佣金管理</span>
            </template>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/commission/rules">佣金规则</el-menu-item>
            <el-menu-item index="/commission/details">佣金明细</el-menu-item>
          </el-sub-menu>

          <!-- 统计分析 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="analysis">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>统计分析</span>
            </template>
            <el-menu-item index="/analysis/screen">数据大屏</el-menu-item>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/analysis/export">导出报表</el-menu-item>
          </el-sub-menu>

          <!-- 系统管理 -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- ========== 右侧主体 ========== -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="system-header">
        <div class="header-left">
          <el-button
            class="collapse-btn"
            :icon="isCollapse ? Expand : Fold"
            text
            @click="toggleSidebar"
          />
          <el-breadcrumb separator="/" class="header-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">
              <el-icon><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.to ? { path: item.path } : undefined">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 快捷操作 -->
          <el-tooltip content="刷新页面" placement="bottom">
            <el-button text circle @click="refreshPage">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip content="全屏切换" placement="bottom">
            <el-button text circle @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>

          <el-divider direction="vertical" />

          <!-- 用户信息下拉 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="avatarUrl" />
              <span class="username">{{ displayName }}</span>
              <el-tag v-if="roleLabel" size="small" effect="plain" :type="roleTagType">{{ roleLabel }}</el-tag>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="system-main">
        <router-view v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>

      <!-- 页脚 -->
      <el-footer class="system-footer">
        <span>联通邮政商盟触点系统 © {{ currentYear }}</span>
        <span class="footer-divider">|</span>
        <span>Version 2.0</span>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  Monitor, User, UserFilled, OfficeBuilding, Connection, CirclePlus,
  TrendCharts, Checked, Money, DataAnalysis, Setting,
  Expand, Fold, HomeFilled, Refresh, FullScreen, ArrowDown,
  Lock, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// ========== 侧边栏折叠 ==========
const isCollapse = ref(false)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
  localStorage.setItem('sidebarCollapsed', isCollapse.value ? '1' : '0')
}

// 响应式自动折叠
const handleResize = () => {
  if (window.innerWidth < 992) {
    isCollapse.value = true
  }
}

onMounted(() => {
  const saved = localStorage.getItem('sidebarCollapsed')
  if (saved === '1') isCollapse.value = true
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// ========== 面包屑 ==========
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  // 跳过 Layout 自身
  return matched.slice(1).map((item, index, arr) => ({
    title: item.meta.title,
    path: item.path,
    to: index < arr.length - 1
  }))
})

// ========== 页面缓存 ==========
const cachedViews = ref(['Dashboard', 'OrderList', 'CommissionDetails', 'SystemLogs'])

// ========== 活跃菜单 ==========
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/' || path === '/index') return '/dashboard'
  return path
})

// ========== 用户信息 ==========
const displayName = computed(() => {
  if (authStore.userInfo?.realName) return authStore.userInfo.realName
  if (authStore.userInfo?.username) return authStore.userInfo.username
  return '用户'
})

const avatarUrl = computed(() => {
  return authStore.userInfo?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const roleLabel = computed(() => {
  const roleMap = {
    'ROLE_PROVINCE': '省分管理员',
    'ROLE_CITY': '地市管理员',
    'ROLE_OUTLET': '网点管理员',
    'ROLE_DEVELOPER': '发展人'
  }
  for (const role of authStore.roles) {
    if (roleMap[role]) return roleMap[role]
  }
  return ''
})

const roleTagType = computed(() => {
  const typeMap = {
    'ROLE_PROVINCE': 'danger',
    'ROLE_CITY': 'warning',
    'ROLE_OUTLET': 'success',
    'ROLE_DEVELOPER': 'info'
  }
  for (const role of authStore.roles) {
    if (typeMap[role]) return typeMap[role]
  }
  return 'info'
})

// ========== 角色判断 ==========
const isRole = (role) => authStore.roles.includes(role)
const isAnyRole = (roles) => roles.some(role => authStore.roles.includes(role))

// ========== 年份 ==========
const currentYear = computed(() => new Date().getFullYear())

// ========== 操作 ==========
const refreshPage = () => {
  router.replace({ path: '/redirect' + route.fullPath, query: { _t: Date.now() } }).catch(() => {})
  setTimeout(() => router.replace(route.fullPath).catch(() => {}), 50)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    router.push('/profile?tab=password')
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = async () => {
  await authStore.handleLogout()
  router.push('/login')
}
</script>

<style scoped>
/* ========== 整体布局 ========== */
.layout-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
}

/* ========== 侧边栏 ========== */
.system-aside {
  background-color: #ffffff;
  border-right: 1px solid var(--color-border-lighter);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base);
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--color-border-lighter);
  flex-shrink: 0;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  white-space: nowrap;
}

.logo-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.logo-text {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.5px;
}

.logo-collapsed {
  padding: 0;
}

/* 菜单滚动区 */
.sidebar-menu-wrap {
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu-wrap :deep(.el-menu) {
  border-right: none;
  padding: 8px 0;
}

.sidebar-menu-wrap :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: var(--radius-base);
}

.sidebar-menu-wrap :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: var(--radius-base);
}

.sidebar-menu-wrap :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.sidebar-menu-wrap :deep(.el-sub-menu .el-icon) {
  font-size: 18px;
}

/* 折叠状态菜单 popover 样式修正 */
.sidebar-menu-wrap :deep(.el-menu--collapse) {
  width: 64px;
}

/* ========== 主容器 ========== */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 顶部导航 ========== */
.system-header {
  height: 56px !important;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border-lighter);
  box-shadow: var(--shadow-header);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collapse-btn {
  font-size: 20px;
  color: var(--color-text-regular);
  padding: 6px;
}

.collapse-btn:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-bg);
}

.header-breadcrumb {
  margin-left: 4px;
}

.header-breadcrumb :deep(.el-breadcrumb__item) {
  font-size: var(--font-size-sm);
}

.header-breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-normal);
}

.header-breadcrumb :deep(.el-breadcrumb__inner.is-link) {
  color: var(--color-text-regular);
}

.header-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--color-primary);
}

.header-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right .el-divider--vertical {
  height: 20px;
  margin: 0 8px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-round);
  transition: background-color var(--transition-fast);
}

.user-info:hover {
  background-color: var(--color-bg-page);
}

.username {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 12px;
  color: var(--color-text-secondary);
  transition: transform var(--transition-fast);
}

/* ========== 主内容区 ========== */
.system-main {
  flex: 1;
  padding: var(--spacing-lg);
  overflow-y: auto;
  background-color: var(--color-bg-page);
}

/* ========== 页脚 ========== */
.system-footer {
  height: 40px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  border-top: 1px solid var(--color-border-lighter);
  background-color: #ffffff;
  flex-shrink: 0;
}

.footer-divider {
  color: var(--color-border-base);
}

/* ========== 页面过渡动画 ========== */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== 侧边栏 fade 过渡 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========== 响应式 ========== */
@media screen and (max-width: 992px) {
  .system-main {
    padding: var(--spacing-base);
  }
}

@media screen and (max-width: 768px) {
  .system-header {
    padding: 0 12px;
  }

  .username {
    display: none;
  }

  .system-main {
    padding: var(--spacing-sm);
  }

  .system-footer {
    font-size: 11px;
  }
}
</style>
