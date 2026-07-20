import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    {
        path: '/',
        redirect: '/home',
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '营销首页' },
    },
    {
        path: '/trade',
        name: 'Trade',
        component: () => import('@/views/TradeView.vue'),
        meta: { title: '交易下单' },
    },
    {
        path: '/order',
        name: 'Order',
        component: () => import('@/views/OrderView.vue'),
        meta: { title: '订单管理' },
    },
    {
        path: '/dcc',
        name: 'DCC',
        component: () => import('@/views/DccView.vue'),
        meta: { title: '动态配置' },
    },
    {
        path: '/test',
        name: 'Test',
        component: () => import('@/views/TestView.vue'),
        meta: { title: '模拟回调' },
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
