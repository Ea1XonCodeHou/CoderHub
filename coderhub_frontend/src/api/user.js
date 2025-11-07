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
