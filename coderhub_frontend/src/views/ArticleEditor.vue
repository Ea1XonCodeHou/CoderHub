<template>
  <div class="article-editor">
    <!-- 顶部工具栏 -->
    <div class="editor-header">
      <div class="header-left">
        <button @click="goBack" class="btn-back">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h2>撰写文章</h2>
      </div>
      <div class="header-right">
        <button @click="importMarkdown" class="btn-import">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15M17 8L12 3M12 3L7 8M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          导入MD
        </button>
        <button @click="saveDraft" class="btn-draft">保存草稿</button>
        <button @click="publishArticle" class="btn-publish">发布文章</button>
      </div>
    </div>

    <!-- 文章信息区 -->
    <div class="article-info">
      <!-- 标题和标签在同一行 -->
      <div class="title-tags-row">
        <input 
          v-model="form.title" 
          type="text" 
          placeholder="请输入文章标题..." 
          class="title-input"
          maxlength="100"
        >
        
        <!-- 标签展示区（跟随标题） -->
        <div class="tags-display" v-if="form.tags.length > 0">
          <span v-for="(tag, index) in form.tags" :key="index" class="tag-capsule">
            <span class="tag-text">{{ tag }}</span>
            <svg @click="removeTag(index)" class="tag-remove-icon" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </span>
        </div>
      </div>

      <!-- 元信息区域：左右分栏 -->
      <div class="meta-section-grid">
        <!-- 左侧卡片：基本信息 -->
        <div class="meta-left-card">
          <!-- 左右分栏容器 -->
          <div class="left-card-grid">
            <!-- 左列：封面上传 + 参考文章 -->
            <div class="cover-references-column">
              <!-- 封面上传 -->
              <div class="cover-upload-section">
                <label class="section-label">文章封面</label>
                <div class="cover-upload-area" @click="selectCoverImage">
                  <div v-if="!form.coverImage" class="upload-placeholder">
                    <svg viewBox="0 0 24 24" fill="none">
                      <path d="M4 16L8.586 11.414C8.96106 11.0391 9.46967 10.8284 10 10.8284C10.5303 10.8284 11.0389 11.0391 11.414 11.414L16 16M14 14L15.586 12.414C15.9611 12.0391 16.4697 11.8284 17 11.8284C17.5303 11.8284 18.0389 12.0391 18.414 12.414L20 14M14 8H14.01M6 20H18C18.5304 20 19.0391 19.7893 19.4142 19.4142C19.7893 19.0391 20 18.5304 20 18V6C20 5.46957 19.7893 4.96086 19.4142 4.58579C19.0391 4.21071 18.5304 4 18 4H6C5.46957 4 4.96086 4.21071 4.58579 4.58579C4.21071 4.96086 4 5.46957 4 6V18C4 18.5304 4.21071 19.0391 4.58579 19.4142C4.96086 19.7893 5.46957 20 6 20Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>点击上传封面图</p>
                    <span class="upload-tip">建议尺寸 16:9，不超过 5MB</span>
                  </div>
                  <div v-else class="cover-preview">
                    <img :src="form.coverImage" alt="封面">
                    <div class="cover-actions">
                      <button @click.stop="selectCoverImage" class="btn-change">更换</button>
                      <button @click.stop="removeCoverImage" class="btn-remove">删除</button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 参考文章链接区域 -->
              <div class="references-section">
                <label class="section-label">参考文章</label>
                <div class="references-list">
                  <div v-if="referenceArticles.length === 0" class="no-references">
                    <svg viewBox="0 0 24 24" fill="none">
                      <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>点击右侧文章添加参考</p>
                  </div>
                  <div v-else class="reference-cards">
                    <div v-for="(article, index) in referenceArticles" :key="article.id" class="reference-card">
                      <div class="reference-info">
                        <div class="reference-title">{{ article.title }}</div>
                        <div class="reference-meta">
                          <span>{{ article.viewCount }} 阅读</span>
                          <span class="dot">·</span>
                          <span>{{ article.likeCount }} 点赞</span>
                        </div>
                      </div>
                      <svg @click="removeReference(index)" class="reference-remove" viewBox="0 0 24 24" fill="none">
                        <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右列：分类、标签、选项 -->
            <div class="meta-fields-column">
              <!-- 分类选择（二级联动） -->
              <div class="category-section">
                <label class="section-label">选择分类</label>
                <div class="category-select-wrapper">
                  <select v-model="selectedRootCategory" @change="onRootCategoryChange" class="category-select">
                    <option value="">请选择主分类</option>
                    <option v-for="category in rootCategories" :key="category.id" :value="category.id">
                      {{ category.categoryName }}
                    </option>
                  </select>
                  <select v-if="selectedRootCategory" v-model="form.categoryId" class="category-select">
                    <option value="">请选择子分类</option>
                    <option v-for="category in subCategories" :key="category.id" :value="category.id">
                      {{ category.categoryName }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- 标签选择 -->
              <div class="tags-section">
                <label class="section-label">添加标签</label>
                <div class="tag-input-container">
                  <input 
                    v-model="tagInput"
                    @input="handleTagInput"
                    @keyup.enter="addCurrentTag"
                    @focus="showTagSuggestions = true"
                    type="text" 
                    placeholder="输入标签名称..."
                    class="tag-input-field"
                    maxlength="20"
                  >
                  <span class="tag-count-hint">{{ form.tags.length }}/5</span>
                </div>
                
                <!-- 标签建议列表 -->
                <div v-if="showTagSuggestions && filteredTags.length > 0" class="tag-suggestions">
                  <div 
                    v-for="tag in filteredTags" 
                    :key="tag.id"
                    class="tag-suggestion-item"
                    @click="selectSuggestedTag(tag.tagName)"
                  >
                    <span class="tag-name">{{ tag.tagName }}</span>
                    <span class="tag-usage">{{ tag.usageCount }} 次引用</span>
                  </div>
                </div>

                <!-- 热门标签快捷选择 -->
                <div class="hot-tags">
                  <span class="hot-tags-label">热门标签：</span>
                  <span 
                    v-for="tag in hotTags" 
                    :key="tag.id"
                    :class="['hot-tag-item', { disabled: form.tags.includes(tag.tagName) }]"
                    @click="selectSuggestedTag(tag.tagName)"
                  >
                    {{ tag.tagName }}
                  </span>
                </div>
              </div>

              <!-- 其他选项 -->
              <div class="options-section">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="form.isOriginal" :true-value="1" :false-value="0">
                  <span class="checkbox-text">原创文章</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧卡片：热门推荐 -->
        <div class="meta-right-card">
          <div class="recommend-header">
            <h3 class="recommend-title">📌 或许可以供参考</h3>
            <p class="recommend-tip">点击文章添加到参考列表</p>
          </div>
          <div v-if="hotArticles.length === 0" class="loading-recommend">
            <div class="spinner-small"></div>
            <p>加载中...</p>
          </div>
          <div v-else class="recommend-articles">
            <!-- 使用Home同款卡片，只显示2篇 -->
            <div 
              v-for="(article, index) in hotArticles.slice(0, 2)" 
              :key="article.id" 
              class="hot-article-card"
              @click="addReference(article)"
              :class="{ 'is-referenced': isReferenced(article.id) }"
            >
              <div class="hot-rank-badge">{{ index + 1 }}</div>
              
              <!-- 封面图 -->
              <div v-if="article.coverImage" class="hot-article-cover">
                <img :src="article.coverImage" alt="cover" />
              </div>
              
              <div class="hot-article-content">
                <h4 class="hot-article-title">{{ article.title }}</h4>
                <p class="hot-article-summary">{{ article.summary }}</p>
                
                <div class="hot-article-tags" v-if="article.tags && article.tags.length > 0">
                  <span v-for="tag in article.tags.slice(0, 3)" :key="tag.id" class="hot-tag">
                    {{ tag.tagName }}
                  </span>
                </div>
                
                <div class="hot-article-footer">
                  <div class="hot-article-meta">
                    <span>{{ article.viewCount || 0 }} 阅读</span>
                    <span class="dot">·</span>
                    <span>{{ article.likeCount || 0 }} 点赞</span>
                  </div>
                  <div v-if="isReferenced(article.id)" class="referenced-badge">
                    ✓ 已添加
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body">
      <!-- 左侧：Markdown编辑器 -->
      <div class="editor-panel">
        <div class="panel-header">
          <h3>Markdown编辑</h3>
          <div class="editor-toolbar">
            <button @click="insertMarkdown('**', '**')" title="粗体">
              <strong>B</strong>
            </button>
            <button @click="insertMarkdown('*', '*')" title="斜体">
              <em>I</em>
            </button>
            <button @click="insertMarkdown('`', '`')" title="代码">
              <code>&lt;/&gt;</code>
            </button>
            <button @click="insertMarkdown('\n```\n', '\n```\n')" title="代码块">
              { }
            </button>
            <button @click="insertMarkdown('[', '](url)')" title="链接">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M10 13C10.4295 13.5741 10.9774 14.0491 11.6066 14.3929C12.2357 14.7367 12.9315 14.9411 13.6467 14.9923C14.3618 15.0435 15.0796 14.9403 15.7513 14.6897C16.4231 14.4392 17.0331 14.047 17.54 13.54L20.54 10.54C21.4508 9.59695 21.9548 8.33394 21.9434 7.02296C21.932 5.71198 21.4061 4.45791 20.4791 3.53087C19.5521 2.60383 18.298 2.07799 16.987 2.0666C15.676 2.0552 14.413 2.55918 13.47 3.46997L11.75 5.17997" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 11C13.5705 10.4259 13.0226 9.95083 12.3934 9.60707C11.7642 9.26331 11.0684 9.05889 10.3533 9.00768C9.63816 8.95646 8.92037 9.05965 8.24861 9.31023C7.57685 9.5608 6.96684 9.95303 6.45996 10.46L3.45996 13.46C2.54917 14.403 2.04519 15.666 2.05659 16.977C2.06798 18.288 2.59382 19.5421 3.52086 20.4691C4.4479 21.3961 5.70197 21.922 7.01295 21.9334C8.32393 21.9448 9.58694 21.4408 10.53 20.53L12.24 18.82" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <button @click="uploadImage" title="上传图片">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M21 15V19C21 19.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 19V15M17 8L12 3M12 3L7 8M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
        <textarea 
          ref="markdownTextarea"
          v-model="form.content"
          placeholder="开始编写你的文章内容...

支持Markdown语法：
# 一级标题
## 二级标题
**粗体** *斜体*
- 列表项
```代码块```
[链接](url)
![图片](url)"
          class="markdown-textarea"
        ></textarea>
        <div class="char-count">{{ form.content.length }} 字</div>
      </div>

      <!-- 右侧：实时预览 -->
      <div class="preview-panel">
        <div class="panel-header">
          <h3>实时预览</h3>
        </div>
        <div class="markdown-preview" v-html="renderedMarkdown"></div>
      </div>
    </div>

    <!-- 隐藏的文件输入框 -->
    <input 
      ref="fileInput" 
      type="file" 
      accept=".md,.markdown" 
      @change="handleFileImport"
      style="display: none"
    >
    <input 
      ref="imageInput" 
      type="file" 
      accept="image/*" 
      @change="handleImageUpload"
      style="display: none"
    >
    <input 
      ref="coverInput" 
      type="file" 
      accept="image/*" 
      @change="handleCoverUpload"
      style="display: none"
    >
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import axios from 'axios'

const router = useRouter()

// 表单数据
const form = ref({
  title: '',
  content: '',
  coverImage: '',
  categoryId: '',
  tags: [],
  isOriginal: 1
})

// 参考文章列表
const referenceArticles = ref([])

// 标签输入相关
const tagInput = ref('')
const showTagSuggestions = ref(false)
const tagSuggestions = ref([])
const hotTags = ref([])

// 分类列表
const categoryList = ref([])
const selectedRootCategory = ref('')
const subCategories = ref([])

// 热门文章
const hotArticles = ref([])

// 计算属性：一级分类
const rootCategories = computed(() => {
  return categoryList.value.filter(c => !c.parentId && c.status === 1)
})

// 一级分类改变时，加载子分类
const onRootCategoryChange = () => {
  if (selectedRootCategory.value) {
    subCategories.value = categoryList.value.filter(c => c.parentId === selectedRootCategory.value && c.status === 1)
    form.value.categoryId = '' // 重置子分类选择
  } else {
    subCategories.value = []
    form.value.categoryId = ''
  }
}

// 计算属性：过滤后的标签建议
const filteredTags = computed(() => {
  if (!tagInput.value.trim()) return []
  return tagSuggestions.value.filter(tag => 
    !form.value.tags.includes(tag.tagName)
  ).slice(0, 10)
})

// Markdown预览配置
marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true,
  gfm: true
})

// 实时渲染Markdown
const renderedMarkdown = computed(() => {
  if (!form.value.content) {
    return '<p class="preview-placeholder">在左侧编辑器输入内容，这里会实时显示预览效果</p>'
  }
  return marked(form.value.content)
})

// ==================== 分类相关 ====================
const fetchCategories = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/category/list', {
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      categoryList.value = response.data.data
    }
  } catch (error) {
    console.error('获取分类列表失败：', error)
  }
}

const selectCategory = (categoryId) => {
  form.value.categoryId = categoryId
}

// ==================== 标签相关 ====================
const fetchHotTags = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/article/tags/hot', {
      params: { limit: 15 },
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      hotTags.value = response.data.data
    }
  } catch (error) {
    console.error('获取热门标签失败：', error)
  }
}

const handleTagInput = async () => {
  const keyword = tagInput.value.trim()
  if (!keyword) {
    tagSuggestions.value = []
    return
  }

  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/article/tags/search', {
      params: { keyword },
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      tagSuggestions.value = response.data.data
    }
  } catch (error) {
    console.error('搜索标签失败：', error)
  }
}

const selectSuggestedTag = (tagName) => {
  if (form.value.tags.includes(tagName)) {
    return
  }
  if (form.value.tags.length >= 5) {
    alert('最多只能添加5个标签')
    return
  }
  form.value.tags.push(tagName)
  tagInput.value = ''
  tagSuggestions.value = []
  showTagSuggestions.value = false
}

const addCurrentTag = () => {
  const tag = tagInput.value.trim()
  if (!tag) return
  
  if (form.value.tags.includes(tag)) {
    alert('标签已存在')
    return
  }
  if (form.value.tags.length >= 5) {
    alert('最多只能添加5个标签')
    return
  }
  form.value.tags.push(tag)
  tagInput.value = ''
  tagSuggestions.value = []
  showTagSuggestions.value = false
}

const removeTag = (index) => {
  form.value.tags.splice(index, 1)
}

// 点击外部关闭标签建议
document.addEventListener('click', (e) => {
  if (!e.target.closest('.tag-input-container') && !e.target.closest('.tag-suggestions')) {
    showTagSuggestions.value = false
  }
})

// ==================== 封面图相关 ====================
const coverInput = ref(null)

const selectCoverImage = () => {
  coverInput.value.click()
}

const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    alert('封面图大小不能超过5MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/api/common/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 1) {
      form.value.coverImage = response.data.data
      alert('封面上传成功')
    } else {
      alert('封面上传失败：' + response.data.msg)
    }
  } catch (error) {
    console.error('封面上传失败：', error)
    alert('封面上传失败')
  }

  // 清空input
  coverInput.value.value = ''
}

const removeCoverImage = () => {
  form.value.coverImage = ''
}

// ==================== Markdown编辑相关 ====================
const markdownTextarea = ref(null)

const insertMarkdown = (before, after) => {
  const textarea = markdownTextarea.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.value.content.substring(start, end)
  const newText = before + selectedText + after
  
  form.value.content = 
    form.value.content.substring(0, start) + 
    newText + 
    form.value.content.substring(end)
  
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  }, 0)
}

// 导入Markdown文件
const fileInput = ref(null)

const importMarkdown = () => {
  fileInput.value.click()
}

const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.content = e.target.result
  }
  reader.readAsText(file)
  
  fileInput.value.value = ''
}

// 上传图片
const imageInput = ref(null)

const uploadImage = () => {
  imageInput.value.click()
}

const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过10MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/api/common/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 1) {
      const imageUrl = response.data.data
      const markdown = `![图片](${imageUrl})\n`
      
      const textarea = markdownTextarea.value
      const start = textarea.selectionStart
      form.value.content = 
        form.value.content.substring(0, start) + 
        markdown + 
        form.value.content.substring(start)
      
      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + markdown.length, start + markdown.length)
      }, 0)
    }
  } catch (error) {
    console.error('图片上传失败：', error)
    alert('图片上传失败')
  }

  imageInput.value.value = ''
}

// ==================== 保存和发布 ====================
const saveDraft = async () => {
  if (!validateForm()) return

  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/article/publish', {
      ...form.value,
      status: 0
    }, {
      headers: {
        authentication: token
      }
    })

    if (response.data.code === 1) {
      alert('草稿保存成功')
    } else {
      alert('保存失败：' + response.data.msg)
    }
  } catch (error) {
    console.error('保存草稿失败：', error)
    alert('保存草稿失败')
  }
}

const publishArticle = async () => {
  if (!validateForm()) return

  if (!confirm('确认发布文章？')) return

  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/article/publish', {
      ...form.value,
      status: 1
    }, {
      headers: {
        authentication: token
      }
    })

    if (response.data.code === 1) {
      alert('文章发布成功！')
      router.push('/home')
    } else {
      alert('发布失败：' + response.data.msg)
    }
  } catch (error) {
    console.error('发布文章失败：', error)
    alert('发布文章失败')
  }
}

const validateForm = () => {
  if (!form.value.title.trim()) {
    alert('请输入文章标题')
    return false
  }
  if (!form.value.content.trim()) {
    alert('请输入文章内容')
    return false
  }
  if (!form.value.categoryId) {
    alert('请选择文章分类')
    return false
  }
  if (form.value.tags.length === 0) {
    alert('请至少添加一个标签')
    return false
  }
  return true
}

const goBack = () => {
  if (form.value.content || form.value.title) {
    if (confirm('确定要离开吗？未保存的内容将会丢失。')) {
      router.back()
    }
  } else {
    router.back()
  }
}

// 获取热门文章
const fetchHotArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/article/list', {
      headers: {
        authentication: token
      }
    })
    
    if (response.data.code === 1) {
      // 按阅读量排序，只取前2篇
      const sorted = response.data.data
        .sort((a, b) => b.viewCount - a.viewCount)
        .slice(0, 2)
      
      hotArticles.value = sorted.map((article, index) => ({
        ...article,
        rank: index + 1
      }))
    }
  } catch (error) {
    console.error('获取热门文章失败：', error)
  }
}

// 添加参考文章
const addReference = (article) => {
  // 检查是否已经添加过
  if (referenceArticles.value.some(ref => ref.id === article.id)) {
    return
  }
  
  // 最多添加3篇参考文章
  if (referenceArticles.value.length >= 3) {
    alert('最多只能添加3篇参考文章')
    return
  }
  
  referenceArticles.value.push({
    id: article.id,
    title: article.title,
    viewCount: article.viewCount || 0,
    likeCount: article.likeCount || 0
  })
}

// 移除参考文章
const removeReference = (index) => {
  referenceArticles.value.splice(index, 1)
}

// 检查文章是否已被添加为参考
const isReferenced = (articleId) => {
  return referenceArticles.value.some(ref => ref.id === articleId)
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchHotTags()
  fetchHotArticles()
})
</script>

<style scoped>
.article-editor {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* ==================== 顶部工具栏 ==================== */
.editor-header {
  background: white;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #f5f5f5;
  color: #333;
}

.btn-back svg {
  width: 18px;
  height: 18px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 12px;
}

.btn-import,
.btn-draft,
.btn-publish {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-import {
  background: #f5f5f5;
  color: #666;
}

.btn-import:hover {
  background: #e0e0e0;
}

.btn-import svg {
  width: 16px;
  height: 16px;
}

.btn-draft {
  background: #f0f0f0;
  color: #666;
}

.btn-draft:hover {
  background: #e0e0e0;
}

.btn-publish {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* ==================== 文章信息区 ==================== */
.article-info {
  background: white;
  padding: 24px;
  margin: 16px 24px 0;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 标题和标签行 */
.title-tags-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.title-tags-row:focus-within {
  border-bottom-color: #667eea;
}

.title-input {
  flex: 1;
  min-width: 300px;
  padding: 12px 0;
  border: none;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  outline: none;
  background: transparent;
}

.title-input::placeholder {
  color: #ccc;
}

/* 标签展示区（跟随标题） */
.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-capsule {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0fe 100%);
  color: #667eea;
  border: 1.5px solid #667eea;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: default;
  transition: all 0.3s;
  animation: tagFadeIn 0.3s ease;
}

.tag-capsule:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.tag-text {
  user-select: none;
}

@keyframes tagFadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-remove-icon {
  width: 14px;
  height: 14px;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.2s;
}

.tag-capsule:hover .tag-remove-icon {
  opacity: 1;
}

.tag-remove-icon:hover {
  transform: rotate(90deg);
}

/* 元信息区域：左右分栏 */
.meta-section-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 20px;
}

/* 左侧卡片 */
.meta-left-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 左侧卡片内部网格布局 */
.left-card-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  align-items: stretch;  /* 改为stretch让两列高度一致 */
  min-height: 400px;  /* 设置最小高度 */
}

/* 右列字段容器 */
.meta-fields-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

/* 右侧卡片 */
.meta-right-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  max-height: 800px;
  display: flex;
  flex-direction: column;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.section-label::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

/* 左列：封面和参考文章容器 */
.cover-references-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 封面上传 */
.cover-upload-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-upload-area {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 180px;
  background: #f8f9fa;
  border: 2px dashed #d0d0d0;
  border-radius: 8px;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #667eea;
  background: #f5f7ff;
}

.upload-placeholder svg {
  width: 48px;
  height: 48px;
  color: #999;
  margin-bottom: 12px;
}

.upload-placeholder p {
  margin: 0;
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-actions {
  opacity: 1;
}

.btn-change,
.btn-remove {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-change {
  background: white;
  color: #333;
}

.btn-change:hover {
  background: #f0f0f0;
}

.btn-remove {
  background: #ff4d4f;
  color: white;
}

.btn-remove:hover {
  background: #ff3333;
}

/* 参考文章区域 */
.references-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.references-list {
  min-height: 120px;
}

.no-references {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f8f9fa;
  border: 2px dashed #d0d0d0;
  border-radius: 8px;
  color: #999;
  min-height: 120px;
}

.no-references svg {
  width: 32px;
  height: 32px;
  margin-bottom: 8px;
  color: #c0c0c0;
}

.no-references p {
  margin: 0;
  font-size: 13px;
}

.reference-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reference-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.3s;
}

.reference-card:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.reference-info {
  flex: 1;
  overflow: hidden;
}

.reference-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reference-meta {
  font-size: 11px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 6px;
}

.reference-meta .dot {
  width: 3px;
  height: 3px;
  background: #ccc;
  border-radius: 50%;
}

.reference-remove {
  width: 16px;
  height: 16px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.reference-remove:hover {
  color: #ff4d4f;
  transform: rotate(90deg);
}

/* 分类选择（二级联动） */
.category-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-select-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-select {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  outline: none;
  transition: all 0.3s;
  background: white url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23667eea' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z'%3E%3C/path%3E%3Cpolyline points='9 22 9 12 15 12 15 22'%3E%3C/polyline%3E%3C/svg%3E") no-repeat 12px center;
  background-size: 18px 18px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.category-select:hover {
  border-color: #667eea;
  background-color: #f8f9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.15);
}

.category-select:focus {
  border-color: #667eea;
  background-color: #f8f9ff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.category-select option {
  padding: 10px;
  font-weight: 500;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: #f8f9fa;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-card:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.category-card.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: #667eea;
}

.category-icon {
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s;
}

.category-card.active .category-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.category-icon svg {
  width: 20px;
  height: 20px;
  color: #999;
  transition: all 0.3s;
}

.category-card.active .category-icon svg {
  color: white;
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.category-count {
  font-size: 12px;
  color: #999;
}

/* 标签选择 */
.tags-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
}

.tag-input-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.tag-input-field {
  flex: 1;
  padding: 12px 16px 12px 40px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  outline: none;
  transition: all 0.3s;
  background: white url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23667eea' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z'%3E%3C/path%3E%3Cline x1='7' y1='7' x2='7.01' y2='7'%3E%3C/line%3E%3C/svg%3E") no-repeat 12px center;
  background-size: 18px 18px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tag-input-field:hover {
  border-color: #667eea;
  background-color: #f8f9ff;
}

.tag-input-field:focus {
  border-color: #667eea;
  background-color: #f8f9ff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.tag-count-hint {
  font-size: 12px;
  color: #999;
  font-weight: 500;
  white-space: nowrap;
}

.tag-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
}

.tag-suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.tag-suggestion-item:hover {
  background: #f5f7ff;
}

.tag-suggestion-item .tag-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.tag-suggestion-item .tag-usage {
  font-size: 12px;
  color: #999;
}

.hot-tags {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.hot-tags-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.hot-tag-item {
  padding: 6px 12px;
  background: #f0f2f5;
  color: #666;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag-item:hover {
  background: #e0e4ea;
  color: #333;
}

.hot-tag-item.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.hot-tag-item.disabled:hover {
  background: #f0f2f5;
  color: #666;
}

/* 其他选项 */
.options-section {
  display: flex;
  gap: 24px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 12px 16px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.checkbox-label:hover {
  border-color: #667eea;
  background-color: #f8f9ff;
}

.checkbox-label input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #667eea;
}

.checkbox-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  user-select: none;
}

/* ==================== 编辑器主体 ==================== */
.editor-body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 16px 24px 24px;
  min-height: 600px;
}

.editor-panel,
.preview-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
}

.editor-toolbar button {
  width: 32px;
  height: 32px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.editor-toolbar button:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.markdown-textarea {
  flex: 1;
  padding: 20px;
  border: none;
  outline: none;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  resize: none;
}

.char-count {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
  color: #999;
  text-align: right;
}

.markdown-preview {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

.preview-placeholder {
  text-align: center;
  color: #ccc;
  font-size: 14px;
  margin-top: 40px;
}

/* Markdown预览样式 */
.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3),
.markdown-preview :deep(h4),
.markdown-preview :deep(h5),
.markdown-preview :deep(h6) {
  margin: 24px 0 16px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-preview :deep(h1) { font-size: 28px; }
.markdown-preview :deep(h2) { font-size: 24px; }
.markdown-preview :deep(h3) { font-size: 20px; }
.markdown-preview :deep(h4) { font-size: 18px; }

.markdown-preview :deep(p) {
  margin: 12px 0;
}

.markdown-preview :deep(code) {
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #e83e8c;
}

.markdown-preview :deep(pre) {
  background: #282c34;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-preview :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

.markdown-preview :deep(a) {
  color: #667eea;
  text-decoration: none;
}

.markdown-preview :deep(a:hover) {
  text-decoration: underline;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.markdown-preview :deep(li) {
  margin: 6px 0;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 16px;
  margin: 16px 0;
  color: #666;
  font-style: italic;
}

.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  padding: 12px;
  border: 1px solid #e0e0e0;
  text-align: left;
}

.markdown-preview :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

/* ==================== 热门推荐卡片 ==================== */
.recommend-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.recommend-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.recommend-tip {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.loading-recommend {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
}

.spinner-small {
  width: 28px;
  height: 28px;
  border: 3px solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-recommend p {
  margin: 0;
  font-size: 13px;
}

/* 热门文章列表 */
.recommend-articles {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Home同款文章卡片 */
.hot-article-card {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-article-card:hover {
  background: #e8f0fe;
  transform: translateX(3px);
}

/* 排名徽章 */
.hot-rank-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
  z-index: 1;
}

/* 封面图 */
.hot-article-cover {
  flex-shrink: 0;
  width: 120px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
}

.hot-article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.hot-article-card:hover .hot-article-cover img {
  transform: scale(1.05);
}

/* 文章内容 */
.hot-article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.hot-article-title {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.hot-article-summary {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

/* 标签 */
.hot-article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.hot-tag {
  padding: 3px 8px;
  font-size: 11px;
  color: #64748b;
  background: white;
  border-radius: 4px;
}

/* 底部 */
.hot-article-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hot-article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.hot-article-meta {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
}

.hot-article-meta .dot {
  margin: 0 6px;
}

.referenced-badge {
  padding: 4px 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 12px;
  white-space: nowrap;
}
</style>
