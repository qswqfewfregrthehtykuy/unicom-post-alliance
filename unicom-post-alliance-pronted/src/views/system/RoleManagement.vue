<template>
  <div class="role-management-container">
    <el-card shadow="never" class="search-card">
      <div class="header-actions">
        <div class="title">📋 角色管理</div>
        <el-button type="primary" @click="fetchRoleList" :loading="loading">
          🔄 刷新列表
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="roleList" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="角色ID" width="100" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="150">
          <template #default="{ row }">
            <span class="role-name-text">{{ row.roleName }}</span>
            <el-tag v-if="isSystemRole(row.roleCode)" size="small" type="danger" effect="plain" class="sys-tag">
              系统内置
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色标识" min-width="180">
          <template #default="{ row }">
            <el-tag type="info">{{ row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据范围" min-width="160">
          <template #default="{ row }">
            <el-tag :type="getDataScopeTag(row.roleCode)" effect="dark">
              {{ getDataScopeName(row.roleCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openMemberDrawer(row)">
              👥 成员管理
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="warning" link @click="openPermissionMatrix(row)">
              🛡️ 权限矩阵
            </el-button>
            <el-divider direction="vertical" />
            <el-tooltip
                :disabled="!isSystemRole(row.roleCode)"
                content="系统核心内置角色，禁止删除"
                placement="top"
            >
              <span class="disabled-btn-wrapper">
                <el-button
                    type="danger"
                    link
                    :disabled="isSystemRole(row.roleCode)"
                    @click="handleDeleteRole(row)"
                >
                  🗑️ 删除
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
        :title="`【${currentRole?.roleName}】成员管理`"
        size="700px"
        destroy-on-close
    >
      <div class="drawer-desc">
        💡 仅展示拥有 <b>{{ currentRole?.roleName }}</b> 角色的用户。
      </div>

      <el-table :data="memberList" v-loading="drawerLoading" border stripe style="width: 100%; margin-top: 15px;">
        <el-table-column prop="id" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
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
                <el-button type="danger" link>移除成员</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加成员 -->
      <div class="add-member-wrapper">
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
          <el-button type="primary" @click="addMember" :disabled="!selectedUserId" style="margin-left: 12px;">
            ➕ 添加成员
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 权限矩阵 -->
    <el-dialog
        v-model="matrixVisible"
        :title="`权限矩阵配置 - ${currentRole?.roleName}`"
        width="500px"
    >
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
import request from '@/utils/request'
import { getUserList } from '@/api/auth'

// ==================== 状态 ====================
const loading = ref(false)
const drawerLoading = ref(false)
const drawerVisible = ref(false)
const matrixVisible = ref(false)

const roleList = ref([])
const memberList = ref([])
const currentRole = ref(null)

// 添加成员相关
const selectedUserId = ref(null)
const selectedUserInfo = ref(null)
const searchUserOptions = ref([])
const searchLoading = ref(false)

// ==================== 常量 ====================
const systemRoles = ['ROLE_PROVINCE', 'ROLE_CITY', 'ROLE_OUTLET', 'ROLE_DEVELOPER', 'ROLE_ADMIN']
const roleOptions = ref([
  { id: 1, roleCode: 'ROLE_PROVINCE', roleName: '省分管理员' },
  { id: 2, roleCode: 'ROLE_CITY', roleName: '地市管理员' },
  { id: 3, roleCode: 'ROLE_OUTLET', roleName: '网点管理员' },
  { id: 4, roleCode: 'ROLE_DEVELOPER', roleName: '发展人' },
  { id: 5, roleCode: 'ROLE_ADMIN', roleName: '超级管理员' }
])

// ==================== 辅助函数 ====================
const isSystemRole = (code) => systemRoles.includes(code)

const getDataScopeName = (code) => {
  const map = {
    ROLE_ADMIN: '全局（系统级）',
    ROLE_PROVINCE: '全江西省数据',
    ROLE_CITY: '本地市数据',
    ROLE_OUTLET: '本网点数据',
    ROLE_DEVELOPER: '仅限个人数据'
  }
  return map[code] || '未定义'
}

const getDataScopeTag = (code) => {
  const map = {
    ROLE_ADMIN: 'danger',
    ROLE_PROVINCE: 'danger',
    ROLE_CITY: 'warning',
    ROLE_OUTLET: 'success'
  }
  return map[code] || 'info'
}

// ==================== 1. 获取角色列表 ====================
const fetchRoleList = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/api/v1/roles',
      method: 'get'
    })
    const resData = response.data !== undefined ? response.data : response
    if (Array.isArray(resData)) {
      roleList.value = resData
    } else if (response.code === 200) {
      roleList.value = response.data || []
    } else {
      ElMessage.error(response.msg || '获取角色列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，无法加载角色')
  } finally {
    loading.value = false
  }
}

// ==================== 2. 打开成员抽屉（关键修改：传 role 参数） ====================
const openMemberDrawer = async (role) => {
  currentRole.value = role
  drawerVisible.value = true
  drawerLoading.value = true
  memberList.value = []

  try {
    // ✅ 修改：传入 role 参数，让后端直接过滤
    const response = await getUserList({
      role: role.roleCode,   // 👈 新增
      pageNo: 1,
      pageSize: 1000
    })

    const rawBody = response.data !== undefined ? response.data : response
    let allUsers = []

    if (rawBody.list && Array.isArray(rawBody.list)) {
      allUsers = rawBody.list
    } else if (rawBody.records && Array.isArray(rawBody.records)) {
      allUsers = rawBody.records
    } else if (Array.isArray(rawBody)) {
      allUsers = rawBody
    } else if (response.code === 200 && response.data) {
      allUsers = response.data.list || response.data.records || []
    }

    // 后端已过滤，直接赋值，无需再前端过滤
    memberList.value = allUsers

    console.log(`[${role.roleName}] 成员列表:`, memberList.value)

    if (memberList.value.length === 0) {
      ElMessage.info(`当前 ${role.roleName} 下暂无成员`)
    }

  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error('无法加载该角色的关联成员')
  } finally {
    drawerLoading.value = false
  }
}

// ==================== 3. 移除成员 ====================
const removeUserFromRole = async (user) => {
  if (!currentRole.value) return
  try {
    const response = await request({
      url: `/api/v1/users/${user.id}/roles/${currentRole.value.id}`,
      method: 'delete'
    })
    const ok = response.code === 200 || response.status === 200
    if (ok) {
      ElMessage.success(`已将用户 ${user.realName} 移出 ${currentRole.value.roleName}`)
      await openMemberDrawer(currentRole.value)
    } else {
      ElMessage.error(response.msg || '移出成员失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.msg || '操作异常')
  }
}

// ==================== 4. 远程搜索用户（保持搜索所有用户，便于添加） ====================
const remoteSearchUsers = async (query) => {
  if (!query || query.trim() === '') {
    searchUserOptions.value = []
    return
  }
  searchLoading.value = true
  try {
    // 搜索所有用户（不传 role），以便添加成员时能搜到任何用户
    const response = await getUserList({
      keyword: query.trim(),
      pageNo: 1,
      pageSize: 20
    })
    const rawBody = response.data !== undefined ? response.data : response
    let list = rawBody.list || rawBody.records || []
    searchUserOptions.value = list
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
  } finally {
    searchLoading.value = false
  }
}

// ==================== 5. 选择用户 ====================
const handleUserSelect = (val) => {
  selectedUserInfo.value = searchUserOptions.value.find(u => u.id === val) || null
}

// ==================== 6. 添加成员 ====================
const addMember = async () => {
  if (!selectedUserInfo.value) {
    ElMessage.warning('请先选择一个用户')
    return
  }
  const currentRoleId = currentRole.value.id

  // 获取用户当前角色 ID 列表
  const userRoles = selectedUserInfo.value.roles || []
  const currentRoleIds = userRoles
      .map(code => {
        const found = roleOptions.value.find(r => r.roleCode === code)
        return found ? found.id : null
      })
      .filter(id => id !== null)

  if (currentRoleIds.includes(currentRoleId)) {
    ElMessage.warning(`该用户已拥有 ${currentRole.value.roleName} 角色`)
    return
  }

  const newRoleIds = [...currentRoleIds, currentRoleId]

  try {
    await request({
      url: `/api/v1/users/${selectedUserInfo.value.id}/roles`,
      method: 'post',
      data: newRoleIds
    })
    ElMessage.success(`已成功将 ${selectedUserInfo.value.realName} 添加至 ${currentRole.value.roleName}`)
    await openMemberDrawer(currentRole.value)
    selectedUserId.value = null
    selectedUserInfo.value = null
    searchUserOptions.value = []
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error(error.response?.data?.msg || '添加失败')
  }
}

// ==================== 权限矩阵 ====================
const openPermissionMatrix = (role) => {
  currentRole.value = role
  matrixVisible.value = true
}

// ==================== 删除角色（禁止） ====================
const handleDeleteRole = () => {
  ElMessage.warning('系统核心角色，禁止删除')
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
/* 样式保持不变，省略... */
.role-management-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}
.search-card :deep(.el-card__body) {
  padding: 16px 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions .title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #2c3e50;
}
.sys-tag {
  margin-left: 8px;
  font-weight: bold;
}
.role-name-text {
  font-weight: 500;
  color: #34495e;
}
.drawer-desc {
  background-color: #f0faf7;
  border-left: 4px solid #2cc094;
  padding: 10px 14px;
  color: #4a5568;
  font-size: 0.9rem;
  border-radius: 0 4px 4px 0;
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
.add-member-wrapper {
  margin-top: 20px;
}
.add-member-row {
  display: flex;
  align-items: center;
  margin-top: 8px;
}
</style>