<script setup>
import { ref, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '@/composables/useUser'

const router = useRouter()
const route = useRoute()

const { users, currentUserId, currentUser, switchUser, addUser, removeUser, resetUsers } = useUser()

// 底部 Tab 配置
const tabs = [
  { path: '/home', label: '首页', icon: 'HomeFilled' },
  { path: '/category', label: '分类', icon: 'Grid' },
  { path: '/orders', label: '订单', icon: 'List' },
  { path: '/profile', label: '我的', icon: 'UserFilled' },
]

// 是否显示底部 Tab（子页面隐藏）
function showTabBar() {
  return route.meta.tab !== undefined
}

function navigate(path) {
  router.push(path)
}

provide('currentUserId', currentUserId)
provide('currentUser', currentUser)
provide('users', users)
</script>

<template>
  <div class="app-container">
    <!-- 顶部状态栏占位 + 搜索栏（仅在首页等 tab 页显示） -->
    <header v-if="route.path === '/home'" class="app-header">
      <div class="header-search" @click="router.push('/home')">
        <el-icon class="search-icon"><Search /></el-icon>
        <span class="search-placeholder">搜索拼团商品</span>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="app-main" :class="{ 'with-tab': showTabBar(), 'with-header': route.path === '/home' }">
      <router-view v-slot="{ Component, route: r }">
        <transition :name="r.meta.transition || 'fade'" mode="out-in">
          <component :is="Component" :key="r.path" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 Tab 导航 -->
    <nav v-if="showTabBar()" class="tab-bar safe-bottom">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        :class="['tab-item', { active: route.meta.tab === tab.tab }]"
        @click="navigate(tab.path)"
      >
        <el-icon class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </nav>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

/* ========== 顶部搜索栏 ========== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 99;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  padding: 10px 14px;
  padding-top: max(10px, env(safe-area-inset-top));
}

.header-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border-radius: 20px;
  padding: 8px 16px;
  cursor: pointer;
}

.search-icon {
  color: var(--text-muted);
  font-size: 16px;
}

.search-placeholder {
  color: var(--text-muted);
  font-size: 14px;
}

/* ========== 主内容 ========== */
.app-main {
  flex: 1;
  min-height: 0;
}

.app-main.with-tab {
  padding-bottom: 60px;
}

/* ========== 底部 Tab 栏 ========== */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: 56px;
  background: white;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  transition: color 0.2s;
  padding: 6px 0;
}

.tab-icon {
  font-size: 22px;
  color: var(--text-secondary);
  transition: color 0.2s;
}

.tab-label {
  font-size: 11px;
  color: var(--text-secondary);
  transition: color 0.2s;
}

.tab-item.active .tab-icon,
.tab-item.active .tab-label {
  color: var(--primary);
  font-weight: 600;
}
</style>
