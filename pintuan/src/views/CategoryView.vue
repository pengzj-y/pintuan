<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const categories = [
  { id: 'c1', name: '新鲜水果', icon: '🍎', color: '#FF4D4F' },
  { id: 'c2', name: '蔬菜生鲜', icon: '🥬', color: '#00B578' },
  { id: 'c3', name: '肉禽蛋品', icon: '🥩', color: '#FF6B00' },
  { id: 'c4', name: '海鲜水产', icon: '🦐', color: '#1677FF' },
  { id: 'c5', name: '乳品烘焙', icon: '🧀', color: '#FFC300' },
  { id: 'c6', name: '零食酒水', icon: '🍪', color: '#E02020' },
  { id: 'c7', name: '粮油调味', icon: '🫒', color: '#8B5CF6' },
  { id: 'c8', name: '日用百货', icon: '🧴', color: '#06B6D4' },
]

const hotCategories = [
  { id: '9890001', title: '新疆阿克苏苹果 5kg', image: 'https://img.yzcdn.cn/vant/cat.jpeg', payPrice: '29.9', tag: '热卖' },
  { id: '9890002', title: '海南金煌芒果 2.5kg', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', payPrice: '35.8', tag: '新品' },
  { id: '9890003', title: '四川丑橘 5kg', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', payPrice: '25.8', tag: '' },
  { id: '9890004', title: '云南车厘子 2kg', image: 'https://img.yzcdn.cn/vant/cat.jpeg', payPrice: '68.0', tag: '限时' },
]

function goProduct(goodsId) {
  router.push(`/product/${goodsId}`)
}
</script>

<template>
  <div class="category-page">
    <div class="page-title">商品分类</div>

    <!-- 分类网格 -->
    <div class="cat-grid">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="cat-item"
        @click="goProduct('9890001')"
      >
        <div class="cat-icon" :style="{ background: cat.color + '15' }">
          <span>{{ cat.icon }}</span>
        </div>
        <span class="cat-name">{{ cat.name }}</span>
      </div>
    </div>

    <!-- 热门推荐 -->
    <div class="hot-section">
      <div class="hot-title">🔥 热门拼团</div>
      <div class="hot-list">
        <div
          v-for="item in hotCategories"
          :key="item.id"
          class="hot-item"
          @click="goProduct(item.id)"
        >
          <div class="hot-img">
            <img :src="item.image" />
            <span v-if="item.tag" class="hot-tag">{{ item.tag }}</span>
          </div>
          <p class="hot-name text-ellipsis">{{ item.title }}</p>
          <div class="hot-price">
            <span class="price"><span class="price-symbol">¥</span>{{ item.payPrice }}</span>
            <span class="hot-label">拼团价</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-page {
  padding-bottom: 16px;
}

.page-title {
  font-size: 17px;
  font-weight: 700;
  padding: 16px;
  background: white;
}

/* ========== 分类网格 ========== */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  background: white;
  margin-bottom: 8px;
}
.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.cat-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}
.cat-name {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== 热门推荐 ========== */
.hot-section {
  background: white;
  padding: 16px;
}
.hot-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 12px;
}
.hot-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.hot-item {
  background: #FAFAFA;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.15s;
}
.hot-item:active {
  transform: scale(0.97);
}
.hot-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
}
.hot-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hot-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  background: var(--primary);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
}
.hot-name {
  font-size: 13px;
  font-weight: 500;
  padding: 8px 10px 4px;
  line-height: 1.3;
}
.hot-price {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px 10px;
}
.hot-label {
  font-size: 10px;
  color: var(--primary);
  background: var(--primary-bg);
  padding: 1px 5px;
  border-radius: 3px;
}
</style>
