<template>
  <el-container class="layout-container">
    <el-header class="system-header">
      <div class="header-left">
        <div class="logo-box">
          <span class="logo-icon">🇨🇳</span>
          <span class="logo-text">联通代理达人系统</span>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <span class="username">{{ displayName }}</span>
          <el-tag v-if="roleLabel" size="small" type="success" effect="plain">{{ roleLabel }}</el-tag>
        </div>
        <el-divider direction="vertical" />
        <el-button type="danger" link @click="handleLogout">
          <span class="logout-icon">🚪</span> 退出登录
        </el-button>
      </div>
    </el-header>

    <el-container class="main-container">
      <el-aside width="240px" class="system-aside">
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            unique-opened
            router
            background-color="#ffffff"
            text-color="#4a5568"
            active-text-color="#2cc094"
        >
          <!-- ========== 所有角色可见 ========== -->
          <el-menu-item index="/dashboard">
            <template #title>
              <span class="menu-icon">📊</span>
              <span>首页 Dashboard</span>
            </template>
          </el-menu-item>

          <!-- ========== 个人中心：OUTLET / DEVELOPER / CITY ========== -->
          <el-menu-item v-if="isAnyRole(['ROLE_OUTLET','ROLE_DEVELOPER','ROLE_CITY'])" index="/profile">
            <template #title>
              <span class="menu-icon">👤</span>
              <span>个人中心</span>
            </template>
          </el-menu-item>

          <!-- ========== 管理员功能：PROVINCE ========== -->
          <el-sub-menu v-if="isRole('ROLE_PROVINCE')" index="user">
            <template #title>
              <span class="menu-icon">👥</span>
              <span>账号管理</span>
            </template>
            <el-menu-item index="/user/list">用户列表</el-menu-item>
            <el-menu-item index="/user/role">角色管理</el-menu-item>
          </el-sub-menu>

          <!-- ========== 组织架构：PROVINCE 全部 / CITY 只看网点 ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="org">
            <template #title>
              <span class="menu-icon">🏢</span>
              <span>组织架构</span>
            </template>
            <el-menu-item v-if="isRole('ROLE_PROVINCE')" index="/org/city">地市管理</el-menu-item>
            <el-menu-item v-if="isRole('ROLE_PROVINCE')" index="/org/district">区县管理</el-menu-item>
            <el-menu-item index="/org/outlet">网点管理</el-menu-item>
          </el-sub-menu>

          <!-- ========== 发展人管理：PROVINCE / CITY / OUTLET ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET'])" index="developer">
            <template #title>
              <span class="menu-icon">🤝</span>
              <span>发展人管理</span>
            </template>
            <!-- 公开提交申请：所有角色 + 未登录用户 -->
            <el-menu-item index="/developer/apply">发展人申请</el-menu-item>
            <!-- 审核：PROVINCE / CITY / OUTLET -->
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET'])" index="/developer/audit">发展人审核</el-menu-item>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/developer/list">发展人列表</el-menu-item>
          </el-sub-menu>

          <!-- ========== 发展人端：提交申请入口（单独） ========== -->
          <el-menu-item v-if="isRole('ROLE_DEVELOPER')" index="/developer/apply">
            <template #title>
              <span class="menu-icon">📝</span>
              <span>发展人申请</span>
            </template>
          </el-menu-item>

          <!-- ========== 业务发展：OUTLET / DEVELOPER / PROVINCE / CITY ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="biz">
            <template #title>
              <span class="menu-icon">📈</span>
              <span>业务发展</span>
            </template>
            <el-menu-item v-if="isAnyRole(['ROLE_OUTLET','ROLE_DEVELOPER'])" index="/biz/new">新增业务</el-menu-item>
            <el-menu-item index="/biz/records">业务记录</el-menu-item>
          </el-sub-menu>

          <!-- ========== 审核中心：PROVINCE / CITY / OUTLET ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET'])" index="audit">
            <template #title>
              <span class="menu-icon">⚖️</span>
              <span>审核中心</span>
            </template>
            <el-menu-item index="/audit/intent">意向单审核</el-menu-item>
            <el-menu-item index="/audit/formal">转正审核</el-menu-item>
          </el-sub-menu>

          <!-- ========== 佣金管理 ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="commission">
            <template #title>
              <span class="menu-icon">💰</span>
              <span>佣金管理</span>
            </template>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/commission/rules">佣金规则</el-menu-item>
            <el-menu-item index="/commission/details">佣金明细</el-menu-item>
          </el-sub-menu>

          <!-- ========== 统计分析 ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY','ROLE_OUTLET','ROLE_DEVELOPER'])" index="analysis">
            <template #title>
              <span class="menu-icon">🔮</span>
              <span>统计分析</span>
            </template>
            <el-menu-item index="/analysis/screen">数据大屏</el-menu-item>
            <el-menu-item v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="/analysis/export">导出报表</el-menu-item>
          </el-sub-menu>

          <!-- ========== 系统管理：PROVINCE / CITY ========== -->
          <el-sub-menu v-if="isAnyRole(['ROLE_PROVINCE','ROLE_CITY'])" index="system">
            <template #title>
              <span class="menu-icon">⚙️</span>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main class="system-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 动态高亮当前菜单项
const activeMenu = computed(() => {
  return route.path === '/' || route.path === '/index' ? '/dashboard' : route.path
})

// 显示用户名
const displayName = computed(() => {
  if (authStore.userInfo && authStore.userInfo.realName) {
    return authStore.userInfo.realName
  }
  if (authStore.userInfo && authStore.userInfo.username) {
    return authStore.userInfo.username
  }
  return '用户'
})

// 角色中文标签
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

// 判断是否为指定角色
const isRole = (role) => {
  return authStore.roles.includes(role)
}

// 判断是否为指定角色之一
const isAnyRole = (roles) => {
  return roles.some(role => authStore.roles.includes(role))
}

// 登出逻辑
const handleLogout = async () => {
  await authStore.handleLogout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f4f7f6;
}

.system-header {
  height: 60px !important;
  background: linear-gradient(90deg, #1f2d3d, #2c3e50);
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 10;
  flex-shrink: 0;
}

.header-left .logo-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon { font-size: 1.4rem; }
.logo-text { font-size: 1.25rem; font-weight: 600; letter-spacing: 0.5px; }
.header-right { display: flex; align-items: center; gap: 15px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.username { font-size: 0.9rem; color: #eaedf1; }
.main-container { flex: 1; display: flex; overflow: hidden; }

.system-aside {
  background-color: #ffffff;
  border-right: 1px solid #e2e8f0;
  overflow-y: auto;
}

.el-menu-vertical { border-right: none; height: 100%; }
:deep(.el-sub-menu__title) { font-weight: 500; }
.menu-icon { margin-right: 10px; font-size: 1.1rem; }

.system-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  box-sizing: border-box;
  background-color: #f4f7f6;
}

/* 滚动条唯美优化 */
.system-main::-webkit-scrollbar,
.system-aside::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.system-main::-webkit-scrollbar-thumb,
.system-aside::-webkit-scrollbar-thumb {
  background: rgba(100, 116, 139, 0.2);
  border-radius: 4px;
}
.system-main::-webkit-scrollbar-thumb:hover,
.system-aside::-webkit-scrollbar-thumb:hover {
  background: rgba(44, 192, 148, 0.4);
}
.system-main::-webkit-scrollbar-track,
.system-aside::-webkit-scrollbar-track {
  background: transparent;
}
</style>