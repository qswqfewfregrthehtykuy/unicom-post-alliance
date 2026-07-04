<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="section-title">📋 角色管理</span>
        </div>
        <div class="toolbar-right">
          <el-button :icon="RefreshRight" @click="fetchRoleList" :loading="loading">刷新列表</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="roleList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="角色ID" width="100" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="160">
          <template #default="{ row }">
            <span class="role-name-text">{{ row.roleName }}</span>
            <el-tag v-if="isSystemRole(row.roleCode)" size="small" type="danger" effect="plain" class="sys-tag">
              系统内置
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色标识" min-width="180">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据范围" min-width="160">
          <template #default="{ row }">
            <el-tag :type="getDataScopeTag(row.roleCode)" effect="dark" size="small">
              {{ getDataScopeName(row.roleCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openMemberDrawer(row)">
              <el-icon><User /></el-icon> 成员管理
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="warning" link @click="openPermissionMatrix(row)">
              <el-icon><Key /></el-icon> 权限矩阵
            </el-button>
            <el-divider direction="vertical" />
            <el-tooltip
              :disabled="!isSystemRole(row.roleCode)"
              content="系统核心内置角色，禁止删除"
              placement="top"
            >
              <span class="disabled-btn-wrapper">
                <el-button type="danger" link :disabled="isSystemRole(row.roleCode)" @click="handleDeleteRole(row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 成员管理抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="`成员管理 - ${currentRole?.roleName}`"
      size="700px"
      destroy-on-close
    >
      <div class="drawer-hint">
        <el-icon><InfoFilled /></el-icon>
        仅展示拥有 <b>{{ currentRole?.roleName }}</b> 角色的用户
      </div>

      <el-table :data="memberList" v-loading="drawerLoading" stripe style="width: 100%; margin-top: 16px;">
        <el-table-column prop="id" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="真实姓名" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="{ row }">
            <el-popconfirm
              title="确定要将该用户移出此角色吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="removeUserFromRole(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">移除成员</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">添加成员</el-divider>
      <div class="add-member-row">
        <el-select
          v-model="selectedUserId"
          filterable
          remote
          reserve-keyword
          placeholder="输入用户名或手机号搜索用户"
          :remote-method="remoteSearchUsers"
          :loading="searchLoading"
          style="width: 380px;"
          @change="handleUserSelect"
        >
          <el-option
            v-for="user in searchUserOptions"
            :key="user.id"
            :label="`${user.username} (${user.realName})`"
            :value="user.id"
          />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="addMember" :disabled="!selectedUserId" style="margin-left: 12px;">
          添加成员
        </el-button>
      </div>
    </el-drawer>

    <!-- 权限矩阵 -->
    <el-dialog v-model="matrixVisible" :title="`权限矩阵 - ${currentRole?.roleName}`" width="500px">
      <el-alert title="本系统实施强 RBAC 模型管控，权限由后端底层严格控制。" type="info" show-icon :closable="false" />
      <div class="matrix-content" style="margin-top: 16px;">
        <p><b>对应后端鉴权表达式：</b></p>
        <code class="code-block">@PreAuthorize("hasRole('{{ currentRole?.roleCode?.replace('ROLE_', '') }}')")</code>
      </div>
      <template #footer>
        <el-button type="primary" @click="matrixVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Key, Delete, Plus, InfoFilled, RefreshRight } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getUserList } from '@/api/auth'

const loading = ref(false)
const drawerLoading = ref(false)
const drawerVisible = ref(false)
const matrixVisible = ref(false)

const roleList = ref([])
const memberList = ref([])
const currentRole = ref(null)

const selectedUserId = ref(null)
const selectedUserInfo = ref(null)
const searchUserOptions = ref([])
const searchLoading = ref(false)

const systemRoles = ['ROLE_PROVINCE', 'ROLE_CITY', 'ROLE_OUTLET', 'ROLE_DEVELOPER', 'ROLE_ADMIN']
const roleOptions = ref([
  { id: 1, roleCode: 'ROLE_PROVINCE', roleName: '省分管理员' },
  { id: 2, roleCode: 'ROLE_CITY', roleName: '地市管理员' },
  { id: 3, roleCode: 'ROLE_OUTLET', roleName: '网点管理员' },
  { id: 4, roleCode: 'ROLE_DEVELOPER', roleName: '发展人' },
  { id: 5, roleCode: 'ROLE_ADMIN', roleName: '超级管理员' }
])

const isSystemRole = (code) => systemRoles.includes(code)

const getDataScopeName = (code) => {
  const map = {
    ROLE_ADMIN: '全局（系统级）', ROLE_PROVINCE: '全江西省数据',
    ROLE_CITY: '本地市数据', ROLE_OUTLET: '本网点数据', ROLE_DEVELOPER: '仅限个人数据'
  }
  return map[code] || '未定义'
}

const getDataScopeTag = (code) => {
  const map = { ROLE_ADMIN: 'danger', ROLE_PROVINCE: 'danger', ROLE_CITY: 'warning', ROLE_OUTLET: 'success' }
  return map[code] || 'info'
}

const fetchRoleList = async () => {
  loading.value = true
  try {
    const response = await request({ url: '/api/v1/roles', method: 'get' })
    const resData = response.data !== undefined ? response.data : response
    if (Array.isArray(resData)) { roleList.value = resData }
    else if (response.code === 200) { roleList.value = response.data || [] }
    else { ElMessage.error(response.msg || '获取角色列表失败') }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，无法加载角色')
  } finally { loading.value = false }
}

const openMemberDrawer = async (role) => {
  currentRole.value = role
  drawerVisible.value = true
  drawerLoading.value = true
  memberList.value = []
  try {
    const response = await getUserList({ role: role.roleCode, pageNo: 1, pageSize: 1000 })
    const rawBody = response.data !== undefined ? response.data : response
    let allUsers = []
    if (rawBody.list && Array.isArray(rawBody.list)) allUsers = rawBody.list
    else if (rawBody.records && Array.isArray(rawBody.records)) allUsers = rawBody.records
    else if (Array.isArray(rawBody)) allUsers = rawBody
    else if (response.code === 200 && response.data) allUsers = response.data.list || response.data.records || []
    memberList.value = allUsers
    if (memberList.value.length === 0) ElMessage.info(`当前 ${role.roleName} 下暂无成员`)
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error('无法加载该角色的关联成员')
  } finally { drawerLoading.value = false }
}

const removeUserFromRole = async (user) => {
  if (!currentRole.value) return
  try {
    const response = await request({ url: `/api/v1/users/${user.id}/roles/${currentRole.value.id}`, method: 'delete' })
    const ok = response.code === 200 || response.status === 200
    if (ok) {
      ElMessage.success(`已将用户 ${user.realName} 移出 ${currentRole.value.roleName}`)
      await openMemberDrawer(currentRole.value)
    } else { ElMessage.error(response.msg || '移出成员失败') }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.msg || '操作异常')
  }
}

const remoteSearchUsers = async (query) => {
  if (!query || query.trim() === '') { searchUserOptions.value = []; return }
  searchLoading.value = true
  try {
    const response = await getUserList({ keyword: query.trim(), pageNo: 1, pageSize: 20 })
    const rawBody = response.data !== undefined ? response.data : response
    searchUserOptions.value = rawBody.list || rawBody.records || []
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
  } finally { searchLoading.value = false }
}

const handleUserSelect = (val) => {
  selectedUserInfo.value = searchUserOptions.value.find(u => u.id === val) || null
}

const addMember = async () => {
  if (!selectedUserInfo.value) { ElMessage.warning('请先选择一个用户'); return }
  const currentRoleId = currentRole.value.id
  const userRoles = selectedUserInfo.value.roles || []
  const currentRoleIds = userRoles
    .map(code => { const found = roleOptions.value.find(r => r.roleCode === code); return found ? found.id : null })
    .filter(id => id !== null)
  if (currentRoleIds.includes(currentRoleId)) { ElMessage.warning(`该用户已拥有 ${currentRole.value.roleName} 角色`); return }
  const newRoleIds = [...currentRoleIds, currentRoleId]
  try {
    await request({ url: `/api/v1/users/${selectedUserInfo.value.id}/roles`, method: 'post', data: newRoleIds })
    ElMessage.success(`已成功将 ${selectedUserInfo.value.realName} 添加至 ${currentRole.value.roleName}`)
    await openMemberDrawer(currentRole.value)
    selectedUserId.value = null; selectedUserInfo.value = null; searchUserOptions.value = []
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error(error.response?.data?.msg || '添加失败')
  }
}

const openPermissionMatrix = (role) => { currentRole.value = role; matrixVisible.value = true }
const handleDeleteRole = () => { ElMessage.warning('系统核心角色，禁止删除') }

onMounted(() => { fetchRoleList() })
</script>

<style scoped>
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.role-name-text {
  font-weight: 500;
  color: var(--color-text-primary);
}

.sys-tag {
  margin-left: 8px;
  font-weight: 600;
}

.drawer-hint {
  background-color: var(--color-primary-bg);
  border-left: 4px solid var(--color-primary);
  padding: 10px 14px;
  color: var(--color-text-regular);
  font-size: 13px;
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.code-block {
  display: block;
  background-color: #2d3748;
  color: #a3b8cc;
  padding: 12px;
  border-radius: 6px;
  font-family: Consolas, Monaco, monospace;
  font-size: 0.95rem;
  margin-top: 8px;
}

.disabled-btn-wrapper {
  cursor: not-allowed;
  display: inline-block;
}

.add-member-row {
  display: flex;
  align-items: center;
  margin-top: 8px;
  margin-bottom: 16px;
}
</style>
