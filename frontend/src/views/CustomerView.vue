<script setup>
import { ref, computed, onMounted } from 'vue'
import { queryGroupBuyMarketConfig, lockMarketPayOrder, querySeckillActivity, doSeckill } from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUser } from '@/composables/useUser'

const { users, currentUserId, currentUser, switchUser } = useUser()

// 商品列表
const products = ref([
  { goodsId: '9890001', activityId: 100123, name: '《手写MyBatis：渐进式源码实践》', image: '📚' },
  { goodsId: '9890002', activityId: 100124, name: '《Redis实战与源码分析》', image: '🔴' },
  { goodsId: '9890003', activityId: 100125, name: '《SpringBoot微服务架构实战》', image: '🍃' },
  { goodsId: '9890004', activityId: 100126, name: '《DDD领域驱动设计》', image: '🏗️' },
  { goodsId: '9890005', activityId: 100127, name: '《Netty网络编程实战》', image: '🌐' },
  { goodsId: '9890006', activityId: 100128, name: '《深入理解JVM虚拟机》', image: '☕' },
  { goodsId: '9890007', activityId: 100129, name: '《算法导论：从零到精通》', image: '🧮' },
])

// 状态
const loading = ref({})
const productData = ref({})
const selectedProduct = ref(null)
const showDetail = ref(false)
const showUserPanel = ref(false)
const joining = ref(false)

// 秒杀相关状态
const seckillList = ref([])
const seckillLoading = ref(false)
const seckillDetail = ref(null)
const showSeckillDetail = ref(false)
const seckilling = ref(false)

// 当前选中的商品信息
const currentProduct = computed(() => {
  if (!selectedProduct.value) return null
  return productData.value[selectedProduct.value.goodsId]
})

// 加载所有商品数据
async function loadAllProducts() {
  for (const product of products.value) {
    loading.value[product.goodsId] = true
    try {
      const res = await queryGroupBuyMarketConfig({
        userId: currentUserId.value,
        source: 's01',
        channel: 'c01',
        goodsId: product.goodsId,
      })
      if (res.code === '0000') {
        productData.value[product.goodsId] = res.data
      }
    } catch {
      // 忽略错误
    } finally {
      loading.value[product.goodsId] = false
    }
  }
}

// 加载秒杀列表
async function loadSeckillList() {
  seckillLoading.value = true
  try {
    const res = await querySeckillActivity({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
    })
    if (res.code === '0000') {
      seckillList.value = res.data || []
    }
  } catch {
    // 忽略错误
  } finally {
    seckillLoading.value = false
  }
}

// 打开商品详情
function openDetail(product) {
  selectedProduct.value = product
  showDetail.value = true
}

// 参与拼团
async function joinGroup(teamId) {
  if (!selectedProduct.value) return
  joining.value = true
  let outTradeNo = ''
  for (let i = 0; i < 12; i++) {
    outTradeNo += Math.floor(Math.random() * 10)
  }
  try {
    const res = await lockMarketPayOrder({
      userId: currentUserId.value,
      teamId: teamId || null,
      activityId: selectedProduct.value.activityId,
      goodsId: selectedProduct.value.goodsId,
      source: 's01',
      channel: 'c01',
      outTradeNo: outTradeNo,
      notifyConfigVO: { notifyType: 'HTTP', notifyUrl: 'http://127.0.0.1:8091/api/v1/test/group_buy_notify' },
    })
    if (res.code === '0000') {
      ElMessageBox.confirm(
        `锁单成功！\n订单号: ${res.data.orderId}\n实付: ¥${res.data.payPrice}`,
        '拼团成功',
        { confirmButtonText: '确定', type: 'success' }
      )
      showDetail.value = false
      loadAllProducts()
    }
  } catch {
    ElMessage.error('锁单失败')
  } finally {
    joining.value = false
  }
}

// 查看秒杀详情
function openSeckillDetail(seckill) {
  seckillDetail.value = seckill
  showSeckillDetail.value = true
}

// 执行秒杀
async function handleSeckill(seckill) {
  seckilling.value = true
  let outTradeNo = ''
  for (let i = 0; i < 12; i++) {
    outTradeNo += Math.floor(Math.random() * 10)
  }
  try {
    const res = await doSeckill({
      userId: currentUserId.value,
      activityId: seckill.activityId,
      source: 's01',
      channel: 'c01',
      outTradeNo: outTradeNo,
    })
    if (res.code === '0000') {
      ElMessageBox.confirm(
        `秒杀成功！\n订单号: ${res.data.orderId}\n秒杀价: ¥${res.data.seckillPrice}`,
        '恭喜抢到了',
        { confirmButtonText: '确定', type: 'success' }
      )
      showSeckillDetail.value = false
      loadSeckillList()
    }
  } catch {
    ElMessage.error('秒杀失败，请重试')
  } finally {
    seckilling.value = false
  }
}

// 切换用户
function handleSwitchUser(userId) {
  switchUser(userId)
  showUserPanel.value = false
  loadAllProducts()
  loadSeckillList()
}

// 格式化倒计时
function formatCountdown(seconds) {
  if (!seconds || seconds <= 0) return '已结束'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h}时${m}分${s}秒`
}

onMounted(() => {
  loadAllProducts()
  loadSeckillList()
})
</script>

<template>
  <div class="customer-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo">
          <span class="logo-icon">🛒</span>
          <span class="logo-name">拼团商城</span>
        </div>
        <div class="header-right">
          <div class="user-trigger" @click="showUserPanel = !showUserPanel">
            <el-icon><User /></el-icon>
            <span>{{ currentUser?.name || currentUserId }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </div>
          <!-- 用户切换面板 -->
          <div v-show="showUserPanel" class="user-dropdown">
            <div class="user-dropdown-title">切换测试账号</div>
            <div
              v-for="user in users"
              :key="user.id"
              :class="['user-dropdown-item', { active: user.id === currentUserId }]"
              @click="handleSwitchUser(user.id)"
            >
              <el-icon v-if="user.id === currentUserId"><Check /></el-icon>
              <span class="user-id">{{ user.id }}</span>
              <span class="user-role">{{ user.role }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Banner -->
    <div class="banner">
      <div class="banner-content">
        <h1>拼团狂欢节</h1>
        <p>邀请好友一起拼，优惠享不停</p>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section">
      <h2 class="section-title">🔥 热门拼团</h2>
      <div class="product-grid">
        <div
          v-for="product in products"
          :key="product.goodsId"
          class="product-card"
          @click="openDetail(product)"
        >
          <div class="product-image">{{ product.image }}</div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div v-if="loading[product.goodsId]" class="product-loading">
              <el-icon class="loading-icon"><Loading /></el-icon>
            </div>
            <template v-else-if="productData[product.goodsId]">
              <div class="price-row">
                <span class="group-price">¥{{ productData[product.goodsId]?.goods?.payPrice }}</span>
                <span class="original-price">¥{{ productData[product.goodsId]?.goods?.originalPrice }}</span>
              </div>
              <div class="team-info">
                <span class="team-count">{{ productData[product.goodsId]?.teamStatistic?.allTeamCount || 0 }}人正在拼团</span>
              </div>
            </template>
            <div v-else class="product-error">加载失败</div>
          </div>
          <div class="product-tag">拼团</div>
        </div>
      </div>
    </div>

    <!-- 秒杀活动列表 -->
    <div class="seckill-section">
      <h2 class="section-title">⚡ 限时秒杀</h2>
      <div v-if="seckillLoading" class="seckill-loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="seckillList.length === 0" class="seckill-empty">
        <el-empty description="暂无秒杀活动，敬请期待" />
      </div>
      <div v-else class="seckill-grid">
        <div
          v-for="seckill in seckillList"
          :key="seckill.activityId"
          class="seckill-card"
          @click="openSeckillDetail(seckill)"
        >
          <div class="seckill-image">⚡</div>
          <div class="seckill-info">
            <h3 class="seckill-name">{{ seckill.goodsName }}</h3>
            <div class="seckill-price-row">
              <span class="seckill-price">¥{{ seckill.seckillPrice }}</span>
              <span class="seckill-original">¥{{ seckill.originalPrice }}</span>
            </div>
            <div class="seckill-countdown">
              <span class="countdown-icon">⏱</span>
              <span>{{ formatCountdown(seckill.seckillCountdown) }}</span>
            </div>
          </div>
          <div class="seckill-tag">秒杀</div>
        </div>
      </div>
    </div>

    <!-- 秒杀详情弹窗 -->
    <el-dialog v-model="showSeckillDetail" width="90%" max-width="450px" :show-close="false" class="seckill-dialog">
      <div v-if="seckillDetail" class="seckill-detail-content">
        <div class="seckill-detail-header">
          <div class="seckill-detail-image">⚡</div>
          <div class="seckill-detail-info">
            <h2 class="seckill-detail-name">{{ seckillDetail.goodsName }}</h2>
            <div class="seckill-detail-price-row">
              <span class="seckill-detail-price">¥{{ seckillDetail.seckillPrice }}</span>
              <span class="seckill-detail-original">¥{{ seckillDetail.originalPrice }}</span>
              <span class="seckill-detail-save">省¥{{ (seckillDetail.originalPrice - seckillDetail.seckillPrice).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="seckill-detail-stats">
          <div class="seckill-stat">
            <span class="seckill-stat-num">{{ seckillDetail.seckillStock }}</span>
            <span class="seckill-stat-label">剩余库存</span>
          </div>
          <div class="seckill-stat">
            <span class="seckill-stat-num">{{ formatCountdown(seckillDetail.seckillCountdown) }}</span>
            <span class="seckill-stat-label">距离结束</span>
          </div>
        </div>

        <div class="seckill-detail-actions">
          <el-button type="danger" size="large" :loading="seckilling" @click="handleSeckill(seckillDetail)" class="seckill-btn">
            立即秒杀 ¥{{ seckillDetail.seckillPrice }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="showDetail" width="90%" max-width="500px" :show-close="false" class="detail-dialog">
      <div v-if="selectedProduct && currentProduct" class="detail-content">
        <!-- 商品头部 -->
        <div class="detail-header">
          <div class="detail-image">{{ selectedProduct.image }}</div>
          <div class="detail-info">
            <h2 class="detail-name">{{ selectedProduct.name }}</h2>
            <div class="detail-price-row">
              <span class="detail-price">¥{{ currentProduct.goods?.payPrice }}</span>
              <span class="detail-original">¥{{ currentProduct.goods?.originalPrice }}</span>
              <span class="detail-save">省¥{{ currentProduct.goods?.deductionPrice }}</span>
            </div>
          </div>
        </div>

        <!-- 拼团统计 -->
        <div class="detail-stats">
          <div class="stat">
            <span class="stat-num">{{ currentProduct.teamStatistic?.allTeamCount || 0 }}</span>
            <span class="stat-label">开团队伍</span>
          </div>
          <div class="stat">
            <span class="stat-num">{{ currentProduct.teamStatistic?.allTeamCompleteCount || 0 }}</span>
            <span class="stat-label">已成团</span>
          </div>
          <div class="stat">
            <span class="stat-num">{{ currentProduct.teamStatistic?.allTeamUserCount || 0 }}</span>
            <span class="stat-label">参团人数</span>
          </div>
        </div>

        <!-- 拼团队伍 -->
        <div class="team-section">
          <h3 class="team-section-title">正在拼团，可直接参与</h3>
          <div v-if="currentProduct.teamList && currentProduct.teamList.length > 0" class="team-list">
            <div v-for="team in currentProduct.teamList" :key="team.teamId" class="team-item">
              <div class="team-progress-info">
                <span>还差 <strong>{{ team.targetCount - team.lockCount }}</strong> 人成团</span>
                <span class="countdown">⏱ {{ team.validTimeCountdown }}</span>
              </div>
              <el-progress
                :percentage="Math.round((team.lockCount / team.targetCount) * 100)"
                :format="() => `${team.lockCount}/${team.targetCount}`"
              />
              <el-button type="danger" size="small" :loading="joining" @click="joinGroup(team.teamId)">
                参与拼团
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无进行中的队伍，快来开团吧！" />
        </div>

        <!-- 开团按钮 -->
        <div class="detail-actions">
          <el-button type="danger" size="large" :loading="joining" @click="joinGroup(null)" class="open-team-btn">
            自己开团 ¥{{ currentProduct.goods?.payPrice }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.customer-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

/* ========== Header ========== */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 28px;
}

.logo-name {
  font-size: 20px;
  font-weight: 700;
  color: #e02e24;
}

.header-right {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: #f5f5f5;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background 0.2s;
}

.user-trigger:hover {
  background: #e8e8e8;
}

.arrow {
  font-size: 12px;
  transition: transform 0.2s;
}

.user-dropdown {
  position: absolute;
  top: 40px;
  right: 0;
  width: 200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 8px;
  z-index: 200;
}

.user-dropdown-title {
  font-size: 12px;
  color: #999;
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}

.user-dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.user-dropdown-item:hover {
  background: #f5f5f5;
}

.user-dropdown-item.active {
  background: #fff0f0;
  color: #e02e24;
}

.user-id {
  flex: 1;
}

.user-role {
  font-size: 10px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
}

/* ========== Banner ========== */
.banner {
  background: linear-gradient(135deg, #e02e24 0%, #ff6b6b 100%);
  padding: 40px 20px;
  text-align: center;
  color: white;
}

.banner h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
}

.banner p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* ========== Product Section ========== */
.product-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.group-price {
  font-size: 22px;
  font-weight: 700;
  color: #e02e24;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.team-info {
  margin-top: 6px;
}

.team-count {
  font-size: 12px;
  color: #ff6b00;
}

.product-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #e02e24;
  color: white;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.product-loading {
  padding: 10px 0;
  text-align: center;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.product-error {
  color: #999;
  font-size: 13px;
  padding: 8px 0;
}

/* ========== Detail Dialog ========== */
.detail-content {
  padding: 0;
}

.detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-image {
  width: 100px;
  height: 100px;
  font-size: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  flex-shrink: 0;
}

.detail-info {
  flex: 1;
}

.detail-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 10px;
  line-height: 1.4;
}

.detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.detail-price {
  font-size: 26px;
  font-weight: 700;
  color: #e02e24;
}

.detail-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.detail-save {
  font-size: 12px;
  color: #e02e24;
  background: #fff0f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.detail-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.detail-stats .stat {
  text-align: center;
}

.detail-stats .stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.detail-stats .stat-label {
  font-size: 12px;
  color: #999;
}

.team-section {
  margin-bottom: 16px;
}

.team-section-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #333;
}

.team-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.team-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.team-progress-info {
  flex: 1;
  font-size: 13px;
  color: #666;
}

.team-progress-info strong {
  color: #e02e24;
}

.countdown {
  color: #ff6b00;
  margin-left: 8px;
}

.team-item :deep(.el-progress) {
  flex: 1;
}

.detail-actions {
  padding-top: 12px;
}

.open-team-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  height: 44px;
}

/* ========== Dialog Override ========== */
.detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.detail-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

/* ========== Seckill Section ========== */
.seckill-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.seckill-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #999;
}

.seckill-empty {
  padding: 20px 0;
}

.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.seckill-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
  border: 2px solid #ff6b00;
}

.seckill-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 107, 0, 0.15);
}

.seckill-image {
  width: 60px;
  height: 60px;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-radius: 8px;
  flex-shrink: 0;
}

.seckill-info {
  flex: 1;
  min-width: 0;
}

.seckill-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 6px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seckill-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.seckill-price {
  font-size: 22px;
  font-weight: 700;
  color: #e02e24;
}

.seckill-original {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.seckill-countdown {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #ff6b00;
}

.countdown-icon {
  font-size: 14px;
}

.seckill-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, #ff6b00 0%, #ff9800 100%);
  color: white;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 600;
}

/* ========== Seckill Dialog ========== */
.seckill-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.seckill-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.seckill-detail-content {
  padding: 0;
}

.seckill-detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.seckill-detail-image {
  width: 80px;
  height: 80px;
  font-size: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-radius: 8px;
  flex-shrink: 0;
}

.seckill-detail-info {
  flex: 1;
}

.seckill-detail-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 10px;
  line-height: 1.4;
}

.seckill-detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.seckill-detail-price {
  font-size: 26px;
  font-weight: 700;
  color: #e02e24;
}

.seckill-detail-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.seckill-detail-save {
  font-size: 12px;
  color: #e02e24;
  background: #fff0f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.seckill-detail-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.seckill-stat {
  text-align: center;
}

.seckill-stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.seckill-stat-label {
  font-size: 12px;
  color: #999;
}

.seckill-detail-actions {
  padding-top: 8px;
}

.seckill-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  height: 44px;
  background: linear-gradient(135deg, #e02e24 0%, #ff6b00 100%);
  border: none;
}
</style>
