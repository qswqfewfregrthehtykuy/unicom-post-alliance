<template>
  <div class="user-management-container">
    <!-- 头部搜索与操作条 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="角色类型">
          <el-select v-model="queryParams.role" placeholder="全部角色" clearable style="width: 140px">
            <el-option label="省分管理员" value="ROLE_PROVINCE" />
            <el-option label="地市管理员" value="ROLE_CITY" />
            <el-option label="网点管理员" value="ROLE_OUTLET" />
            <el-option label="发展人" value="ROLE_DEVELOPER" />
            <el-option label="超级管理员" value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属地市">
          <el-select v-model="queryParams.cityId" placeholder="全部地市" clearable style="width: 140px">
            <el-option v-for="item in cityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="账号状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="用户名/姓名/手机号" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <span class="btn-icon">🔍</span> 查询
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="action-bar">
        <el-button type="success" @click="openDialog('add')">
          <span class="btn-icon">➕</span> 新增用户
        </el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="90" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="110" />
        <el-table-column prop="phone" label="手机号" width="120" align="center" />
        <el-table-column label="所属角色" min-width="140">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role" class="mx-1" size="small" type="info">
              {{ formatRole(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataScopeType" label="数据权限粒度" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getScopeTypeTag(scope.row.dataScopeType)" size="small">
              {{ scope.row.dataScopeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openDialog('edit', scope.row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.pageNo"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleQuery"
            @current-change="fetchUserList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        :title="dialogType === 'add' ? '新增系统用户' : '编辑系统用户'"
        v-model="dialogVisible"
        width="540px"
        @close="closeDialog"
    >
      <el-form :model="userForm" :rules="formRules" ref="userFormRef" label-width="110px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="登录账号（如手机号或工号）" :disabled="dialogType === 'edit'" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入员工真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入常用手机号" />
        </el-form-item>

        <el-form-item label="分配角色" prop="roleIds">
          <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色类型（多选）" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="数据范围类型" prop="dataScopeType">
          <el-select v-model="userForm.dataScopeType" placeholder="请选择数据可见范围" style="width: 100%">
            <el-option label="省级全量" value="PROVINCE" />
            <el-option label="地市级" value="CITY" />
            <el-option label="网点级" value="OUTLET" />
            <el-option label="个人专属" value="PERSONAL" />
          </el-select>
        </el-form-item>

        <!-- 联动：只有选择地市级或更细时才显现 -->
        <el-form-item v-if="userForm.dataScopeType === 'CITY' || userForm.dataScopeType === 'OUTLET'" label="管辖地市" prop="scopeCityId">
          <el-select v-model="userForm.scopeCityId" placeholder="请指定管辖的地市" style="width: 100%">
            <el-option v-for="item in cityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="userForm.dataScopeType === 'OUTLET'" label="管辖网点ID" prop="scopeOutletId">
          <el-input-number v-model="userForm.scopeOutletId" :min="1" placeholder="绑定所属网点主键ID" style="width: 100%" controls-position="right" />
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="userForm.remark" type="textarea" :rows="2" placeholder="可在此输入岗位或部门备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, updateUserStatus, deleteUser } from '@/api/auth' // 👈 统一引入

// --- 响应式变量 ---
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const userFormRef = ref(null)
const userList = ref([])
const total = ref(0)

const cityOptions = ref([
  { id: 1, name: '南昌市' }, { id: 2, name: '九江市' }, { id: 3, name: '赣州市' },
  { id: 4, name: '吉安市' }, { id: 5, name: '宜春市' }, { id: 6, name: '抚州市' },
  { id: 7, name: '上饶市' }, { id: 8, name: '萍乡市' }, { id: 9, name: '景德镇市' },
  { id: 10, name: '新余市' }, { id: 11, name: '鹰潭市' }
])

const roleOptions = ref([
  { id: 1, roleCode: 'ROLE_PROVINCE', roleName: '省分管理员' },
  { id: 2, roleCode: 'ROLE_CITY', roleName: '地市管理员' },
  { id: 3, roleCode: 'ROLE_OUTLET', roleName: '网点管理员' },
  { id: 4, roleCode: 'ROLE_DEVELOPER', roleName: '发展人' },
  { id: 5, roleCode: 'ROLE_ADMIN', roleName: '超级管理员' }
])

const queryParams = reactive({ role: '', cityId: null, status: null, keyword: '', pageNo: 1, pageSize: 20 })
const userForm = reactive({ id: null, username: '', realName: '', phone: '', roleIds: [], dataScopeType: 'PROVINCE', scopeCityId: null, scopeOutletId: null, remark: '' })

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  roleIds: [{ required: true, message: '请至少分配一个角色', trigger: 'change' }],
  dataScopeType: [{ required: true, message: '请选择数据范围策略', trigger: 'change' }]
}

// --- API 交互 ---
const fetchUserList = () => {
  loading.value = true
  getUserList(queryParams).then(res => {
    const pageData = res.data || res
    userList.value = pageData.list || pageData.records || []
    total.value = pageData.total || 0
  }).catch(() => {
    ElMessage.error('获取用户数据失败')
  }).finally(() => { loading.value = false })
}

const handleStatusChange = (row) => {
  updateUserStatus(row.id, row.status).then(() => {
    ElMessage.success(`账号已${row.status === 1 ? '成功启用' : '成功禁用'}`)
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  })
}

const handleSubmit = () => {
  userFormRef.value.validate(valid => {
    if (!valid) return
    if (userForm.dataScopeType === 'CITY' && !userForm.scopeCityId) {
      ElMessage.warning('CITY 数据权限必须指定关联的地市！')
      return
    }
    submitLoading.value = true
    const isEdit = dialogType.value === 'edit'
    const apiCall = isEdit ? updateUser(userForm.id, userForm) : createUser(userForm)

    apiCall.then(() => {
      ElMessage.success(isEdit ? '修改账号成功' : '账号创建成功')
      dialogVisible.value = false
      fetchUserList()
    }).catch(err => {
      ElMessage.error(err.response?.data?.msg || '操作失败')
    }).finally(() => { submitLoading.value = false })
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要彻底删除用户账号 [${row.username}] 吗？`, '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    deleteUser(row.id).then(() => {
      ElMessage.success('账号已成功删除')
      fetchUserList()
    })
  }).catch(() => {})
}

const handleQuery = () => { queryParams.pageNo = 1; fetchUserList(); }
const resetQuery = () => { queryParams.role = ''; queryParams.cityId = null; queryParams.status = null; queryParams.keyword = ''; handleQuery(); }

const openDialog = (type, row = null) => {
  dialogType.value = type
  dialogVisible.value = true
  if (type === 'edit' && row) {
    Object.assign(userForm, {
      id: row.id, username: row.username, realName: row.realName, phone: row.phone,
      roleIds: row.roleIds || (row.roles ? row.roles.map(r => roleOptions.value.find(o => o.roleCode === r)?.id).filter(Boolean) : []),
      dataScopeType: row.dataScopeType || 'PROVINCE', scopeCityId: row.scopeCityId, scopeOutletId: row.scopeOutletId, remark: row.remark || ''
    })
  } else {
    Object.assign(userForm, { id: null, username: '', realName: '', phone: '', roleIds: [], dataScopeType: 'PROVINCE', scopeCityId: null, scopeOutletId: null, remark: '' })
  }
}

const closeDialog = () => { if (userFormRef.value) userFormRef.value.resetFields() }
const formatRole = (code) => { const target = roleOptions.value.find(item => item.roleCode === code); return target ? target.roleName : code; }
const getScopeTypeTag = (type) => { if (type === 'PROVINCE') return 'danger'; if (type === 'CITY') return 'warning'; if (type === 'OUTLET') return 'success'; return 'info'; }

onMounted(() => { fetchUserList() })
</script>

<style scoped>
.user-management-container { display: flex; flex-direction: column; gap: 16px; height: 100%; }
.search-card { border-radius: 8px; background-color: #ffffff; }
.search-form { margin-bottom: -10px; }
.action-bar { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #e2e8f0; }
.table-card { flex: 1; border-radius: 8px; background-color: #ffffff; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.btn-icon { margin-right: 4px; }
.mx-1 { margin: 2px; }
:deep(.el-table .cell) { white-space: nowrap; }
</style>