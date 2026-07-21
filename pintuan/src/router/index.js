import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '拼团商城', tab: 'home' },
  },
  {
    path: '/seckill',
    name: 'Seckill',
    component: () => import('@/views/SeckillView.vue'),
    meta: { title: '限时秒杀', tab: 'seckill' },
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/CategoryView.vue'),
    meta: { title: '分类', tab: 'category' },
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/OrderListView.vue'),
    meta: { title: '我的订单', tab: 'orders' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '个人中心', tab: 'profile' },
  },
  {
    path: '/product/:goodsId',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetailView.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/group/:teamId',
    name: 'GroupDetail',
    component: () => import('@/views/GroupDetailView.vue'),
    meta: { title: '拼团详情' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 动态设置页面标题
router.afterEach((to) => {
  document.title = to.meta.title || '拼团商城'
})

export default router
