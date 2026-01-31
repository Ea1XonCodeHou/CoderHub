<template>
  <div class="user-management">
    <div class="section-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <input 
          v-model="searchForm.username" 
          type="text" 
          placeholder="搜索用户名" 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <input 
          v-model="searchForm.phone" 
          type="text" 
          placeholder="搜索手机号" 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <select v-model="searchForm.userLevel" class="select-filter">
          <option :value="null">全部等级</option>
          <option :value="0">普通用户</option>
          <option :value="1">VIP会员</option>
          <option :value="2">SVIP会员</option>
        </select>
        <select v-model="searchForm.status" class="select-filter">
          <option :value="null">全部状态</option>
          <option :value="1">正常</option>
          <option :value="0">禁用</option>
        </select>
        <button @click="handleSearch" class="btn-search">搜索</button>
        <button @click="handleReset" class="btn-reset">重置</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 表格区域 -->
    <div v-else class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>账户编号</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>手机号</th>
            <th>会员等级</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="userList.length === 0">
            <td colspan="8" class="empty-data">
              <svg viewBox="0 0 64 41" xmlns="http://www.w3.org/2000/svg">
                <g transform="translate(0 1)" fill="none" fill-rule="evenodd">
                  <ellipse fill="#f5f5f5" cx="32" cy="33" rx="32" ry="7"></ellipse>
                  <g fill-rule="nonzero" stroke="#d9d9d9">
                    <path d="M55 12.76L44.854 1.258C44.367.474 43.656 0 42.907 0H21.093c-.749 0-1.460.474-1.947 1.257L9 12.761V22h46v-9.24z"></path>
                    <path d="M41.613 15.931c0-1.605.994-2.93 2.227-2.931H55v18.137C55 33.26 53.68 35 52.05 35h-40.1C10.32 35 9 33.259 9 31.137V13h11.16c1.233 0 2.227 1.323 2.227 2.928v.022c0 1.605 1.005 2.901 2.237 2.901h14.752c1.232 0 2.237-1.308 2.237-2.913v-.007z" fill="#fafafa"></path>
                  </g>
                </g>
              </svg>
              <p>暂无数据</p>
            </td>
          </tr>
          <tr v-for="user in userList" :key="user.id">
            <td>{{ user.account }}</td>
            <td>
              <div class="user-cell">
                <img :src="user.avatar" alt="avatar" class="table-avatar" />
                {{ user.username }}
              </div>
            </td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone }}</td>
            <td>
              <span :class="['badge', 'level-' + user.userLevel]">
                {{ getLevelText(user.userLevel) }}
              </span>
            </td>
            <td>
              <span :class="['badge', user.status === 1 ? 'success' : 'danger']">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button class="btn-action" @click="handleEdit(user)">编辑</button>
              <button 
                :class="['btn-action', user.status === 1 ? 'danger' : 'success']"
                @click="handleToggleStatus(user)"
              >
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页器 -->
      <div class="pagination">
        <div class="pagination-info">
          共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="pagination-controls">
          <button 
            :disabled="currentPage === 1" 
            @click="handlePageChange(1)"
            class="pagination-btn"
          >
            首页
          </button>
          <button 
            :disabled="currentPage === 1" 
            @click="handlePageChange(currentPage - 1)"
            class="pagination-btn"
          >
            上一页
          </button>
          
          <span class="page-number" v-for="page in visiblePages" :key="page">
            <button 
              v-if="page !== '...'" 
              :class="['pagination-btn', { 'active': page === currentPage }]"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </button>
            <span v-else class="pagination-ellipsis">...</span>
          </span>

          <button 
            :disabled="currentPage === totalPages" 
            @click="handlePageChange(currentPage + 1)"
            class="pagination-btn"
          >
            下一页
          </button>
          <button 
            :disabled="currentPage === totalPages" 
            @click="handlePageChange(totalPages)"
            class="pagination-btn"
          >
            末页
          </button>

          <select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
            <option :value="100">100条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click.self="closeEditDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>编辑用户信息</h3>
          <button @click="closeEditDialog" class="close-btn">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input 
              v-model="editForm.username" 
              type="text" 
              class="form-input"
              placeholder="请输入用户名"
            />
          </div>
          <div class="form-group">
            <label>手机号 <span class="required">*</span></label>
            <input 
              v-model="editForm.phone" 
              type="text" 
              class="form-input"
              placeholder="请输入手机号"
            />
          </div>
          <div class="form-group">
            <label>邮箱 <span class="required">*</span></label>
            <input 
              v-model="editForm.email" 
              type="email" 
              class="form-input"
              placeholder="请输入邮箱"
            />
          </div>
          <div class="form-group">
            <label>会员等级 <span class="required">*</span></label>
            <select v-model="editForm.userLevel" class="form-select">
              <option :value="0">普通用户</option>
              <option :value="1">VIP会员</option>
              <option :value="2">SVIP会员</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="closeEditDialog" class="btn-cancel">取消</button>
          <button @click="handleSaveEdit" class="btn-confirm" :disabled="editLoading">
            {{ editLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserPage, updateUser, updateUserStatus } from '@/api/admin'

// 数据状态
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = ref({
  username: '',
  phone: '',
  email: '',
  account: '',
  userLevel: null,
  status: null
})

// 编辑对话框状态
const showEditDialog = ref(false)
const editLoading = ref(false)
const editForm = ref({
  userId: '',
  username: '',
  phone: '',
  email: '',
  userLevel: 0
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value) || 1
})

// 计算可见的页码
const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 7 // 最多显示7个页码
  const half = Math.floor(maxVisible / 2)
  
  let start = Math.max(1, currentPage.value - half)
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  // 调整起始位置
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  // 添加首页和省略号
  if (start > 1) {
    pages.push(1)
    if (start > 2) pages.push('...')
  }
  
  // 添加中间页码
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  // 添加省略号和末页
  if (end < totalPages.value) {
    if (end < totalPages.value - 1) pages.push('...')
    pages.push(totalPages.value)
  }
  
  return pages
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm.value
    }
    
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const response = await getUserPage(params)
    userList.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('获取用户列表失败：', error)
    alert('获取用户列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    username: '',
    phone: '',
    email: '',
    account: '',
    userLevel: null,
    status: null
  }
  currentPage.value = 1
  fetchUserList()
}

// 翻页
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchUserList()
}

// 修改每页条数
const handlePageSizeChange = () => {
  currentPage.value = 1
  fetchUserList()
}

// 编辑用户
const handleEdit = (user) => {
  // 填充表单数据
  editForm.value = {
    userId: user.id,
    username: user.username,
    phone: user.phone,
    email: user.email,
    userLevel: user.userLevel
  }
  showEditDialog.value = true
}

// 关闭编辑对话框
const closeEditDialog = () => {
  showEditDialog.value = false
  editForm.value = {
    userId: '',
    username: '',
    phone: '',
    email: '',
    userLevel: 0
  }
}

// 保存编辑
const handleSaveEdit = async () => {
  // 表单验证
  if (!editForm.value.username || !editForm.value.username.trim()) {
    alert('请输入用户名')
    return
  }
  
  if (!editForm.value.phone || !editForm.value.phone.trim()) {
    alert('请输入手机号')
    return
  }
  
  // 手机号格式验证
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(editForm.value.phone)) {
    alert('手机号格式不正确')
    return
  }
  
  if (!editForm.value.email || !editForm.value.email.trim()) {
    alert('请输入邮箱')
    return
  }
  
  // 邮箱格式验证
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(editForm.value.email)) {
    alert('邮箱格式不正确')
    return
  }
  
  try {
    editLoading.value = true
    await updateUser(editForm.value)
    alert('用户信息修改成功')
    closeEditDialog()
    // 刷新列表
    fetchUserList()
  } catch (error) {
    console.error('编辑用户失败：', error)
    alert('编辑用户失败：' + (error.message || '未知错误'))
  } finally {
    editLoading.value = false
  }
}

// 切换状态
const handleToggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  if (!confirm(`确定要${action}用户 ${user.username} 吗？`)) {
    return
  }
  
  try {
    loading.value = true
    await updateUserStatus(user.id, newStatus)
    alert(`用户已${action}`)
    // 刷新列表
    fetchUserList()
  } catch (error) {
    console.error('修改用户状态失败：', error)
    alert('修改用户状态失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  
  let date
  
  // 处理数组格式：[2025, 1, 20, 14, 30, 45]
  if (Array.isArray(dateString)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateString
    date = new Date(year, month - 1, day, hour, minute, second)
  } 
  // 处理字符串格式
  else if (typeof dateString === 'string') {
    // 处理 ISO 格式：2025-01-20T14:30:45
    date = new Date(dateString.replace('T', ' ').replace(/\.\d+$/, ''))
  } 
  // 处理时间戳
  else if (typeof dateString === 'number') {
    date = new Date(dateString)
  } 
  else {
    return '-'
  }
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.error('无效的日期格式：', dateString)
    return '-'
  }
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取等级文本
const getLevelText = (level) => {
  const levels = { 0: '普通', 1: 'VIP', 2: 'SVIP' }
  return levels[level] || '普通'
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-management {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input,
.select-filter {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.search-input {
  width: 160px;
}

.search-input:focus,
.select-filter:focus {
  outline: none;
  border-color: #2c3e50;
}

.btn-search,
.btn-reset {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-search {
  background: #2c3e50;
  color: white;
}

.btn-search:hover {
  background: #34495e;
}

.btn-reset {
  background: #ecf0f1;
  color: #2c3e50;
}

.btn-reset:hover {
  background: #bdc3c7;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #2c3e50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  margin-top: 16px;
  color: #95a5a6;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: #f8f9fa;
}

.data-table th {
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  border-bottom: 2px solid #ecf0f1;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #ecf0f1;
  font-size: 14px;
  color: #34495e;
}

.data-table tbody tr:hover {
  background: #f8f9fa;
}

.empty-data {
  text-align: center;
  padding: 60px 20px !important;
  color: #95a5a6;
}

.empty-data svg {
  width: 64px;
  height: 41px;
  margin-bottom: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge.success {
  background: #d4edda;
  color: #155724;
}

.badge.danger {
  background: #f8d7da;
  color: #721c24;
}

.badge.level-0 {
  background: #e3f2fd;
  color: #1565c0;
}

.badge.level-1 {
  background: #fff3e0;
  color: #e65100;
}

.badge.level-2 {
  background: #fce4ec;
  color: #c2185b;
}

.btn-action {
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: #ecf0f1;
  color: #2c3e50;
  margin-right: 8px;
}

.btn-action:hover {
  background: #bdc3c7;
}

.btn-action.danger {
  background: #fee;
  color: #c0392b;
}

.btn-action.danger:hover {
  background: #fdd;
}

.btn-action.success {
  background: #d4edda;
  color: #155724;
}

.btn-action.success:hover {
  background: #c3e6cb;
}

/* 分页器 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #ecf0f1;
}

.pagination-info {
  font-size: 14px;
  color: #34495e;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: #34495e;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #2c3e50;
  color: #2c3e50;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: #2c3e50;
  color: white;
  border-color: #2c3e50;
}

.pagination-ellipsis {
  padding: 0 8px;
  color: #95a5a6;
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  background: white;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.dialog-container {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ecf0f1;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #95a5a6;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #ecf0f1;
  color: #2c3e50;
}

.dialog-body {
  padding: 24px;
  max-height: calc(90vh - 160px);
  overflow-y: auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.required {
  color: #e74c3c;
}

.form-input,
.form-select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #2c3e50;
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #ecf0f1;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.btn-cancel {
  background: #ecf0f1;
  color: #2c3e50;
}

.btn-cancel:hover {
  background: #bdc3c7;
}

.btn-confirm {
  background: #2c3e50;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #34495e;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(44, 62, 80, 0.2);
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 1200px) {
  .search-input {
    width: 140px;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .search-input {
    flex: 1;
    min-width: 120px;
  }

  .pagination {
    flex-direction: column;
    gap: 12px;
  }

  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }

  .dialog-container {
    width: 95%;
  }

  .dialog-body {
    padding: 16px;
  }
}
</style>

