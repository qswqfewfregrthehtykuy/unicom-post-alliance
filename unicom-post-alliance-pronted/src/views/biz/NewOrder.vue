<template>
  <div class="new-order">
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">📝 新增业务发展记录</span>
      </template>
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          style="max-width: 700px;"
      >
        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="form.businessType" placeholder="请选择业务类型">
            <el-option
              v-for="item in BUSINESS_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发展来源" prop="developSource">
          <el-select v-model="form.developSource" placeholder="请选择发展来源">
            <el-option
              v-for="item in DEVELOP_SOURCES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="客户手机" prop="customerPhone">
          <el-input v-model="form.customerPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="customerIdCard">
          <el-input v-model="form.customerIdCard" placeholder="选填" />
        </el-form-item>
        <el-form-item label="客户地址" prop="customerAddress">
          <el-input v-model="form.customerAddress" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交发展记录</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { submitOrder } from '@/api/order'
import { BUSINESS_TYPES, DEVELOP_SOURCES } from '@/constants/business'

const formRef = ref()
const submitting = ref(false)

const form = reactive({
  businessType: '',
  developSource: '',
  customerName: '',
  customerPhone: '',
  customerIdCard: '',
  customerAddress: '',
  remark: ''
})

const rules = {
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  developSource: [{ required: true, message: '请选择发展来源', trigger: 'change' }],
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  customerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await submitOrder(form)
    ElMessage.success(`发展记录提交成功，订单号：${res.data.orderNo}`)
    resetForm()
  } catch (error) {
    // 已由拦截器处理
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value.resetFields()
  Object.assign(form, {
    businessType: '',
    developSource: '',
    customerName: '',
    customerPhone: '',
    customerIdCard: '',
    customerAddress: '',
    remark: ''
  })
}
</script>

<style scoped>
.new-order {
  padding: 10px;
}
.form-card {
  max-width: 800px;
  margin: 0 auto;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}
</style>