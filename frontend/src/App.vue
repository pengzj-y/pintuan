<script setup>
import { ref, provide } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const currentUserId = ref('xfg01');

const menuItems = [
    { path: '/home', label: '营销首页', icon: 'HomeFilled' },
    { path: '/trade', label: '交易下单', icon: 'ShoppingCart' },
    { path: '/order', label: '订单管理', icon: 'List' },
    { path: '/dcc', label: '动态配置', icon: 'Setting' },
    { path: '/test', label: '模拟回调', icon: 'Promotion' },
];

function navigate(path) {
    router.push(path);
}

// 跨页面通信方法
const tradeRef = ref(null);
const orderRef = ref(null);

function handleJoinTeam(teamId) {
    navigate('/trade');
    // 等待页面加载后设置 teamId
    setTimeout(() => {
        const tradeView = document.querySelector('.page-container');
        if (tradeView && tradeView.__vueParentComponent) {
            tradeView.__vueParentComponent.exposed?.setTeamId?.(teamId);
        }
    }, 300);
}

function handleFillSettle(outTradeNo) {
    navigate('/trade');
}

function handleFillRefund(outTradeNo) {
    navigate('/trade');
}

provide('currentUserId', currentUserId);
</script>

<template>
    <div class="layout">
        <!-- 侧边栏 -->
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
            <div class="user-bar">
                <el-input v-model="currentUserId" size="small" placeholder="用户ID">
                    <template #prefix>用户:</template>
                </el-input>
            </div>
        </aside>

        <!-- 主内容 -->
        <main class="main">
            <router-view v-slot="{ Component }">
                <component :is="Component" :currentUserId="currentUserId" />
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

.user-bar {
    padding: 16px;
    border-top: 1px solid #334155;
}

.main {
    flex: 1;
    margin-left: 220px;
    background: #f1f5f9;
}
</style>
