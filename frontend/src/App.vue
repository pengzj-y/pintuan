<script setup>
import { ref, provide, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '@/composables/useUser'

const router = useRouter()
const route = useRoute()

// 使用用户管理 composable
const { users, currentUserId, currentUser, switchUser, addUser, removeUser, resetUsers } = useUser()

// 控制用户面板展开
const showUserPanel = ref(false)
// 新增用户表单
const newUserId = ref('')
const newUserName = ref('')

const menuItems = [
  { path: '/customer', label: '拼团商城', icon: 'ShoppingBag' },
  { path: '/home', label: '营销首页', icon: 'HomeFilled' },
  { path: '/trade', label: '交易下单', icon: 'ShoppingCart' },
  { path: '/order', label: '订单管理', icon: 'List' },
  { path: '/dcc', label: '动态配置', icon: 'Setting' },
  { path: '/test', label: '模拟回调', icon: 'Promotion' },
]

function navigate(path) {
  router.push(path)
}

// 添加新用户
function handleAddUser() {
  if (!newUserId.value.trim()) return
  addUser(newUserId.value.trim(), newUserName.value.trim() || newUserId.value.trim())
  newUserId.value = ''
  newUserName.value = ''
}

// 切换用户并刷新页面
function handleSwitchUser(userId) {
  switchUser(userId)
  showUserPanel.value = false
  // 切换用户后跳转到首页
  if (route.path !== '/home') {
    router.push('/home')
  }
}

provide('currentUserId', currentUserId)
provide('currentUser', currentUser)
provide('users', users)
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <span class="logo-icon">🛒</span>
        <span class="logo-text">团购商城</span>
      </div>
      <nav class="menu">
        <div
          v-for="item in menuItems"
          :key="item.path"
          :class="['menu-item', { active: route.path === item.path }]"
          @click="navigate(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </nav>

      <!-- 账号切换区域 -->
      <div class="user-section">
        <div class="user-section-title" @click="showUserPanel = !showUserPanel">
          <el-icon><User /></el-icon>
          <span>账号切换</span>
          <el-icon class="arrow" :class="{ rotated: showUserPanel }"><ArrowDown /></el-icon>
        </div>
        <div v-show="showUserPanel" class="user-panel">
          <!-- 当前用户信息 -->
          <div class="current-user">
            <div class="current-user-label">当前用户</div>
            <div class="current-user-info">
              <span class="current-user-id">{{ currentUser?.id }}</span>
              <span class="current-user-name">{{ currentUser?.name }}</span>
            </div>
          </div>

          <!-- 用户列表 -->
          <div class="user-list">
            <div
              v-for="user in users"
              :key="user.id"
              :class="['user-item', { active: user.id === currentUserId }]"
              @click="handleSwitchUser(user.id)"
            >
              <div class="user-item-info">
                <span class="user-item-id">{{ user.id }}</span>
                <span class="user-item-name">{{ user.name }}</span>
              </div>
              <span class="user-item-role">{{ user.role }}</span>
            </div>
          </div>

          <!-- 添加新用户 -->
          <div class="add-user">
            <div class="add-user-title">添加账号</div>
            <el-input v-model="newUserId" size="small" placeholder="用户ID" />
            <el-input v-model="newUserName" size="small" placeholder="昵称（可选）" />
            <el-button type="primary" size="small" @click="handleAddUser">添加</el-button>
          </div>

          <!-- 重置 -->
          <div class="user-actions">
            <el-button size="small" @click="resetUsers">恢复默认账号</el-button>
          </div>
        </div>
      </div>
    </aside>

    <main class="main">
      <router-view v-slot="{ Component }">
        <component :is="Component" :currentUserId="currentUserId" :currentUser="currentUser" />
      </router-view>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #1e293b;
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.logo {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #334155;
}

.logo-icon {
  font-size: 24px;
}

.menu {
  flex: 1;
  padding: 12px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
  color: #94a3b8;
}

.menu-item:hover {
  background: #334155;
  color: white;
}

.menu-item.active {
  background: #4f46e5;
  color: white;
}

/* ========== 账号切换区域 ========== */
.user-section {
  border-top: 1px solid #334155;
}

.user-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #94a3b8;
  transition: color 0.2s;
  user-select: none;
}

.user-section-title:hover {
  color: white;
}

.arrow {
  margin-left: auto;
  transition: transform 0.2s;
}

.arrow.rotated {
  transform: rotate(180deg);
}

.user-panel {
  background: #0f172a;
  padding: 12px;
  border-top: 1px solid #334155;
  max-height: 400px;
  overflow-y: auto;
}

/* 当前用户 */
.current-user {
  padding: 8px 10px;
  background: #1e293b;
  border-radius: 6px;
  margin-bottom: 10px;
}

.current-user-label {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 4px;
}

.current-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-user-id {
  font-weight: 600;
  font-size: 14px;
  color: white;
}

.current-user-name {
  font-size: 12px;
  color: #94a3b8;
}

/* 用户列表 */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-item:hover {
  background: #1e293b;
}

.user-item.active {
  background: #4f46e5;
}

.user-item-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-item-id {
  font-size: 13px;
  font-weight: 500;
}

.user-item-name {
  font-size: 11px;
  color: #94a3b8;
}

.user-item.active .user-item-name {
  color: rgba(255, 255, 255, 0.8);
}

.user-item-role {
  font-size: 10px;
  color: #64748b;
  background: #334155;
  padding: 2px 6px;
  border-radius: 4px;
}

.user-item.active .user-item-role {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* 添加用户 */
.add-user {
  padding: 10px 0;
  border-top: 1px solid #334155;
  margin-bottom: 10px;
}

.add-user-title {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.add-user :deep(.el-input) {
  margin-bottom: 6px;
}

.add-user :deep(.el-button) {
  width: 100%;
}

/* 操作按钮 */
.user-actions {
  display: flex;
  gap: 6px;
}

.user-actions :deep(.el-button) {
  flex: 1;
  font-size: 12px;
}

/* 滚动条样式 */
.user-panel::-webkit-scrollbar {
  width: 4px;
}

.user-panel::-webkit-scrollbar-track {
  background: transparent;
}

.user-panel::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 2px;
}

.main {
  flex: 1;
  margin-left: 220px;
  background: #f1f5f9;
}
</style>
