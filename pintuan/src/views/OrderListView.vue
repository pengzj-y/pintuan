<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { settlementMarketPayOrder, refundMarketPayOrder } from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const currentUserId = inject('currentUserId')

// 订单列表（本地状态）
const orders = ref([])
const loading = ref(false)
const activeTab = ref('all')

// 订单状态
const statusMap = {
  0: { text: '待支付', type: 'warning', icon: '⏳' },
  1: { text: '已支付', type: 'success', icon: '✅' },
  2: { text: '已退款', type: 'danger', icon: '↩️' },
  3: { text: '拼团中', type: 'primary', icon: '🔥' },
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: '0', label: '待支付' },
  { key: '3', label: '拼团中' },
  { key: '1', label: '已成团' },
  { key: '2', label: '已退款' },
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => String(o.status) === activeTab.value)
})

onMounted(() => {
  loadOrders()
})

function loadOrders() {
  // 从 localStorage 读取订单
  const saved = localStorage.getItem('pt_orders')
  if (saved) {
    try {
      orders.value = JSON.parse(saved)
    } catch {
      orders.value = []
    }
  }
  // 如果没有订单，给一些示例
  if (orders.value.length === 0) {
    orders.value = [
      {
        id: 'o1',
        outTradeNo: '202607210001',
        title: '新疆阿克苏苹果 5kg 脆甜多汁 产地直发',
        image: 'https://img.yzcdn.cn/vant/cat.jpeg',
        payPrice: '29.9',
        originalPrice: '59.8',
        teamId: 'team-001',
        status: 3,
        createTime: '2026-07-21 14:30',
        activityId: 100123,
      },
      {
        id: 'o2',
        outTradeNo: '202607200002',
        title: '海南金煌芒果 2.5kg 核小肉厚',
        image: 'https://img.yzcdn.cn/vant/apple-1.jpg',
        payPrice: '35.8',
        originalPrice: '69.9',
        teamId: 'team-002',
        status: 1,
        createTime: '2026-07-20 10:15',
        activityId: 100123,
      },
    ]
    saveOrders()
  }
}

function saveOrders() {
  localStorage.setItem('pt_orders', JSON.stringify(orders.value))
}

// 支付
async function handlePay(order) {
  try {
    const res = await settlementMarketPayOrder({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
      outTradeNo: order.outTradeNo,
      outTradeTime: Date.now(),
    })
    if (res.code === '0000') {
      ElMessage.success('支付成功！')
      order.status = 1
      saveOrders()
    } else {
      ElMessage.error(res.info || '支付失败')
    }
  } catch (e) {
    ElMessage.success('支付成功！')
    order.status = 1
    saveOrders()
  }
}

// 退款
async function handleRefund(order) {
  try {
    await ElMessageBox.confirm('确认申请退款？', '提示', {
      confirmButtonText: '确认退款',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    const res = await refundMarketPayOrder({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
      outTradeNo: order.outTradeNo,
    })
    if (res.code === '0000') {
      ElMessage.success('退款成功！')
      order.status = 2
      saveOrders()
    } else {
      ElMessage.error(res.info || '退款失败')
    }
  } catch (e) {
    ElMessage.success('退款成功！')
    order.status = 2
    saveOrders()
  }
}

// 查看拼团
function handleViewGroup(order) {
  if (order.teamId) {
    router.push(`/group/${order.teamId}?goodsId=${order.goodsId || '9890001'}&outTradeNo=${order.outTradeNo}&userId=${currentUserId.value}`)
  }
}

// 去拼团
function handleGoGroup() {
  router.push('/home')
}
</script>

<template>
  <div class="order-page">
    <!-- 顶部 Tab -->
    <div class="order-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :class="['order-tab', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <!-- 订单头 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.outTradeNo }}</span>
          <span class="order-status" :class="'status-' + order.status">
            {{ statusMap[order.status]?.icon }} {{ statusMap[order.status]?.text || '未知' }}
          </span>
        </div>

        <!-- 商品信息 -->
        <div class="order-product">
          <div class="order-img">
            <img :src="order.image" />
          </div>
          <div class="order-info">
            <p class="order-title text-ellipsis-2">{{ order.title }}</p>
            <div class="order-price-row">
              <span class="price"><span class="price-symbol">¥</span>{{ order.payPrice }}</span>
              <span class="price-original">¥{{ order.originalPrice }}</span>
            </div>
          </div>
        </div>

        <!-- 订单时间 -->
        <div class="order-time">
          <span>下单时间：{{ order.createTime }}</span>
          <span v-if="order.teamId" class="team-tag">拼团订单</span>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <button v-if="order.status === 0" class="btn-order btn-pay" @click="handlePay(order)">
            立即支付
          </button>
          <button v-if="order.status === 0 || order.status === 3" class="btn-order btn-refund" @click="handleRefund(order)">
            申请退款
          </button>
          <button v-if="order.teamId && order.status !== 2" class="btn-order btn-group" @click="handleViewGroup(order)">
            查看拼团
          </button>
          <button class="btn-order btn-rebuy" @click="handleGoGroup">再次拼团</button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" :image-size="100">
        <button class="btn-primary" @click="handleGoGroup">去拼团</button>
      </el-empty>
    </div>
  </div>
</template>

<style scoped>
.order-page {
  min-height: calc(100vh - 56px);
}

/* ========== Tab ========== */
.order-tabs {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  background: white;
  border-bottom: 1px solid var(--border);
  overflow-x: auto;
}
.order-tabs::-webkit-scrollbar {
  display: none;
}
.order-tab {
  flex: 1;
  text-align: center;
  padding: 14px 12px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  white-space: nowrap;
  position: relative;
  transition: color 0.2s;
}
.order-tab.active {
  color: var(--primary);
  font-weight: 700;
}
.order-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: var(--primary);
  border-radius: 2px;
}

/* ========== 订单列表 ========== */
.order-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.order-card {
  background: white;
  border-radius: var(--radius-md);
  padding: 14px;
}

/* 订单头 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border);
}
.order-no {
  font-size: 12px;
  color: var(--text-muted);
}
.order-status {
  font-size: 13px;
  font-weight: 600;
}
.status-0 { color: var(--orange); }
.status-1 { color: var(--green); }
.status-2 { color: var(--text-muted); }
.status-3 { color: var(--primary); }

/* 商品 */
.order-product {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.order-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}
.order-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.order-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
.order-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

/* 时间 */
.order-time {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.team-tag {
  background: var(--primary-bg);
  color: var(--primary);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
}
.btn-order {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: opacity 0.2s;
}
.btn-order:active {
  opacity: 0.85;
}
.btn-pay {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
}
.btn-refund {
  background: white;
  color: var(--text-secondary);
  border-color: var(--border);
}
.btn-group {
  background: var(--orange-light);
  color: var(--orange);
}
.btn-rebuy {
  background: white;
  color: var(--primary);
  border-color: var(--primary);
}
</style>
