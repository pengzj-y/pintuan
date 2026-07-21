<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { querySeckillActivity, doSeckill } from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUser } from '@/composables/useUser'

const router = useRouter()
const { currentUserId } = useUser()

// 秒杀活动列表
const seckillList = ref([])
const loading = ref(false)

// 秒杀详情弹窗
const seckillDetail = ref(null)
const showSeckillDetail = ref(false)
const seckilling = ref(false)

// 倒计时定时器
let countdownTimer = null

// 当前时间（用于倒计时计算）
const now = ref(Math.floor(Date.now() / 1000))

// 计算倒计时（秒）
function getCountdown(seckill) {
  if (!seckill.seckillEndTime) return 0
  const end = Math.floor(new Date(seckill.seckillEndTime).getTime() / 1000)
  const diff = end - now.value
  return diff > 0 ? diff : 0
}

// 格式化倒计时
function formatCountdown(seconds) {
  if (!seconds || seconds <= 0) return '已结束'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h}时${m}分${s}秒`
}

// 加载秒杀列表
async function loadSeckillList() {
  loading.value = true
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
    loading.value = false
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

// 判断秒杀是否进行中
function isOngoing(seckill) {
  const countdown = getCountdown(seckill)
  return countdown > 0
}

onMounted(() => {
  loadSeckillList()
  // 每秒更新倒计时
  countdownTimer = setInterval(() => {
    now.value = Math.floor(Date.now() / 1000)
  }, 1000)
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

// 当前秒杀倒计时
const currentCountdown = computed(() => {
  if (!seckillDetail.value) return 0
  return getCountdown(seckillDetail.value)
})
</script>

<template>
  <div class="seckill-page">
    <!-- 头部 -->
    <div class="seckill-header">
      <h1 class="seckill-title">⚡ 限时秒杀</h1>
      <p class="seckill-subtitle">限时限量 抢完即止</p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="seckill-loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="seckillList.length === 0" class="seckill-empty">
      <el-empty description="暂无秒杀活动，敬请期待" />
    </div>

    <!-- 秒杀列表 -->
    <div v-else class="seckill-list">
      <div
        v-for="seckill in seckillList"
        :key="seckill.activityId"
        class="seckill-card"
        @click="openSeckillDetail(seckill)"
      >
        <div class="seckill-card-left">
          <div class="seckill-goods-image">⚡</div>
        </div>
        <div class="seckill-card-center">
          <h3 class="seckill-goods-name">{{ seckill.goodsName }}</h3>
          <div class="seckill-price-row">
            <span class="seckill-price">¥{{ seckill.seckillPrice }}</span>
            <span class="seckill-original">¥{{ seckill.originalPrice }}</span>
          </div>
          <div class="seckill-progress">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: ((seckill.seckillStock - (seckill.seckillRemainStock || 0)) / seckill.seckillStock * 100) + '%' }"
              />
            </div>
            <span class="progress-text">已抢{{ Math.round((seckill.seckillStock - (seckill.seckillRemainStock || 0)) / seckill.seckillStock * 100) }}%</span>
          </div>
        </div>
        <div class="seckill-card-right">
          <div class="seckill-countdown">
            <span class="countdown-label">距结束</span>
            <span class="countdown-time">{{ formatCountdown(getCountdown(seckill)) }}</span>
          </div>
          <el-button type="danger" size="small" class="seckill-btn">
            立即抢
          </el-button>
        </div>
      </div>
    </div>

    <!-- 秒杀详情弹窗 -->
    <el-dialog v-model="showSeckillDetail" width="90%" max-width="420px" :show-close="false" class="seckill-dialog">
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
            <span class="seckill-stat-num countdown-inline">{{ formatCountdown(currentCountdown) }}</span>
            <span class="seckill-stat-label">距离结束</span>
          </div>
        </div>

        <div class="seckill-detail-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>秒杀商品每人限购1件，抢完即止</span>
        </div>

        <div class="seckill-detail-actions">
          <el-button type="danger" size="large" :loading="seckilling" @click="handleSeckill(seckillDetail)" class="seckill-action-btn">
            立即秒杀 ¥{{ seckillDetail.seckillPrice }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.seckill-page {
  padding-bottom: 16px;
  min-height: 100vh;
  background: #f5f5f5;
}

/* ========== Header ========== */
.seckill-header {
  background: linear-gradient(135deg, #E02E24 0%, #FF6B00 100%);
  padding: 24px 20px;
  text-align: center;
  color: white;
}
.seckill-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 6px;
}
.seckill-subtitle {
  font-size: 13px;
  opacity: 0.9;
  margin: 0;
}

/* ========== Loading & Empty ========== */
.seckill-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: #999;
}
.loading-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.seckill-empty {
  padding: 40px 0;
}

/* ========== Seckill List ========== */
.seckill-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.seckill-card {
  background: white;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.seckill-card:active {
  transform: scale(0.98);
}
.seckill-card-left {
  flex-shrink: 0;
}
.seckill-goods-image {
  width: 72px;
  height: 72px;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
  border-radius: 8px;
}
.seckill-card-center {
  flex: 1;
  min-width: 0;
}
.seckill-goods-name {
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
  margin-bottom: 8px;
}
.seckill-price {
  font-size: 22px;
  font-weight: 700;
  color: #E02E24;
}
.seckill-original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}
.seckill-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}
.progress-bar {
  flex: 1;
  height: 6px;
  background: #FFE0E0;
  border-radius: 3px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #E02E24, #FF6B00);
  border-radius: 3px;
  transition: width 0.3s;
}
.progress-text {
  font-size: 11px;
  color: #E02E24;
  flex-shrink: 0;
}
.seckill-card-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.seckill-countdown {
  text-align: center;
}
.countdown-label {
  display: block;
  font-size: 10px;
  color: #999;
}
.countdown-time {
  font-size: 12px;
  color: #FF6B00;
  font-weight: 600;
}
.seckill-btn {
  background: linear-gradient(135deg, #E02E24 0%, #FF6B00 100%);
  border: none;
  font-weight: 600;
  padding: 0 16px;
}

/* ========== Dialog ========== */
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
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
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
  font-size: 28px;
  font-weight: 700;
  color: #E02E24;
}
.seckill-detail-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}
.seckill-detail-save {
  font-size: 12px;
  color: #E02E24;
  background: #FFF0F0;
  padding: 2px 6px;
  border-radius: 4px;
}

.seckill-detail-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #F0F0F0;
  border-bottom: 1px solid #F0F0F0;
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
.countdown-inline {
  font-size: 16px;
  color: #FF6B00;
}

.seckill-detail-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  background: #FFF8E1;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #F57C00;
}

.seckill-detail-actions {
  padding-top: 4px;
}
.seckill-action-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  height: 44px;
  background: linear-gradient(135deg, #E02E24 0%, #FF6B00 100%);
  border: none;
}
</style>
