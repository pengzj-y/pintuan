<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { users, currentUserId, currentUser, switchUser, addUser, removeUser, resetUsers } = useUser()

// 用户切换面板
const showUserPanel = ref(false)
const newUserId = ref('')
const newUserName = ref('')

// 订单统计
const orderStats = ref([
  { key: '0', label: '待支付', icon: '⏳', count: 1, color: 'var(--orange)' },
  { key: '3', label: '拼团中', icon: '🔥', count: 1, color: 'var(--primary)' },
  { key: '1', label: '已成团', icon: '✅', count: 1, color: 'var(--green)' },
  { key: '2', label: '已退款', icon: '↩️', count: 0, color: 'var(--text-muted)' },
])

// 功能菜单
const menuItems = [
  { icon: '📍', label: '收货地址', desc: '管理收货地址' },
  { icon: '🎫', label: '优惠券', desc: '2张可用' },
  { icon: '⭐', label: '我的收藏', desc: '收藏的商品' },
  { icon: '💬', label: '客服中心', desc: '7x24小时在线' },
  { icon: '📋', label: '拼团记录', desc: '历史拼团' },
  { icon: '⚙️', label: '设置', desc: '账号与隐私' },
]

function handleSwitchUser(userId) {
  switchUser(userId)
  showUserPanel.value = false
  ElMessage.success(`已切换至 ${users.value.find(u => u.id === userId)?.name || userId}`)
}

function handleAddUser() {
  if (!newUserId.value.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }
  addUser(newUserId.value.trim(), newUserName.value.trim() || newUserId.value.trim())
  ElMessage.success('添加成功')
  newUserId.value = ''
  newUserName.value = ''
}

function handleRemoveUser(id) {
  removeUser(id)
  ElMessage.success('已移除')
}

function handleReset() {
  resetUsers()
  ElMessage.success('已恢复默认账号')
}

function goOrders(tab) {
  router.push('/orders')
}
</script>

<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-bg"></div>
      <div class="user-info">
        <div class="user-avatar">{{ currentUser?.avatar || '🙂' }}</div>
        <div class="user-detail">
          <div class="user-name-row">
            <span class="user-name">{{ currentUser?.name || '用户' }}</span>
            <span class="user-role-tag">{{ currentUser?.role || '客户' }}</span>
          </div>
          <div class="user-id">ID: {{ currentUser?.id || '-' }}</div>
        </div>
        <div class="user-switch-btn" @click="showUserPanel = !showUserPanel">
          <el-icon><Switch /></el-icon>
          <span>切换</span>
        </div>
      </div>
    </div>

    <!-- 用户切换面板 -->
    <transition name="slide-down">
      <div v-if="showUserPanel" class="user-panel">
        <div class="panel-header">
          <span class="panel-title">切换账号</span>
          <el-icon class="panel-close" @click="showUserPanel = false"><Close /></el-icon>
        </div>

        <!-- 当前用户 -->
        <div class="current-user-bar">
          <span class="current-label">当前登录</span>
          <div class="current-info">
            <span class="current-avatar">{{ currentUser?.avatar }}</span>
            <span class="current-name">{{ currentUser?.name }}</span>
            <span class="current-id">({{ currentUser?.id }})</span>
          </div>
        </div>

        <!-- 用户列表 -->
        <div class="user-list">
          <div
            v-for="user in users"
            :key="user.id"
            :class="['user-list-item', { active: user.id === currentUserId }]"
            @click="handleSwitchUser(user.id)"
          >
            <div class="user-list-left">
              <span class="user-list-avatar">{{ user.avatar || '🙂' }}</span>
              <div>
                <span class="user-list-name">{{ user.name }}</span>
                <span class="user-list-id">{{ user.id }}</span>
              </div>
            </div>
            <div class="user-list-right">
              <span class="user-list-role">{{ user.role }}</span>
              <el-icon v-if="user.id === currentUserId" class="check-icon"><Check /></el-icon>
              <el-icon
                v-else-if="user.role === '自定义' || user.role === '测试'"
                class="remove-icon"
                @click.stop="handleRemoveUser(user.id)"
              ><Close /></el-icon>
            </div>
          </div>
        </div>

        <!-- 添加用户 -->
        <div class="add-user-section">
          <div class="add-user-title">添加测试账号</div>
          <div class="add-user-form">
            <input v-model="newUserId" class="add-input" placeholder="用户ID" />
            <input v-model="newUserName" class="add-input" placeholder="昵称（可选）" />
            <button class="add-btn" @click="handleAddUser">添加</button>
          </div>
        </div>

        <!-- 重置 -->
        <div class="reset-bar">
          <button class="reset-btn" @click="handleReset">恢复默认账号</button>
        </div>
      </div>
    </transition>

    <!-- 订单统计 -->
    <div class="order-stats">
      <div class="stats-header">
        <span class="stats-title">我的订单</span>
        <span class="stats-more" @click="goOrders()">查看全部 <el-icon><ArrowRight /></el-icon></span>
      </div>
      <div class="stats-grid">
        <div
          v-for="stat in orderStats"
          :key="stat.key"
          class="stat-item"
          @click="goOrders(stat.key)"
        >
          <span class="stat-icon">{{ stat.icon }}</span>
          <span class="stat-label">{{ stat.label }}</span>
          <span v-if="stat.count > 0" class="stat-count" :style="{ color: stat.color }">{{ stat.count }}</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div v-for="item in menuItems" :key="item.label" class="menu-item">
        <span class="menu-icon">{{ item.icon }}</span>
        <div class="menu-text">
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-desc">{{ item.desc }}</span>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 版本信息 -->
    <div class="version-info">
      <span>拼团商城 v1.0.0</span>
      <span>Powered by DDD</span>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding-bottom: 16px;
}

/* ========== 用户卡片 ========== */
.user-card {
  position: relative;
  margin: 0 12px 12px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: white;
}
.user-bg {
  height: 80px;
  background: linear-gradient(135deg, #FF4D4F, #E02020);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px 16px;
  margin-top: -30px;
  position: relative;
}
.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.user-detail {
  flex: 1;
}
.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-name {
  font-size: 18px;
  font-weight: 700;
}
.user-role-tag {
  font-size: 10px;
  background: var(--primary-bg);
  color: var(--primary);
  padding: 2px 6px;
  border-radius: 8px;
}
.user-id {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}
.user-switch-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--primary);
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  transition: background 0.2s;
}
.user-switch-btn:active {
  background: var(--primary-bg);
}

/* ========== 用户切换面板 ========== */
.user-panel {
  margin: 0 12px 12px;
  background: white;
  border-radius: var(--radius-md);
  padding: 14px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
}
.panel-close {
  cursor: pointer;
  color: var(--text-muted);
}

.current-user-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: var(--primary-bg);
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
}
.current-label {
  font-size: 11px;
  color: var(--primary);
  background: white;
  padding: 2px 8px;
  border-radius: 10px;
}
.current-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.current-avatar {
  font-size: 20px;
}
.current-name {
  font-size: 14px;
  font-weight: 600;
}
.current-id {
  font-size: 12px;
  color: var(--text-muted);
}

/* 用户列表 */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: 12px;
}
.user-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}
.user-list-item:hover {
  background: var(--bg);
}
.user-list-item.active {
  background: var(--primary-bg);
}
.user-list-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-list-avatar {
  font-size: 24px;
}
.user-list-name {
  font-size: 14px;
  font-weight: 500;
  display: block;
}
.user-list-id {
  font-size: 11px;
  color: var(--text-muted);
}
.user-list-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-list-role {
  font-size: 10px;
  color: var(--text-muted);
  background: var(--bg);
  padding: 2px 6px;
  border-radius: 8px;
}
.check-icon {
  color: var(--primary);
  font-size: 16px;
}
.remove-icon {
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
}
.remove-icon:hover {
  color: var(--danger);
}

/* 添加用户 */
.add-user-section {
  border-top: 1px solid var(--border);
  padding-top: 12px;
  margin-bottom: 12px;
}
.add-user-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.add-user-form {
  display: flex;
  gap: 8px;
}
.add-input {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}
.add-input:focus {
  border-color: var(--primary);
}
.add-btn {
  background: var(--primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.add-btn:active {
  opacity: 0.85;
}

/* 重置 */
.reset-bar {
  text-align: center;
}
.reset-btn {
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 6px 16px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
}

/* ========== 订单统计 ========== */
.order-stats {
  background: white;
  margin: 0 12px 12px;
  border-radius: var(--radius-md);
  padding: 14px 16px;
}
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.stats-title {
  font-size: 15px;
  font-weight: 700;
}
.stats-more {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
}
.stats-grid {
  display: flex;
  justify-content: space-around;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  position: relative;
}
.stat-icon {
  font-size: 24px;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.stat-count {
  position: absolute;
  top: -4px;
  right: -8px;
  font-size: 11px;
  font-weight: 700;
  background: white;
  border-radius: 8px;
  padding: 0 4px;
}

/* ========== 功能菜单 ========== */
.menu-section {
  background: white;
  margin: 0 12px 12px;
  border-radius: var(--radius-md);
  padding: 4px 0;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #FAFAFA;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-item:active {
  background: var(--bg);
}
.menu-icon {
  font-size: 22px;
}
.menu-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.menu-label {
  font-size: 14px;
  font-weight: 500;
}
.menu-desc {
  font-size: 11px;
  color: var(--text-muted);
}
.menu-arrow {
  color: var(--text-muted);
  font-size: 14px;
}

/* ========== 版本信息 ========== */
.version-info {
  text-align: center;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 11px;
  color: var(--text-muted);
}

/* ========== 动画 ========== */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
