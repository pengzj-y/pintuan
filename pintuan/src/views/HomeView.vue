<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { queryGroupBuyMarketConfig } from '@/api/index'
import ProductCard from '@/components/ProductCard.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 商品列表（首页展示多个商品）
const products = ref([])
const loading = ref(false)

// 滚动提示
const tips = [
  '用户 xfg02 刚刚拼团成功',
  '用户 xfg03 邀请了好友参团',
  '用户 xfg01 开团成功，还差2人',
  '用户 xfg04 拼团成功，节省了 ¥29.9',
  '用户 xfg05 刚刚参团',
]
const tipIndex = ref(0)
let tipTimer = null

// 商品池（模拟多个商品，共用同一活动配置）
const goodsPool = [
  { goodsId: '9890001', title: '新疆阿克苏苹果 5kg 脆甜多汁 产地直发', image: 'https://img.yzcdn.cn/vant/cat.jpeg', tag: '热卖', sales: 2380, targetCount: 3 },
  { goodsId: '9890002', title: '海南金煌芒果 2.5kg 核小肉厚 香甜软糯', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', tag: '新品', sales: 856, targetCount: 2 },
  { goodsId: '9890003', title: '四川丑橘 5kg 皮薄汁多 酸甜爽口', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', tag: '', sales: 1240, targetCount: 5 },
  { goodsId: '9890004', title: '云南红宝石车厘子 2kg 当季新鲜水果', image: 'https://img.yzcdn.cn/vant/cat.jpeg', tag: '限时', sales: 568, targetCount: 3 },
  { goodsId: '9890005', title: '福建琯溪蜜柚 4个装 红心柚子 清甜润肺', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', tag: '', sales: 342, targetCount: 2 },
  { goodsId: '9890006', title: '赣南脐橙 5kg 橙子 皮薄汁多 维C满满', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', tag: '热卖', sales: 1890, targetCount: 3 },
]

// 拼团价配置（本地模拟，实际应从接口获取）
const priceConfig = {
  '9890001': { originalPrice: '59.8', payPrice: '29.9', deductionPrice: '29.9' },
  '9890002': { originalPrice: '69.9', payPrice: '35.8', deductionPrice: '34.1' },
  '9890003': { originalPrice: '49.9', payPrice: '25.8', deductionPrice: '24.1' },
  '9890004': { originalPrice: '128.0', payPrice: '68.0', deductionPrice: '60.0' },
  '9890005': { originalPrice: '45.8', payPrice: '22.9', deductionPrice: '22.9' },
  '9890006': { originalPrice: '55.8', payPrice: '28.8', deductionPrice: '27.0' },
}

onMounted(async () => {
  loading.value = true
  try {
    // 查询第一个商品的配置，获取活动信息
    const res = await queryGroupBuyMarketConfig({
      userId: 'xfg01',
      source: 's01',
      channel: 'c01',
      goodsId: '9890001',
    })
    if (res.code === '0000') {
      // 用接口返回的统计数据
      const data = res.data
      // 构建商品列表
      buildProducts(data)
    }
  } catch (e) {
    // 接口不可用时使用本地数据
    buildProducts(null)
  } finally {
    loading.value = false
  }

  // 启动滚动提示轮播
  tipTimer = setInterval(() => {
    tipIndex.value = (tipIndex.value + 1) % tips.length
  }, 3000)
})

onUnmounted(() => {
  if (tipTimer) clearInterval(tipTimer)
})

function buildProducts(apiData) {
  products.value = goodsPool.map(g => {
    const price = priceConfig[g.goodsId] || { originalPrice: '99.0', payPrice: '49.0', deductionPrice: '50.0' }
    return {
      ...g,
      ...price,
      activityId: apiData?.activityId || 100123,
    }
  })
}

function goProduct(product) {
  router.push(`/product/${product.goodsId}`)
}
</script>

<template>
  <div class="home">
    <!-- Banner 区域 -->
    <div class="banner">
      <div class="banner-content">
        <div class="banner-title">全民拼团</div>
        <div class="banner-subtitle">拼着买 更便宜</div>
        <div class="banner-tag">
          <span class="tag-icon">🔥</span>
          <span>万人拼团 低至1折</span>
        </div>
      </div>
      <div class="banner-deco">🎉</div>
    </div>

    <!-- 活动入口 -->
    <div class="quick-entries">
      <div class="entry-item">
        <div class="entry-icon" style="background:#FFF0E6">💰</div>
        <span>0元开团</span>
      </div>
      <div class="entry-item">
        <div class="entry-icon" style="background:#E6F7FF">⏰</div>
        <span>限时秒杀</span>
      </div>
      <div class="entry-item">
        <div class="entry-icon" style="background:#FFF1F0">👥</div>
        <span>新人拼团</span>
      </div>
      <div class="entry-item">
        <div class="entry-icon" style="background:#F6FFED">🏆</div>
        <span>排行榜</span>
      </div>
    </div>

    <!-- 正在拼团提示条 -->
    <div class="scroll-tip">
      <span class="tip-icon">📢</span>
      <div class="tip-text">
        <transition name="fade" mode="out-in">
          <span :key="tipIndex" class="tip-item">{{ tips[tipIndex] }}</span>
        </transition>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="section-header">
      <span class="section-title">🔥 热门拼团</span>
      <span class="section-more">更多 <el-icon><ArrowRight /></el-icon></span>
    </div>

    <div v-loading="loading" class="product-list">
      <ProductCard
        v-for="product in products"
        :key="product.goodsId"
        :product="product"
        @click="goProduct"
      />
    </div>

    <div v-if="!loading && products.length === 0" class="empty">
      <el-empty description="暂无拼团商品" />
    </div>
  </div>
</template>

<style scoped>
.home {
  padding-bottom: 16px;
}

/* ========== Banner ========== */
.banner {
  background: linear-gradient(135deg, #FF4D4F 0%, #E02020 50%, #B81414 100%);
  margin: 0 12px 12px;
  border-radius: var(--radius-lg);
  padding: 20px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}
.banner::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
}
.banner::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 30%;
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.08);
  border-radius: 50%;
}
.banner-content {
  position: relative;
  z-index: 1;
}
.banner-title {
  font-size: 24px;
  font-weight: 800;
  color: white;
  letter-spacing: 2px;
}
.banner-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.9);
  margin-top: 4px;
}
.banner-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(255,255,255,0.2);
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  margin-top: 10px;
}
.tag-icon {
  font-size: 14px;
}
.banner-deco {
  font-size: 48px;
  position: relative;
  z-index: 1;
  animation: bounce 2s ease-in-out infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* ========== 快捷入口 ========== */
.quick-entries {
  display: flex;
  justify-content: space-around;
  padding: 16px 8px;
  background: white;
  margin: 0 12px 12px;
  border-radius: var(--radius-md);
}
.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.entry-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.entry-item span {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== 滚动提示 ========== */
.scroll-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 12px 12px;
  padding: 8px 12px;
  background: var(--orange-light);
  border-radius: var(--radius-sm);
}
.tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}
.tip-text {
  flex: 1;
  overflow: hidden;
  height: 18px;
  position: relative;
}
.tip-item {
  font-size: 12px;
  color: var(--orange);
  position: absolute;
}

/* ========== 商品列表 ========== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 12px;
}
.section-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text);
}
.section-more {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 2px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 12px;
  min-height: 100px;
}

.empty {
  padding: 40px 0;
}
</style>
