<template>
  <router-view />
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'

const router = useRouter()
const notificationStore = useNotificationStore()

// 监听路由变化，根据登录态全局管理通知轮询
router.afterEach((to) => {
  const token = localStorage.getItem('token')

  if (token && to.meta?.requiresAuth) {
    // 延迟启动，确保页面和 token 均已稳定，避免登录后立刻轮询的时序问题
    setTimeout(() => {
      notificationStore.startPolling()
    }, 500)
  } else if (!token && (to.name === 'login' || to.name === 'register')) {
    // 明确到达登录/注册页且无 token，停止轮询
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
