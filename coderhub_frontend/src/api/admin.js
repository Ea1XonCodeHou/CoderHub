import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.authentication = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 1) {
      return res
    } else {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token过期或无效
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/'
    }
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 分页查询用户列表
export function getUserPage(params) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params
  })
}

// 编辑用户信息
export function updateUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}

// 修改用户状态
export function updateUserStatus(userId, status) {
  return request({
    url: `/admin/user/status/${userId}/${status}`,
    method: 'put'
  })
}

// ============= 分类管理 =============

// 查询所有分类
export function getAllCategories() {
  return request({
    url: '/admin/category/list',
    method: 'get'
  })
}

// 查询一级分类
export function getRootCategories() {
  return request({
    url: '/admin/category/root',
    method: 'get'
  })
}

// 查询子分类
export function getSubCategories(parentId) {
  return request({
    url: `/admin/category/sub/${parentId}`,
    method: 'get'
  })
}

// 根据ID查询分类
export function getCategoryById(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

// 新增分类
export function addCategory(data) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(data) {
  return request({
    url: '/admin/category',
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'delete'
  })
}

// 修改分类状态
export function updateCategoryStatus(id, status) {
  return request({
    url: `/admin/category/status/${id}/${status}`,
    method: 'put'
  })
}

// ============= 教程管理 =============

// 获取教程列表
export function getTutorialList(params) {
  return request({
    url: '/admin/tutorial/list',
    method: 'get',
    params
  })
}

// 创建教程
export function createTutorial(data) {
  return request({
    url: '/admin/tutorial/create',
    method: 'post',
    data
  })
}

// 更新教程
export function updateTutorial(data) {
  return request({
    url: '/admin/tutorial/update',
    method: 'put',
    data
  })
}

// 删除教程
export function deleteTutorial(id) {
  return request({
    url: `/admin/tutorial/${id}`,
    method: 'delete'
  })
}

// ==================== 章节管理接口 ====================

// 创建章节
export function createChapter(data) {
  return request({
    url: '/admin/tutorial/chapter/create',
    method: 'post',
    data
  })
}

// 获取章节列表
export function getChapterList(tutorialId) {
  return request({
    url: '/admin/tutorial/chapter/list',
    method: 'get',
    params: { tutorialId }
  })
}

// 上传文档
export function uploadDocument(data) {
  return request({
    url: '/admin/tutorial/document/upload',
    method: 'post',
    data
  })
}

// 获取文档列表
export function getDocumentList(chapterId) {
  return request({
    url: '/admin/tutorial/document/list',
    method: 'get',
    params: { chapterId }
  })
}

// 上传视频
export function uploadVideo(data) {
  return request({
    url: '/admin/tutorial/video/upload',
    method: 'post',
    data
  })
}

// 获取视频列表
export function getVideoList(chapterId) {
  return request({
    url: '/admin/tutorial/video/list',
    method: 'get',
    params: { chapterId }
  })
}

// 修改教程状态
export function updateTutorialStatus(id, status) {
  return request({
    url: `/admin/tutorial/status/${id}/${status}`,
    method: 'put'
  })
}

