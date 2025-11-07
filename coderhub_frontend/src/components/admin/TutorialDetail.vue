<template>
  <div class="tutorial-detail">
    <!-- Toast 提示 -->
    <Toast 
      v-model:show="showToast"
      :type="toastConfig.type"
      :title="toastConfig.title"
      :message="toastConfig.message"
    />

    <!-- 头部操作栏 -->
    <div class="detail-header">
      <div class="header-left">
        <button class="btn-back" @click="$emit('back')">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h2>教程详情管理</h2>
      </div>
      <button class="btn-primary" @click="showCreateChapterDialog = true">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        新增章节
      </button>
    </div>

    <!-- 教程信息卡片 -->
    <div class="tutorial-info-card">
      <div class="info-cover">
        <img :src="tutorial.coverImage" :alt="tutorial.title" />
      </div>
      <div class="info-content">
        <h3 class="info-title">{{ tutorial.title }}</h3>
        <p class="info-desc">{{ tutorial.description }}</p>
        <div class="info-meta">
          <div class="meta-item">
            <span class="label">讲师：</span>
            <span class="value">{{ tutorial.instructorName }}</span>
          </div>
          <div class="meta-item">
            <span class="label">价格：</span>
            <span class="value price">{{ tutorial.price > 0 ? `¥${tutorial.price}` : '免费' }}</span>
          </div>
          <div class="meta-item">
            <span class="label">章节数：</span>
            <span class="value">{{ chapters.length }} 章</span>
          </div>
          <div class="meta-item">
            <span class="label">难度：</span>
            <span :class="['badge', getDifficultyClass(tutorial.difficulty)]">
              {{ getDifficultyText(tutorial.difficulty) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 章节列表 -->
    <div class="chapters-section">
      <h3 class="section-title">章节列表</h3>
      
      <div v-if="chapters.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 6.25278V19.2528M12 6.25278C10.8321 5.47686 9.24649 5 7.5 5C5.75351 5 4.16789 5.47686 3 6.25278V19.2528C4.16789 18.4769 5.75351 18 7.5 18C9.24649 18 10.8321 18.4769 12 19.2528M12 6.25278C13.1679 5.47686 14.7535 5 16.5 5C18.2465 5 19.8321 5.47686 21 6.25278V19.2528C19.8321 18.4769 18.2465 18 16.5 18C14.7535 18 13.1679 18.4769 12 19.2528" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>还没有章节</p>
        <span>点击上方"新增章节"按钮创建第一个章节</span>
      </div>

      <div v-else class="chapter-list">
        <div 
          v-for="(chapter, index) in chapters" 
          :key="chapter.id" 
          class="chapter-item"
          :class="{ expanded: expandedChapters.includes(chapter.id) }"
        >
          <!-- 章节头部 -->
          <div class="chapter-header" @click="toggleChapter(chapter.id)">
            <div class="chapter-left">
              <svg class="expand-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="chapter-order">第{{ chapter.chapterOrder }}章</span>
              <span class="chapter-title">{{ chapter.chapterTitle }}</span>
              <span v-if="chapter.duration" class="chapter-duration">{{ chapter.duration }}</span>
            </div>
            <div class="chapter-right">
              <span :class="['badge', chapter.isFree === 1 ? 'free' : 'paid']">
                {{ chapter.isFree === 1 ? '免费试看' : '付费内容' }}
              </span>
              <span :class="['badge', chapter.status === 1 ? 'published' : 'draft']">
                {{ chapter.status === 1 ? '已发布' : '未发布' }}
              </span>
            </div>
          </div>

          <!-- 章节内容（展开时显示） -->
          <div v-if="expandedChapters.includes(chapter.id)" class="chapter-content">
            <!-- 文档列表 -->
            <div class="resource-section">
              <div class="resource-header">
                <h4>
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 12H15M9 16H15M17 21H7C5.89543 21 5 20.1046 5 19V5C5 3.89543 5.89543 3 7 3H12.5858C12.851 3 13.1054 3.10536 13.2929 3.29289L18.7071 8.70711C18.8946 8.89464 19 9.149 19 9.41421V19C19 20.1046 18.1046 21 17 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  文档资料
                </h4>
                <button class="btn-add" @click="openUploadDocumentDialog(chapter)">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  上传文档
                </button>
              </div>
              <div v-if="chapterDocuments[chapter.id]?.length > 0" class="resource-list">
                <div v-for="doc in chapterDocuments[chapter.id]" :key="doc.id" class="resource-item">
                  <div class="resource-icon">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M7 21H17C18.1046 21 19 20.1046 19 19V9.41421C19 9.149 18.8946 8.89464 18.7071 8.70711L13.2929 3.29289C13.1054 3.10536 12.851 3 12.5858 3H7C5.89543 3 5 3.89543 5 5V19C5 20.1046 5.89543 21 7 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <div class="resource-info">
                    <span class="resource-name">{{ doc.documentTitle }}</span>
                    <span class="resource-meta">{{ doc.documentType.toUpperCase() }} • {{ formatFileSize(doc.fileSize) }}</span>
                  </div>
                  <button class="btn-delete" @click="deleteDocument(doc.id)">删除</button>
                </div>
              </div>
              <div v-else class="resource-empty">暂无文档</div>
            </div>

            <!-- 视频列表 -->
            <div class="resource-section">
              <div class="resource-header">
                <h4>
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M15 10L19.5528 7.72361C20.2177 7.39116 21 7.87465 21 8.61803V15.382C21 16.1253 20.2177 16.6088 19.5528 16.2764L15 14M5 18H13C14.1046 18 15 17.1046 15 16V8C15 6.89543 14.1046 6 13 6H5C3.89543 6 3 6.89543 3 8V16C3 17.1046 3.89543 18 5 18Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  视频资源
                </h4>
                <button class="btn-add" @click="openUploadVideoDialog(chapter)">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  上传视频
                </button>
              </div>
              <div v-if="chapterVideos[chapter.id]?.length > 0" class="resource-list">
                <div v-for="video in chapterVideos[chapter.id]" :key="video.id" class="resource-item">
                  <div class="resource-icon video-icon">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M14.7519 11.1679L11.5547 9.03647C10.8901 8.59343 10 9.06982 10 9.86852V14.1315C10 14.9302 10.8901 15.4066 11.5547 14.9635L14.7519 12.8321C15.3457 12.4362 15.3457 11.5638 14.7519 11.1679Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <rect x="2" y="2" width="20" height="20" rx="2" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </div>
                  <div class="resource-info">
                    <span class="resource-name">{{ video.videoTitle }}</span>
                    <span class="resource-meta">{{ video.duration }} • {{ formatFileSize(video.fileSize) }}</span>
                  </div>
                  <button class="btn-delete" @click="deleteVideo(video.id)">删除</button>
                </div>
              </div>
              <div v-else class="resource-empty">暂无视频</div>
            </div>

            <!-- 章节操作按钮 -->
            <div class="chapter-actions">
              <button class="btn-edit" @click="editChapter(chapter)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H7C5.89543 4 5 4.89543 5 6V18C5 19.1046 5.89543 20 7 20H17C18.1046 20 19 19.1046 19 18V14M17.5858 3.58579C18.3668 2.80474 19.6332 2.80474 20.4142 3.58579C21.1953 4.36683 21.1953 5.63316 20.4142 6.41421L11.8284 15H9L9 12.1716L17.5858 3.58579Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                编辑章节
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增章节对话框 -->
    <div v-if="showCreateChapterDialog" class="dialog-overlay" @click.self="closeCreateChapterDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>新增章节</h3>
          <button class="btn-close" @click="closeCreateChapterDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleCreateChapter">
            <div class="form-group">
              <label class="required">章节排序</label>
              <input 
                v-model.number="chapterForm.chapterOrder" 
                type="number" 
                class="form-input" 
                placeholder="数字越小越靠前"
                min="1"
                required
              />
            </div>

            <div class="form-group">
              <label class="required">章节标题</label>
              <input 
                v-model="chapterForm.chapterTitle" 
                type="text" 
                class="form-input" 
                placeholder="请输入章节标题"
                required
              />
            </div>

            <div class="form-group">
              <label>本章时长</label>
              <input 
                v-model="chapterForm.duration" 
                type="text" 
                class="form-input" 
                placeholder="例如：45分钟"
              />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="required">是否免费试看</label>
                <select v-model.number="chapterForm.isFree" class="form-select" required>
                  <option :value="0">付费内容</option>
                  <option :value="1">免费试看</option>
                </select>
              </div>

              <div class="form-group">
                <label class="required">发布状态</label>
                <select v-model.number="chapterForm.status" class="form-select" required>
                  <option :value="0">未发布</option>
                  <option :value="1">已发布</option>
                </select>
              </div>
            </div>

            <div class="dialog-footer">
              <button type="button" class="btn-cancel" @click="closeCreateChapterDialog">取消</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting">
                {{ isSubmitting ? '创建中...' : '创建章节' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑章节对话框 -->
    <div v-if="showEditChapterDialog" class="dialog-overlay" @click.self="closeEditChapterDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>编辑章节</h3>
          <button class="btn-close" @click="closeEditChapterDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="submitEditChapter">
            <div class="form-row">
              <div class="form-group">
                <label class="required">章节序号</label>
                <input 
                  v-model.number="editChapterForm.chapterOrder" 
                  type="number" 
                  class="form-input" 
                  min="1"
                  placeholder="请输入章节序号"
                  required
                />
              </div>

              <div class="form-group">
                <label class="required">章节标题</label>
                <input 
                  v-model="editChapterForm.chapterTitle" 
                  type="text" 
                  class="form-input" 
                  placeholder="请输入章节标题"
                  required
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>预计时长（分钟）</label>
                <input 
                  v-model="editChapterForm.duration" 
                  type="text" 
                  class="form-input" 
                  placeholder="例如：45 或 60-90"
                />
              </div>

              <div class="form-group">
                <label class="required">是否免费试看</label>
                <select v-model.number="editChapterForm.isFree" class="form-select" required>
                  <option :value="0">需要解锁</option>
                  <option :value="1">免费试看</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="required">发布状态</label>
                <select v-model.number="editChapterForm.status" class="form-select" required>
                  <option :value="0">未发布</option>
                  <option :value="1">已发布</option>
                </select>
              </div>
            </div>

            <div class="dialog-footer">
              <button type="button" class="btn-cancel" @click="closeEditChapterDialog">取消</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting">
                {{ isSubmitting ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 上传文档对话框 -->
    <div v-if="showUploadDocumentDialog" class="dialog-overlay" @click.self="closeUploadDocumentDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>上传文档</h3>
          <button class="btn-close" @click="closeUploadDocumentDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleUploadDocument">
            <div class="form-group">
              <label class="required">文档标题</label>
              <input 
                v-model="documentForm.documentTitle" 
                type="text" 
                class="form-input" 
                placeholder="请输入文档标题"
                required
              />
            </div>

            <div class="form-group">
              <label class="required">文档类型</label>
              <select v-model="documentForm.documentType" class="form-select" required>
                <option value="pdf">PDF</option>
                <option value="doc">Word文档</option>
                <option value="ppt">PPT</option>
                <option value="md">Markdown</option>
                <option value="zip">压缩包</option>
              </select>
            </div>

            <div class="form-group">
              <label class="required">上传文件</label>
              <div class="upload-area" @click="$refs.docFileInput.click()">
                <div v-if="!documentForm.documentUrl" class="upload-placeholder">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15V19C21 20.1046 20.1046 21 19 21H5C3.89543 21 3 20.1046 3 19V15M17 8L12 3M12 3L7 8M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <p>点击选择文件</p>
                  <span>支持 PDF、Word、PPT等格式</span>
                </div>
                <div v-else class="upload-success">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <p>文件已上传</p>
                  <span>{{ documentForm.fileSize ? formatFileSize(documentForm.fileSize) : '' }}</span>
                </div>
              </div>
              <input 
                ref="docFileInput" 
                type="file" 
                style="display: none" 
                @change="handleDocumentFileUpload"
              />
            </div>

            <div class="dialog-footer">
              <button type="button" class="btn-cancel" @click="closeUploadDocumentDialog">取消</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting || !documentForm.documentUrl">
                {{ isSubmitting ? '上传中...' : '确认上传' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 上传视频对话框 -->
    <div v-if="showUploadVideoDialog" class="dialog-overlay" @click.self="closeUploadVideoDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>上传视频</h3>
          <button class="btn-close" @click="closeUploadVideoDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleUploadVideo">
            <div class="form-group">
              <label class="required">视频标题</label>
              <input 
                v-model="videoForm.videoTitle" 
                type="text" 
                class="form-input" 
                placeholder="请输入视频标题"
                required
              />
            </div>

            <div class="form-group">
              <label>视频时长</label>
              <input 
                v-model="videoForm.duration" 
                type="text" 
                class="form-input" 
                placeholder="例如：45:30"
              />
            </div>

            <div class="form-group">
              <label class="required">上传视频文件</label>
              <div class="upload-area" @click="!isUploadingVideo && $refs.videoFileInput.click()">
                <div v-if="!videoForm.videoUrl && !isUploadingVideo" class="upload-placeholder">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M15 10L19.5528 7.72361C20.2177 7.39116 21 7.87465 21 8.61803V15.382C21 16.1253 20.2177 16.6088 19.5528 16.2764L15 14M5 18H13C14.1046 18 15 17.1046 15 16V8C15 6.89543 14.1046 6 13 6H5C3.89543 6 3 6.89543 3 8V16C3 17.1046 3.89543 18 5 18Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <p>点击选择视频文件</p>
                  <span>支持 MP4、AVI、MOV等格式，最大500MB</span>
                </div>
                
                <!-- 上传进度条 -->
                <div v-else-if="isUploadingVideo" class="upload-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: videoUploadProgress + '%' }"></div>
                  </div>
                  <p class="progress-text">正在上传... {{ videoUploadProgress }}%</p>
                  <span class="progress-hint">请勿关闭页面</span>
                </div>
                
                <div v-else class="upload-success">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <p>视频已上传</p>
                  <span>{{ videoForm.fileSize ? formatFileSize(videoForm.fileSize) : '' }}</span>
                </div>
              </div>
              <input 
                ref="videoFileInput" 
                type="file" 
                accept="video/*"
                style="display: none" 
                @change="handleVideoFileUpload"
                :disabled="isUploadingVideo"
              />
            </div>

            <div class="dialog-footer">
              <button type="button" class="btn-cancel" @click="closeUploadVideoDialog">取消</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting || !videoForm.videoUrl">
                {{ isSubmitting ? '上传中...' : '确认上传' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { createChapter, deleteDocument as deleteDocumentApi, deleteVideo as deleteVideoApi, getChapterList, getDocumentList, getVideoList, updateChapter, uploadDocument, uploadFile, uploadFileToMinio, uploadVideo } from '@/api/admin'
import Toast from '@/components/Toast.vue'
import { onMounted, reactive, ref } from 'vue'

const props = defineProps({
  tutorial: {
    type: Object,
    required: true
  }
})

// Toast配置
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

// 章节数据
const chapters = ref([])
const expandedChapters = ref([])
const chapterDocuments = ref({})
const chapterVideos = ref({})

// 对话框状态
const showCreateChapterDialog = ref(false)
const showEditChapterDialog = ref(false)
const showUploadDocumentDialog = ref(false)
const showUploadVideoDialog = ref(false)
const isSubmitting = ref(false)

// 当前操作的章节
const currentChapter = ref(null)

// 编辑章节表单
const editChapterForm = reactive({
  id: '',
  chapterOrder: 1,
  chapterTitle: '',
  duration: '',
  isFree: 0,
  status: 1
})

// 章节表单
const chapterForm = reactive({
  tutorialId: props.tutorial.id,
  chapterOrder: 1,
  chapterTitle: '',
  duration: '',
  isFree: 0,
  status: 1
})

// 文档表单
const documentForm = reactive({
  chapterId: '',
  documentTitle: '',
  documentUrl: '',
  documentType: 'pdf',
  fileSize: 0
})

// 视频表单
const videoForm = reactive({
  chapterId: '',
  videoTitle: '',
  videoUrl: '',
  coverImage: '',
  duration: '',
  fileSize: 0
})

// 视频上传进度
const videoUploadProgress = ref(0)
const isUploadingVideo = ref(false)

// 获取章节列表
const fetchChapters = async () => {
  try {
    const res = await getChapterList(props.tutorial.id)
    chapters.value = res.data || []
    
    // 自动展开第一个章节并加载资源
    if (chapters.value.length > 0 && expandedChapters.value.length === 0) {
      const firstChapterId = chapters.value[0].id
      expandedChapters.value.push(firstChapterId)
      await loadChapterResources(firstChapterId)
    }
  } catch (error) {
    console.error('获取章节列表失败：', error)
    showMessage('error', '获取失败', '无法加载章节列表')
  }
}

// 切换章节展开/收起
const toggleChapter = async (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
    // 加载该章节的资源
    await loadChapterResources(chapterId)
  }
}

// 加载章节资源（文档和视频）
const loadChapterResources = async (chapterId) => {
  try {
    // 加载文档
    const docRes = await getDocumentList(chapterId)
    chapterDocuments.value[chapterId] = docRes.data || []
    
    // 加载视频
    const videoRes = await getVideoList(chapterId)
    chapterVideos.value[chapterId] = videoRes.data || []
  } catch (error) {
    console.error('加载章节资源失败：', error)
  }
}

// 创建章节
const handleCreateChapter = async () => {
  if (isSubmitting.value) return
  
  isSubmitting.value = true
  try {
    await createChapter(chapterForm)
    showMessage('success', '创建成功', '章节已成功创建')
    closeCreateChapterDialog()
    fetchChapters()
  } catch (error) {
    console.error('创建章节失败：', error)
    showMessage('error', '创建失败', error.message || '章节创建失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 关闭创建章节对话框
const closeCreateChapterDialog = () => {
  showCreateChapterDialog.value = false
  Object.assign(chapterForm, {
    tutorialId: props.tutorial.id,
    chapterOrder: chapters.value.length + 1,
    chapterTitle: '',
    duration: '',
    isFree: 0,
    status: 1
  })
}

// 打开上传文档对话框
const openUploadDocumentDialog = (chapter) => {
  currentChapter.value = chapter
  documentForm.chapterId = chapter.id
  showUploadDocumentDialog.value = true
}

// 关闭上传文档对话框
const closeUploadDocumentDialog = () => {
  showUploadDocumentDialog.value = false
  currentChapter.value = null
  Object.assign(documentForm, {
    chapterId: '',
    documentTitle: '',
    documentUrl: '',
    documentType: 'pdf',
    fileSize: 0
  })
}

// 处理文档文件上传
const handleDocumentFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const res = await uploadFile(file)
    documentForm.documentUrl = res.data
    documentForm.fileSize = file.size
    showMessage('success', '上传成功', '文件已上传')
  } catch (error) {
    console.error('文件上传失败：', error)
    showMessage('error', '上传失败', '文件上传失败，请重试')
  }
}

// 上传文档
const handleUploadDocument = async () => {
  if (isSubmitting.value) return
  
  isSubmitting.value = true
  try {
    const chapterId = currentChapter.value.id // 先保存章节ID
    await uploadDocument(documentForm)
    showMessage('success', '上传成功', '文档已成功上传')
    closeUploadDocumentDialog()
    await loadChapterResources(chapterId) // 使用保存的ID
  } catch (error) {
    console.error('上传文档失败：', error)
    showMessage('error', '上传失败', error.message || '文档上传失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 打开上传视频对话框
const openUploadVideoDialog = (chapter) => {
  currentChapter.value = chapter
  videoForm.chapterId = chapter.id
  showUploadVideoDialog.value = true
}

// 关闭上传视频对话框
const closeUploadVideoDialog = () => {
  showUploadVideoDialog.value = false
  currentChapter.value = null
  videoUploadProgress.value = 0
  isUploadingVideo.value = false
  Object.assign(videoForm, {
    chapterId: '',
    videoTitle: '',
    videoUrl: '',
    coverImage: '',
    duration: '',
    fileSize: 0
  })
}

// 处理视频文件上传
const handleVideoFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('video/')) {
    showMessage('error', '格式错误', '请上传视频文件')
    return
  }

  // 显示文件大小
  const fileSizeMB = (file.size / 1024 / 1024).toFixed(2)
  console.log(`选择的视频文件：${file.name}，大小：${fileSizeMB}MB`)

  try {
    isUploadingVideo.value = true
    videoUploadProgress.value = 0
    
    // 使用MinIO上传大文件
    const res = await uploadFileToMinio(file, (progressEvent) => {
      // 计算上传进度
      videoUploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      console.log(`上传进度：${videoUploadProgress.value}%`)
    })
    
    videoForm.videoUrl = res.data
    videoForm.fileSize = file.size
    
    showMessage('success', '上传成功', `视频已上传（${fileSizeMB}MB）`)
  } catch (error) {
    console.error('视频上传失败：', error)
    showMessage('error', '上传失败', error.message || '视频上传失败，请重试')
    videoUploadProgress.value = 0
  } finally {
    isUploadingVideo.value = false
  }
}

// 上传视频
const handleUploadVideo = async () => {
  if (isSubmitting.value) return
  
  isSubmitting.value = true
  try {
    const chapterId = currentChapter.value.id // 先保存章节ID
    await uploadVideo(videoForm)
    showMessage('success', '上传成功', '视频已成功上传')
    closeUploadVideoDialog()
    await loadChapterResources(chapterId) // 使用保存的ID
  } catch (error) {
    console.error('上传视频失败：', error)
    showMessage('error', '上传失败', error.message || '视频上传失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 删除文档
const deleteDocument = async (documentId) => {
  if (!confirm('确认删除该文档吗？删除后将同时删除OSS中的文件，且无法恢复！')) {
    return
  }

  try {
    await deleteDocumentApi(documentId)
    showMessage('success', '成功', '文档删除成功')
    
    // 刷新当前章节的文档列表
    const currentChapterId = Object.keys(chapterDocuments.value).find(chapterId => 
      chapterDocuments.value[chapterId]?.some(doc => doc.id === documentId)
    )
    if (currentChapterId) {
      await loadChapterResources(currentChapterId)
    }
  } catch (error) {
    console.error('删除文档失败：', error)
    showMessage('error', '删除失败', error.message || '删除文档失败，请重试')
  }
}

// 删除视频
const deleteVideo = async (videoId) => {
  if (!confirm('确认删除该视频吗？删除后将同时删除OSS中的文件，且无法恢复！')) {
    return
  }

  try {
    await deleteVideoApi(videoId)
    showMessage('success', '成功', '视频删除成功')
    
    // 刷新当前章节的视频列表
    const currentChapterId = Object.keys(chapterVideos.value).find(chapterId => 
      chapterVideos.value[chapterId]?.some(video => video.id === videoId)
    )
    if (currentChapterId) {
      await loadChapterResources(currentChapterId)
    }
  } catch (error) {
    console.error('删除视频失败：', error)
    showMessage('error', '删除失败', error.message || '删除视频失败，请重试')
  }
}

// 编辑章节
const editChapter = (chapter) => {
  // 填充编辑表单
  editChapterForm.id = chapter.id
  editChapterForm.chapterOrder = chapter.chapterOrder
  editChapterForm.chapterTitle = chapter.chapterTitle
  editChapterForm.duration = chapter.duration || ''
  editChapterForm.isFree = chapter.isFree
  editChapterForm.status = chapter.status
  
  // 打开编辑对话框
  showEditChapterDialog.value = true
}

// 关闭编辑章节对话框
const closeEditChapterDialog = () => {
  showEditChapterDialog.value = false
}

// 提交编辑章节
const submitEditChapter = async () => {
  // 表单验证
  if (!editChapterForm.chapterTitle.trim()) {
    showMessage('warning', '提示', '请输入章节标题')
    return
  }

  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    await updateChapter(editChapterForm)
    showMessage('success', '成功', '章节更新成功')
    closeEditChapterDialog()
    
    // 重新加载章节列表
    await fetchChapters()
  } catch (error) {
    console.error('更新章节失败：', error)
    showMessage('error', '更新失败', error.message || '更新章节失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 工具函数
const getDifficultyText = (difficulty) => {
  const map = { 0: '入门', 1: '进阶', 2: '高级' }
  return map[difficulty] || '未知'
}

const getDifficultyClass = (difficulty) => {
  const map = { 0: 'beginner', 1: 'intermediate', 2: 'advanced' }
  return map[difficulty] || 'beginner'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 初始化
onMounted(() => {
  fetchChapters()
  // 设置下一个章节序号
  chapterForm.chapterOrder = chapters.value.length + 1
})
</script>

<style scoped>
.tutorial-detail {
  padding: 24px;
  background: #f8fafc;
  min-height: calc(100vh - 60px);
}

/* 头部 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #2c3e50;
  color: #2c3e50;
}

.btn-back svg {
  width: 18px;
  height: 18px;
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
}

.btn-primary svg {
  width: 20px;
  height: 20px;
}

/* 教程信息卡片 */
.tutorial-info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  gap: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.info-cover {
  flex-shrink: 0;
  width: 240px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
}

.info-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-content {
  flex: 1;
}

.info-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.info-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.info-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.meta-item .label {
  color: #94a3b8;
}

.meta-item .value {
  color: #1e293b;
  font-weight: 600;
}

.meta-item .value.price {
  color: #ef4444;
  font-size: 16px;
}

/* 徽章 */
.badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.badge.beginner {
  background: #e0f2fe;
  color: #0c4a6e;
}

.badge.intermediate {
  background: #fef3c7;
  color: #92400e;
}

.badge.advanced {
  background: #fee2e2;
  color: #991b1b;
}

.badge.free {
  background: #d1fae5;
  color: #065f46;
}

.badge.paid {
  background: #fef3c7;
  color: #92400e;
}

.badge.published {
  background: #d1fae5;
  color: #065f46;
}

.badge.draft {
  background: #f1f5f9;
  color: #64748b;
}

/* 章节区域 */
.chapters-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px 0;
  padding-left: 12px;
  border-left: 4px solid #64748b;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 8px 0;
}

.empty-state span {
  font-size: 14px;
  color: #94a3b8;
}

/* 章节列表 */
.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chapter-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.chapter-item:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  background: #f8fafc;
  transition: background 0.2s;
}

.chapter-header:hover {
  background: #f1f5f9;
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.expand-icon {
  width: 20px;
  height: 20px;
  color: #64748b;
  transition: transform 0.3s;
}

.chapter-item.expanded .expand-icon {
  transform: rotate(90deg);
}

.chapter-order {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  min-width: 60px;
}

.chapter-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  flex: 1;
}

.chapter-duration {
  font-size: 13px;
  color: #94a3b8;
  margin-left: auto;
  padding: 4px 12px;
  background: white;
  border-radius: 12px;
}

.chapter-right {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}

/* 章节内容 */
.chapter-content {
  padding: 20px;
  background: white;
  border-top: 1px solid #e2e8f0;
}

/* 资源区域 */
.resource-section {
  margin-bottom: 20px;
}

.resource-section:last-child {
  margin-bottom: 0;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.resource-header h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin: 0;
}

.resource-header svg {
  width: 18px;
  height: 18px;
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add:hover {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
}

.btn-add svg {
  width: 14px;
  height: 14px;
}

/* 资源列表 */
.resource-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  transition: all 0.2s;
}

.resource-item:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.resource-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

.resource-icon svg {
  width: 20px;
  height: 20px;
}

.resource-icon.video-icon {
  background: #fef3c7;
  border-color: #fde68a;
  color: #92400e;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.resource-meta {
  font-size: 12px;
  color: #94a3b8;
}

.btn-delete {
  padding: 6px 12px;
  background: white;
  border: 1px solid #fee2e2;
  border-radius: 6px;
  font-size: 13px;
  color: #dc2626;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-delete:hover {
  background: #dc2626;
  border-color: #dc2626;
  color: white;
}

.resource-empty {
  padding: 24px;
  text-align: center;
  color: #cbd5e1;
  font-size: 13px;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
}

/* 章节操作 */
.chapter-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 12px;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit:hover {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
}

.btn-edit svg {
  width: 16px;
  height: 16px;
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
  padding: 20px;
}

.dialog-container {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.dialog-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.btn-close {
  width: 32px;
  height: 32px;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f1f5f9;
  color: #64748b;
}

.btn-close svg {
  width: 18px;
  height: 18px;
}

.dialog-body {
  padding: 24px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.form-group label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-input,
.form-select {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  color: #1e293b;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #2c3e50;
  background: white;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-group {
  flex: 1;
}

/* 上传区域 */
.upload-area {
  width: 100%;
  min-height: 120px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #f8fafc;
}

.upload-area:hover {
  border-color: #2c3e50;
  background: #f1f5f9;
}

.upload-placeholder,
.upload-success,
.upload-progress {
  text-align: center;
  padding: 20px;
}

.upload-placeholder svg,
.upload-success svg {
  width: 48px;
  height: 48px;
  color: #cbd5e1;
  margin-bottom: 12px;
}

.upload-success svg {
  color: #10b981;
}

.upload-placeholder p,
.upload-success p {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  margin: 0 0 8px 0;
}

.upload-placeholder span,
.upload-success span {
  font-size: 12px;
  color: #94a3b8;
}

/* 上传进度条 */
.upload-progress {
  padding: 30px 20px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 15px;
  font-weight: 600;
  color: #3b82f6;
  margin: 0 0 6px 0;
}

.progress-hint {
  font-size: 12px;
  color: #94a3b8;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.btn-cancel,
.btn-submit {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e2e8f0;
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
  transform: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .tutorial-info-card {
    flex-direction: column;
  }

  .info-cover {
    width: 100%;
    height: 200px;
  }

  .form-row {
    flex-direction: column;
  }

  .dialog-container {
    max-width: 100%;
    margin: 0 10px;
  }
}
</style>
