<template>
  <div class="tutorial-management">
    <!-- Toast 提示组件 -->
    <Toast 
      v-model:show="showToast"
      :type="toastConfig.type"
      :title="toastConfig.title"
      :message="toastConfig.message"
    />
    
    <!-- 删除确认对话框 -->
    <div v-if="showDeleteConfirm" class="dialog-overlay" @click.self="showDeleteConfirm = false">
      <div class="confirm-dialog">
        <div class="confirm-icon danger">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3 class="confirm-title">确认删除教程？</h3>
        <p class="confirm-message">
          您即将删除教程《<strong>{{ deletingTutorial?.title }}</strong>》<br/>
          此操作将会<span class="text-danger">级联删除所有章节、文档和视频资源</span>！
        </p>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
          <button class="btn-danger" @click="confirmDelete" :disabled="isDeleting">
            <span v-if="!isDeleting">确认删除</span>
            <span v-else>删除中...</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 删除结果对话框 -->
    <div v-if="showDeleteResult" class="dialog-overlay" @click.self="showDeleteResult = false">
      <div class="result-dialog">
        <div class="result-icon success">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3 class="result-title">删除成功</h3>
        <div class="result-details">
          <div class="result-item">
            <span class="result-label">教程名称：</span>
            <span class="result-value">{{ deleteResult?.tutorialTitle }}</span>
          </div>
          <div class="result-stats">
            <div class="stat-box">
              <div class="stat-value">{{ deleteResult?.chapterCount || 0 }}</div>
              <div class="stat-label">章节</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ deleteResult?.documentCount || 0 }}</div>
              <div class="stat-label">文档</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ deleteResult?.videoCount || 0 }}</div>
              <div class="stat-label">视频</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ deleteResult?.ossFileCount || 0 }}</div>
              <div class="stat-label">文件</div>
            </div>
          </div>
          <div class="result-summary">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M13 16H12V12H11M12 8H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>共清理 <strong>{{ formatFileSize(deleteResult?.totalFileSize || 0) }}</strong> 的云存储空间</p>
          </div>
        </div>
        <button class="btn-primary" @click="closeDeleteResult">我知道了</button>
      </div>
    </div>
    
    <!-- 头部操作栏 -->
    <div class="section-header">
      <h2>教程管理</h2>
      <button class="btn-primary" @click="showCreateDialog = true">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        新增教程
      </button>
    </div>

    <!-- 教程卡片网格 -->
    <div v-if="tutorials.length > 0" class="tutorial-grid">
      <div v-for="tutorial in tutorials" :key="tutorial.id" class="tutorial-card" @click="$emit('view-detail', tutorial)">
        <!-- 封面图 -->
        <div class="tutorial-cover">
          <img :src="tutorial.coverImage" :alt="tutorial.title" />
          <div class="tutorial-overlay">
            <span class="price-tag" v-if="tutorial.price > 0">¥{{ tutorial.price }}</span>
            <span class="price-tag free" v-else>免费</span>
          </div>
        </div>

        <!-- 教程信息 -->
        <div class="tutorial-info">
          <h3 class="tutorial-title">{{ tutorial.title }}</h3>
          <p class="tutorial-desc">{{ tutorial.description }}</p>

          <!-- 讲师信息 -->
          <div class="instructor-info">
            <img :src="tutorial.instructorAvatar" :alt="tutorial.instructorName" class="instructor-avatar" />
            <div class="instructor-details">
              <span class="instructor-name">{{ tutorial.instructorName }}</span>
              <span class="instructor-desc">{{ tutorial.instructorDesc }}</span>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="tutorial-stats">
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span>{{ tutorial.viewCount }}</span>
            </div>
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20.84 4.61C20.3292 4.099 19.7228 3.69365 19.0554 3.41708C18.3879 3.14052 17.6725 2.99817 16.95 2.99817C16.2275 2.99817 15.5121 3.14052 14.8446 3.41708C14.1772 3.69365 13.5708 4.099 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99871 7.05 2.99871C5.59096 2.99871 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54871 7.04097 1.54871 8.5C1.54871 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.351 11.8792 21.7563 11.2728 22.0329 10.6053C22.3095 9.93789 22.4518 9.22248 22.4518 8.5C22.4518 7.77752 22.3095 7.0621 22.0329 6.39464C21.7563 5.72718 21.351 5.12075 20.84 4.61Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>{{ tutorial.likeCount }}</span>
            </div>
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span>{{ tutorial.studentCount }}</span>
            </div>
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="currentColor"/>
              </svg>
              <span>{{ tutorial.rating }}</span>
            </div>
          </div>

          <!-- 底部标签和操作 -->
          <div class="tutorial-footer">
            <div class="tags">
              <span class="difficulty-tag" :class="getDifficultyClass(tutorial.difficulty)">
                {{ getDifficultyText(tutorial.difficulty) }}
              </span>
              <span class="chapter-tag">{{ tutorial.chapterCount }} 章节</span>
            </div>
            <div class="actions">
              <button class="btn-icon" @click.stop="handleEdit(tutorial)" title="编辑">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <button class="btn-icon danger" @click.stop="handleDelete(tutorial)" title="删除">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 6H5H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M2 3H8C9.06087 3 10.0783 3.42143 10.8284 4.17157C11.5786 4.92172 12 5.93913 12 7V21C12 20.2044 11.6839 19.4413 11.1213 18.8787C10.5587 18.3161 9.79565 18 9 18H2V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M22 3H16C14.9391 3 13.9217 3.42143 13.1716 4.17157C12.4214 4.92172 12 5.93913 12 7V21C12 20.2044 12.3161 19.4413 12.8787 18.8787C13.4413 18.3161 14.2044 18 15 18H22V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p>暂无教程数据</p>
      <button class="btn-primary" @click="showCreateDialog = true">创建第一个教程</button>
    </div>

    <!-- 新增/编辑教程对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>{{ isEditMode ? '编辑教程' : '新增教程' }}</h3>
          <button class="btn-close" @click="closeDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <div class="dialog-body">
          <form @submit.prevent="handleSubmit">
            <!-- 基本信息 -->
            <div class="form-section">
              <h4 class="section-title">基本信息</h4>
              
              <div class="form-row">
                <div class="form-group full-width">
                  <label class="required">教程标题</label>
                  <input 
                    v-model="formData.title" 
                    type="text" 
                    class="form-input" 
                    placeholder="请输入教程标题"
                    required
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group full-width">
                  <label class="required">教程简介</label>
                  <textarea 
                    v-model="formData.description" 
                    class="form-textarea" 
                    rows="4"
                    placeholder="请输入教程简介"
                    required
                  ></textarea>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="required">分类</label>
                  <select v-model="formData.categoryId" class="form-select" required>
                    <option value="">请选择分类</option>
                    <option v-for="category in categories" :key="category.id" :value="category.id">
                      {{ category.categoryName }}
                    </option>
                  </select>
                </div>

                <div class="form-group">
                  <label class="required">难度等级</label>
                  <select v-model.number="formData.difficulty" class="form-select" required>
                    <option :value="0">入门</option>
                    <option :value="1">进阶</option>
                    <option :value="2">高级</option>
                  </select>
                </div>

                <div class="form-group">
                  <label class="required">价格（元）</label>
                  <input 
                    v-model.number="formData.price" 
                    type="number" 
                    min="0" 
                    step="0.01"
                    class="form-input" 
                    placeholder="0表示免费"
                    required
                  />
                </div>
              </div>

              <!-- 封面图上传 -->
              <div class="form-row">
                <div class="form-group full-width">
                  <label class="required">教程封面</label>
                  <div class="upload-area">
                    <div v-if="formData.coverImage" class="image-preview">
                      <img :src="formData.coverImage" alt="封面" />
                      <button type="button" class="btn-remove" @click="formData.coverImage = ''">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </button>
                    </div>
                    <div v-else class="upload-placeholder" @click="triggerFileUpload('cover')">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15V19C21 19.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M17 8L12 3L7 8M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                      <p>点击上传封面图</p>
                      <span>建议尺寸 400x250，支持 JPG、PNG</span>
                    </div>
                    <input 
                      ref="coverInput" 
                      type="file" 
                      accept="image/*" 
                      style="display: none" 
                      @change="handleCoverUpload"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- 讲师信息 -->
            <div class="form-section">
              <h4 class="section-title">讲师信息</h4>
              
              <!-- 讲师头像和信息横向布局 -->
              <div class="instructor-form-layout">
                <!-- 左侧：头像上传 -->
                <div class="instructor-avatar-section">
                  <label class="required">讲师头像</label>
                  <div class="upload-area avatar-upload">
                    <div v-if="formData.instructorAvatar" class="image-preview avatar-preview">
                      <img :src="formData.instructorAvatar" alt="讲师头像" />
                      <button type="button" class="btn-remove" @click="formData.instructorAvatar = ''">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </button>
                    </div>
                    <div v-else class="upload-placeholder" @click="triggerFileUpload('avatar')">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 14C8 14 9.5 16 12 16C14.5 16 16 14 16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="9" cy="9" r="1" fill="currentColor"/>
                        <circle cx="15" cy="9" r="1" fill="currentColor"/>
                      </svg>
                      <p>上传头像</p>
                      <span>200x200</span>
                    </div>
                    <input 
                      ref="avatarInput" 
                      type="file" 
                      accept="image/*" 
                      style="display: none" 
                      @change="handleAvatarUpload"
                    />
                  </div>
                </div>

                <!-- 右侧：姓名和简介 -->
                <div class="instructor-info-section">
                  <div class="form-group">
                    <label class="required">讲师姓名</label>
                    <input 
                      v-model="formData.instructorName" 
                      type="text" 
                      class="form-input" 
                      placeholder="请输入讲师姓名"
                      required
                    />
                  </div>

                  <div class="form-group">
                    <label class="required">讲师简介</label>
                    <textarea 
                      v-model="formData.instructorDesc" 
                      class="form-textarea" 
                      rows="3"
                      placeholder="请输入讲师简介"
                      required
                    ></textarea>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部操作按钮 -->
            <div class="dialog-footer">
              <button type="button" class="btn-cancel" @click="closeDialog">取消</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting">
                <span v-if="!isSubmitting">{{ isEditMode ? '保存修改' : '创建教程' }}</span>
                <span v-else>提交中...</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { createTutorial, deleteTutorial, getAllCategories, getTutorialList, updateTutorial } from '@/api/admin'
import { uploadFile } from '@/api/user'
import Toast from '@/components/Toast.vue'
import { onMounted, ref } from 'vue'

// 数据
const tutorials = ref([])
const categories = ref([])
const showCreateDialog = ref(false)
const isEditMode = ref(false)
const isSubmitting = ref(false)
const coverInput = ref(null)
const avatarInput = ref(null)

// 删除确认相关
const showDeleteConfirm = ref(false)
const deletingTutorial = ref(null)
const isDeleting = ref(false)

// 删除结果相关
const showDeleteResult = ref(false)
const deleteResult = ref(null)

// Toast 提示
const showToast = ref(false)
const toastConfig = ref({
  type: 'success',
  title: '成功',
  message: ''
})

const showMessage = (type, title, message) => {
  toastConfig.value = { type, title, message }
  showToast.value = true
}

// 表单数据
const formData = ref({
  id: '',
  title: '',
  description: '',
  coverImage: '',
  categoryId: '',
  difficulty: 0,
  instructorName: '',
  instructorAvatar: '',
  instructorDesc: '',
  price: 0
})

// 获取教程列表
const fetchTutorials = async () => {
  try {
    const res = await getTutorialList({ status: 1 })
    // res.data 包含 {total, records}
    tutorials.value = res.data.records || []
    console.log('获取到教程列表：', tutorials.value)
  } catch (error) {
    console.error('获取教程列表失败：', error)
    showMessage('error', '获取失败', '无法加载教程列表，请刷新重试')
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败：', error)
  }
}

// 难度文本
const getDifficultyText = (difficulty) => {
  const map = { 0: '入门', 1: '进阶', 2: '高级' }
  return map[difficulty] || '未知'
}

// 难度样式类
const getDifficultyClass = (difficulty) => {
  const map = { 0: 'beginner', 1: 'intermediate', 2: 'advanced' }
  return map[difficulty] || 'beginner'
}

// 触发文件上传
const triggerFileUpload = (type) => {
  if (type === 'cover') {
    coverInput.value.click()
  } else {
    avatarInput.value.click()
  }
}

// 上传封面图
const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const res = await uploadFile(file)
    formData.value.coverImage = res.data
    showMessage('success', '上传成功', '封面图已上传')
  } catch (error) {
    console.error('封面上传失败：', error)
    showMessage('error', '上传失败', '封面图上传失败，请重试')
  }
}

// 上传讲师头像
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const res = await uploadFile(file)
    formData.value.instructorAvatar = res.data
    showMessage('success', '上传成功', '讲师头像已上传')
  } catch (error) {
    console.error('讲师头像上传失败：', error)
    showMessage('error', '上传失败', '讲师头像上传失败，请重试')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (isSubmitting.value) return

  // 表单验证
  if (!formData.value.title || !formData.value.description) {
    showMessage('warning', '提示', '请填写完整的教程信息')
    return
  }

  if (!formData.value.coverImage) {
    showMessage('warning', '提示', '请上传教程封面')
    return
  }

  if (!formData.value.instructorAvatar) {
    showMessage('warning', '提示', '请上传讲师头像')
    return
  }

  isSubmitting.value = true

  try {
    if (isEditMode.value) {
      await updateTutorial(formData.value)
      showMessage('success', '更新成功', '教程已成功更新')
    } else {
      await createTutorial(formData.value)
      showMessage('success', '创建成功', '教程已成功创建')
    }
    closeDialog()
    fetchTutorials()
  } catch (error) {
    console.error('提交失败：', error)
    showMessage('error', '操作失败', isEditMode.value ? '教程更新失败，请重试' : '教程创建失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 编辑教程
const handleEdit = (tutorial) => {
  isEditMode.value = true
  formData.value = { ...tutorial }
  showCreateDialog.value = true
}

// 删除教程 - 打开确认对话框
const handleDelete = (tutorial) => {
  deletingTutorial.value = tutorial
  showDeleteConfirm.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (isDeleting.value) return
  
  isDeleting.value = true
  try {
    const res = await deleteTutorial(deletingTutorial.value.id)
    deleteResult.value = res.data
    showDeleteConfirm.value = false
    showDeleteResult.value = true
    fetchTutorials()
  } catch (error) {
    console.error('删除失败：', error)
    showMessage('error', '删除失败', error.message || '删除教程失败，请重试')
    showDeleteConfirm.value = false
  } finally {
    isDeleting.value = false
  }
}

// 关闭删除结果对话框
const closeDeleteResult = () => {
  showDeleteResult.value = false
  deleteResult.value = null
  deletingTutorial.value = null
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 关闭对话框
const closeDialog = () => {
  showCreateDialog.value = false
  isEditMode.value = false
  formData.value = {
    id: '',
    title: '',
    description: '',
    coverImage: '',
    categoryId: '',
    difficulty: 0,
    instructorName: '',
    instructorAvatar: '',
    instructorDesc: '',
    price: 0
  }
}

// 页面加载
onMounted(() => {
  fetchTutorials()
  fetchCategories()
})
</script>

<style scoped>
.tutorial-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.section-header h2 {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 700;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.2);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(44, 62, 80, 0.3);
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
}

.btn-primary svg {
  width: 20px;
  height: 20px;
}

/* 教程卡片网格 */
.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.tutorial-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.tutorial-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  border-color: #64748b;
}

/* 封面图 */
.tutorial-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
}

.tutorial-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.tutorial-card:hover .tutorial-cover img {
  transform: scale(1.1);
}

.tutorial-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
}

.price-tag {
  padding: 6px 14px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.price-tag.free {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

/* 教程信息 */
.tutorial-info {
  padding: 20px;
}

.tutorial-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.tutorial-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 讲师信息 */
.instructor-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.instructor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.instructor-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instructor-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
}

.instructor-desc {
  font-size: 13px;
  color: #94a3b8;
}

/* 统计信息 */
.tutorial-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.stat-item svg {
  width: 16px;
  height: 16px;
}

/* 底部标签和操作 */
.tutorial-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags {
  display: flex;
  gap: 8px;
}

.difficulty-tag,
.chapter-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.difficulty-tag.beginner {
  background: #e0f2fe;
  color: #0c4a6e;
  border: 1px solid #bae6fd;
}

.difficulty-tag.intermediate {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.difficulty-tag.advanced {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.chapter-tag {
  background: #e2e8f0;
  color: #64748b;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 36px;
  height: 36px;
  padding: 8px;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
  transform: translateY(-2px);
}

.btn-icon.danger:hover {
  background: #ef4444;
  border-color: #ef4444;
  color: white;
}

.btn-icon svg {
  width: 18px;
  height: 18px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #94a3b8;
}

.empty-state svg {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  color: #cbd5e1;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 24px;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-container {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 2px solid #e2e8f0;
}

.dialog-header h3 {
  font-size: 22px;
  color: #2c3e50;
  font-weight: 700;
}

.btn-close {
  width: 36px;
  height: 36px;
  background: #f5f7fa;
  border: none;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  background: #e2e8f0;
  color: #2c3e50;
}

.btn-close svg {
  width: 20px;
  height: 20px;
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

/* 表单部分 */
.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #64748b;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  flex: none;
  width: 100%;
}

.form-group.flex-2 {
  flex: 2;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-group label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 12px 16px;
  font-size: 14px;
  color: #2c3e50;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 8px;
  outline: none;
  transition: all 0.2s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

/* 讲师信息布局 */
.instructor-form-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.instructor-avatar-section {
  flex-shrink: 0;
  width: 150px;
}

.instructor-avatar-section label {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  display: block;
}

.instructor-avatar-section label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.instructor-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.instructor-info-section .form-group {
  margin-bottom: 0;
}

/* 上传区域 */
.upload-area {
  width: 100%;
}

.upload-area.avatar-upload {
  width: 150px;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e2e8f0;
}

.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 50%;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-remove:hover {
  background: #ef4444;
}

.btn-remove svg {
  width: 16px;
  height: 16px;
}

.upload-placeholder {
  width: 100%;
  height: 200px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f5f7fa;
}

.avatar-upload .upload-placeholder {
  width: 150px;
  height: 150px;
  border-radius: 50%;
}

.avatar-upload .upload-placeholder {
  width: 150px;
  height: 150px;
  border-radius: 50%;
}

.upload-placeholder:hover {
  border-color: #667eea;
  background: white;
}

.upload-placeholder svg {
  width: 48px;
  height: 48px;
  color: #94a3b8;
}

.upload-placeholder p {
  font-size: 15px;
  color: #64748b;
  font-weight: 500;
  margin: 0;
}

.upload-placeholder span {
  font-size: 13px;
  color: #94a3b8;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 32px;
  border-top: 2px solid #e2e8f0;
  background: #f5f7fa;
}

.btn-cancel,
.btn-submit {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  color: #64748b;
  border: 2px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f5f7fa;
  border-color: #cbd5e1;
}

.btn-submit {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.2);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(44, 62, 80, 0.3);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 删除确认对话框 */
.confirm-dialog {
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 480px;
  width: 90%;
  text-align: center;
  animation: slideUp 0.3s;
}

.confirm-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-icon.danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.confirm-icon svg {
  width: 48px;
  height: 48px;
}

.confirm-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.confirm-message {
  font-size: 15px;
  color: #64748b;
  line-height: 1.8;
  margin: 0 0 32px 0;
}

.confirm-message strong {
  color: #1e293b;
  font-weight: 600;
}

.text-danger {
  color: #dc2626;
  font-weight: 600;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-danger {
  padding: 12px 32px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 删除结果对话框 */
.result-dialog {
  background: white;
  border-radius: 16px;
  padding: 40px;
  max-width: 560px;
  width: 90%;
  text-align: center;
  animation: slideUp 0.3s;
}

.result-icon {
  width: 90px;
  height: 90px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-icon.success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.result-icon svg {
  width: 52px;
  height: 52px;
}

.result-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 32px 0;
}

.result-details {
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.result-label {
  font-size: 14px;
  color: #64748b;
}

.result-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-box {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 8px;
  transition: all 0.2s;
}

.stat-box:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #059669;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.result-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 8px;
}

.result-summary svg {
  width: 20px;
  height: 20px;
  color: #92400e;
  flex-shrink: 0;
}

.result-summary p {
  font-size: 14px;
  color: #78350f;
  margin: 0;
}

.result-summary strong {
  color: #92400e;
  font-weight: 700;
}

/* 响应式 */
@media (max-width: 768px) {
  .tutorial-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    flex-direction: column;
  }

  .dialog-container {
    max-width: 100%;
    margin: 0 10px;
  }

  .dialog-body {
    padding: 20px;
  }
}
</style>
