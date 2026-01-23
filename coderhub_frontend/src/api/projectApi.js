import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000 // 文件上传可能需要较长时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['authentication'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 1) {
      return res.data
    }
    console.error('API错误:', res.msg)
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 项目API接口
 */
export const projectApi = {

  // ==================== 分类和技术栈 ====================

  /**
   * 获取所有项目分类（带技术栈）
   */
  getCategories() {
    return request.get('/project/categories')
  },

  /**
   * 根据分类ID获取技术栈
   */
  getTechStacksByCategory(categoryId) {
    return request.get(`/project/categories/${categoryId}/tech-stacks`)
  },

  // ==================== 文件上传 ====================

  /**
   * 上传项目图片（封面图/截图）
   */
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/project/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 上传README.md文件
   */
  uploadReadme(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/project/upload/readme', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 上传项目压缩包
   */
  uploadProjectFile(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/project/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: progressEvent => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percent)
        }
      }
    })
  },

  // ==================== 项目CRUD ====================

  /**
   * 创建项目
   */
  createProject(data) {
    return request.post('/project', data)
  },

  /**
   * 获取项目详情
   */
  getProjectDetail(id) {
    return request.get(`/project/${id}`)
  },

  /**
   * 获取优秀开源项目列表
   */
  getShowcaseProjects(params = {}) {
    return request.get('/project/showcase', { params })
  },

  /**
   * 获取社区项目列表
   */
  getCommunityProjects(params = {}) {
    return request.get('/project/community', { params })
  },

  /**
   * 获取我的项目列表
   */
  getMyProjects(isOpenSource) {
    const params = isOpenSource !== undefined ? { isOpenSource } : {}
    return request.get('/project/my', { params })
  },

  /**
   * 删除项目
   */
  deleteProject(id) {
    return request.delete(`/project/${id}`)
  },

  /**
   * 刷新分类缓存
   */
  refreshCache() {
    return request.post('/project/cache/refresh')
  }
}

export default projectApi

