<template>
  <router-view />
</template>

<script setup>
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'

const router = useRouter()
const notificationStore = useNotificationStore()

// 监听路由变化，根据登录态全局管理通知轮询
// 在 App.vue 层管理，避免轮询生命周期被单个组件挂载/卸载影响
router.afterEach((to) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta?.requiresAuth

  if (token && requiresAuth) {
    // 已登录且进入需要鉴权的页面，确保轮询运行
    notificationStore.startPolling()
  } else if (!token) {
    // 未登录（退出登录后），停止轮询并重置状态
    notificationStore.reset()
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
