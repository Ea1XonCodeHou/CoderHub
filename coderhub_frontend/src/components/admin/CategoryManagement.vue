<template>
  <div class="category-management">
    <div class="section-header">
      <h2>分类管理</h2>
      <div class="header-actions">
        <button @click="handleAdd" class="btn-add">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          新增分类
        </button>
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
            <th>分类名称</th>
            <th>父分类</th>
            <th>排序</th>
            <th>描述</th>
            <th>文章数</th>
            <th>阅读量</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="categoryList.length === 0">
            <td colspan="9" class="empty-data">
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
          <!-- 一级分类 -->
          <template v-for="category in rootCategories" :key="category.id">
            <tr class="root-category">
              <td>
                <div class="category-cell">
                  <svg v-if="hasChildren(category.id)" 
                       @click="toggleExpand(category.id)" 
                       class="expand-icon" 
                       :class="{ 'expanded': isExpanded(category.id) }"
                       viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <strong>{{ category.categoryName }}</strong>
                </div>
              </td>
              <td>-</td>
              <td>{{ category.sortOrder }}</td>
              <td>{{ category.description || '-' }}</td>
              <td>{{ category.articleCount }}</td>
              <td>{{ formatNumber(category.viewCount) }}</td>
              <td>
                <span :class="['badge', category.status === 1 ? 'success' : 'danger']">
                  {{ category.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(category.createTime) }}</td>
              <td>
                <button class="btn-action" @click="handleEdit(category)">编辑</button>
                <button 
                  :class="['btn-action', category.status === 1 ? 'danger' : 'success']"
                  @click="handleToggleStatus(category)"
                >
                  {{ category.status === 1 ? '禁用' : '启用' }}
                </button>
                <button class="btn-action danger" @click="handleDelete(category)">删除</button>
              </td>
            </tr>
            <!-- 二级分类 -->
            <tr v-if="isExpanded(category.id)" 
                v-for="subCategory in getSubCategories(category.id)" 
                :key="subCategory.id"
                class="sub-category">
              <td>
                <div class="category-cell sub">
                  {{ subCategory.categoryName }}
                </div>
              </td>
              <td>{{ subCategory.parentName }}</td>
              <td>{{ subCategory.sortOrder }}</td>
              <td>{{ subCategory.description || '-' }}</td>
              <td>{{ subCategory.articleCount }}</td>
              <td>{{ formatNumber(subCategory.viewCount) }}</td>
              <td>
                <span :class="['badge', subCategory.status === 1 ? 'success' : 'danger']">
                  {{ subCategory.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(subCategory.createTime) }}</td>
              <td>
                <button class="btn-action" @click="handleEdit(subCategory)">编辑</button>
                <button 
                  :class="['btn-action', subCategory.status === 1 ? 'danger' : 'success']"
                  @click="handleToggleStatus(subCategory)"
                >
                  {{ subCategory.status === 1 ? '禁用' : '启用' }}
                </button>
                <button class="btn-action danger" @click="handleDelete(subCategory)">删除</button>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑分类' : '新增分类' }}</h3>
          <button @click="closeDialog" class="close-btn">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>分类名称 <span class="required">*</span></label>
            <input 
              v-model="form.categoryName" 
              type="text" 
              class="form-input"
              placeholder="请输入分类名称"
            />
          </div>
          <div class="form-group">
            <label>父分类</label>
            <select v-model="form.parentId" class="form-select">
              <option :value="null">无（设为一级分类）</option>
              <option v-for="category in rootCategories" :key="category.id" :value="category.id">
                {{ category.categoryName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>排序序号</label>
            <input 
              v-model.number="form.sortOrder" 
              type="number" 
              class="form-input"
              placeholder="数字越小越靠前"
            />
          </div>
          <div class="form-group">
            <label>分类描述</label>
            <textarea 
              v-model="form.description" 
              class="form-textarea"
              placeholder="请输入分类描述"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="form.status" class="form-select">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="closeDialog" class="btn-cancel">取消</button>
          <button @click="handleSave" class="btn-confirm" :disabled="saveLoading">
            {{ saveLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  getAllCategories, 
  getRootCategories, 
  addCategory, 
  updateCategory, 
  deleteCategory, 
  updateCategoryStatus 
} from '@/api/admin'

// 数据状态
const loading = ref(false)
const categoryList = ref([])
const expandedIds = ref([])

// 对话框状态
const showDialog = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const form = ref({
  id: null,
  categoryName: '',
  parentId: null,
  sortOrder: 0,
  description: '',
  status: 1
})

// 计算属性：一级分类
const rootCategories = computed(() => {
  return categoryList.value.filter(c => !c.parentId)
})

// 获取所有分类
const fetchCategories = async () => {
  try {
    loading.value = true
    const response = await getAllCategories()
    categoryList.value = response.data
  } catch (error) {
    console.error('获取分类列表失败：', error)
    alert('获取分类列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 判断是否有子分类
const hasChildren = (parentId) => {
  return categoryList.value.some(c => c.parentId === parentId)
}

// 获取子分类
const getSubCategories = (parentId) => {
  return categoryList.value.filter(c => c.parentId === parentId)
}

// 切换展开/收起
const toggleExpand = (id) => {
  const index = expandedIds.value.indexOf(id)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(id)
  }
}

// 判断是否展开
const isExpanded = (id) => {
  return expandedIds.value.includes(id)
}

// 新增分类
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    id: null,
    categoryName: '',
    parentId: null,
    sortOrder: 0,
    description: '',
    status: 1
  }
  showDialog.value = true
}

// 编辑分类
const handleEdit = (category) => {
  isEdit.value = true
  form.value = {
    id: category.id,
    categoryName: category.categoryName,
    parentId: category.parentId || null,
    sortOrder: category.sortOrder,
    description: category.description,
    status: category.status
  }
  showDialog.value = true
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
  form.value = {
    id: null,
    categoryName: '',
    parentId: null,
    sortOrder: 0,
    description: '',
    status: 1
  }
}

// 保存
const handleSave = async () => {
  // 验证
  if (!form.value.categoryName || !form.value.categoryName.trim()) {
    alert('请输入分类名称')
    return
  }

  try {
    saveLoading.value = true
    if (isEdit.value) {
      await updateCategory(form.value)
      alert('分类更新成功')
    } else {
      await addCategory(form.value)
      alert('分类新增成功')
    }
    closeDialog()
    fetchCategories()
  } catch (error) {
    console.error('保存分类失败：', error)
    alert('保存分类失败：' + (error.message || '未知错误'))
  } finally {
    saveLoading.value = false
  }
}

// 切换状态
const handleToggleStatus = async (category) => {
  const newStatus = category.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  if (!confirm(`确定要${action}分类 ${category.categoryName} 吗？`)) {
    return
  }
  
  try {
    await updateCategoryStatus(category.id, newStatus)
    alert(`分类已${action}`)
    fetchCategories()
  } catch (error) {
    console.error('修改分类状态失败：', error)
    alert('修改分类状态失败：' + (error.message || '未知错误'))
  }
}

// 删除分类
const handleDelete = async (category) => {
  if (!confirm(`确定要删除分类 ${category.categoryName} 吗？\n注意：删除后不可恢复！`)) {
    return
  }
  
  try {
    await deleteCategory(category.id)
    alert('分类删除成功')
    fetchCategories()
  } catch (error) {
    console.error('删除分类失败：', error)
    alert('删除分类失败：' + (error.message || '未知错误'))
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  
  let date
  
  if (Array.isArray(dateString)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateString
    date = new Date(year, month - 1, day, hour, minute, second)
  } else if (typeof dateString === 'string') {
    date = new Date(dateString.replace('T', ' ').replace(/\.\d+$/, ''))
  } else if (typeof dateString === 'number') {
    date = new Date(dateString)
  } else {
    return '-'
  }
  
  if (isNaN(date.getTime())) {
    return '-'
  }
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num || 0
}

// 页面加载
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-management {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add:hover {
  background: #34495e;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(44, 62, 80, 0.2);
}

.btn-add svg {
  width: 18px;
  height: 18px;
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

.root-category {
  background: #f8f9fa;
}

.root-category:hover {
  background: #ecf0f1;
}

.sub-category {
  background: white;
}

.sub-category:hover {
  background: #f8f9fa;
}

.category-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-cell.sub {
  padding-left: 24px;
}

.expand-icon {
  width: 16px;
  height: 16px;
  cursor: pointer;
  transition: transform 0.2s;
  color: #2c3e50;
}

.expand-icon.expanded {
  transform: rotate(90deg);
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
  max-width: 600px;
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
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-textarea {
  font-family: inherit;
  resize: vertical;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
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
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dialog-container {
    width: 95%;
  }

  .dialog-body {
    padding: 16px;
  }
}
</style>

