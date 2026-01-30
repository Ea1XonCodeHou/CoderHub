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

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/updateInfo',
    method: 'put',
    data
  })
}

// 上传文件
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 分页查询教程列表
export function getTutorialList(params) {
  return request({
    url: '/tutorial/page',
    method: 'get',
    params
  })
}

// 获取所有分类列表
export function getCategoryList() {
  return request({
    url: '/tutorial/categories',
    method: 'get'
  })
}

// 获取教程详情
export function getTutorialDetail(id) {
  return request({
    url: `/tutorial/${id}`,
    method: 'get'
  })
}

// 获取教程章节列表
export function getTutorialChapters(tutorialId) {
  return request({
    url: `/tutorial/${tutorialId}/chapters`,
    method: 'get'
  })
}

// 获取章节文档列表
export function getChapterDocuments(chapterId) {
  return request({
    url: `/tutorial/chapter/${chapterId}/documents`,
    method: 'get'
  })
}

// 获取章节视频列表
export function getChapterVideos(chapterId) {
  return request({
    url: `/tutorial/chapter/${chapterId}/videos`,
    method: 'get'
  })
}

// ==================== 教程评论相关接口 ====================

// 获取教程评论列表
export function getTutorialComments(tutorialId) {
  return request({
    url: `/tutorial/${tutorialId}/comment`,
    method: 'get'
  })
}

// 获取教程评论数量
export function getTutorialCommentCount(tutorialId) {
  return request({
    url: `/tutorial/${tutorialId}/comment/count`,
    method: 'get'
  })
}

// 发布教程评论
export function publishTutorialComment(tutorialId, data) {
  return request({
    url: `/tutorial/${tutorialId}/comment`,
    method: 'post',
    data
  })
}

// 删除教程评论
export function deleteTutorialComment(commentId) {
  return request({
    url: `/tutorial/comment/${commentId}`,
    method: 'delete'
  })
}

// ==================== 个人中心相关接口 ====================

// 获取我的文章列表
export function getMyArticles() {
  return request({
    url: '/user/profile/articles',
    method: 'get'
  })
}

// 获取我关注的用户列表
export function getMyFollowing() {
  return request({
    url: '/user/profile/following',
    method: 'get'
  })
}

// 获取我的粉丝列表
export function getMyFollowers() {
  return request({
    url: '/user/profile/followers',
    method: 'get'
  })
}

// 获取我的项目列表
export function getMyProjects() {
  return request({
    url: '/user/profile/projects',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/profile/password',
    method: 'post',
    data
  })
}