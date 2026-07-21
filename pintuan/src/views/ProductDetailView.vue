<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { queryGroupBuyMarketConfig, lockMarketPayOrder } from '@/api/index'
import { ElMessage, ElDialog } from 'element-plus'
import CountdownTimer from '@/components/CountdownTimer.vue'
import GroupCard from '@/components/GroupCard.vue'

const router = useRouter()
const route = useRoute()
const currentUserId = inject('currentUserId')

const goodsId = route.params.goodsId

// 商品信息
const product = ref(null)
const teamList = ref([])
const loading = ref(false)
const activityId = ref(100123)

// 开团/参团弹窗
const showActionSheet = ref(false)
const actionType = ref('') // 'open' | 'join'
const joiningGroup = ref(null)
const submitting = ref(false)

// 商品池（与首页一致）
const goodsPool = {
  '9890001': { title: '新疆阿克苏苹果 5kg 脆甜多汁 产地直发', image: 'https://img.yzcdn.cn/vant/cat.jpeg', tag: '热卖', sales: 2380, targetCount: 3, originalPrice: '59.8', payPrice: '29.9', deductionPrice: '29.9', desc: '新疆阿克苏冰糖心苹果，海拔1500米日照，昼夜温差大，糖心率高。5kg约12-14个，家庭装实惠。' },
  '9890002': { title: '海南金煌芒果 2.5kg 核小肉厚 香甜软糯', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', tag: '新品', sales: 856, targetCount: 2, originalPrice: '69.9', payPrice: '35.8', deductionPrice: '34.1', desc: '海南金煌芒，单果500g+，核薄肉厚，无纤维感，香甜如蜜。' },
  '9890003': { title: '四川丑橘 5kg 皮薄汁多 酸甜爽口', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', tag: '', sales: 1240, targetCount: 5, originalPrice: '49.9', payPrice: '25.8', deductionPrice: '24.1', desc: '四川蒲江丑橘，又名"不知火"，易剥皮，果肉脆嫩，酸甜比极佳。' },
  '9890004': { title: '云南红宝石车厘子 2kg 当季新鲜水果', image: 'https://img.yzcdn.cn/vant/cat.jpeg', tag: '限时', sales: 568, targetCount: 3, originalPrice: '128.0', payPrice: '68.0', deductionPrice: '60.0', desc: '云南高原车厘子，红宝石般色泽，果肉紧实，甜度高。' },
  '9890005': { title: '福建琯溪蜜柚 4个装 红心柚子 清甜润肺', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', tag: '', sales: 342, targetCount: 2, originalPrice: '45.8', payPrice: '22.9', deductionPrice: '22.9', desc: '福建平和琯溪红心蜜柚，30年老树果，果肉细嫩，清甜微酸。' },
  '9890006': { title: '赣南脐橙 5kg 橙子 皮薄汁多 维C满满', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', tag: '热卖', sales: 1890, targetCount: 3, originalPrice: '55.8', payPrice: '28.8', deductionPrice: '27.0', desc: '赣南核心产区脐橙，17°黄金糖酸比，榨汁率70%+。' },
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await queryGroupBuyMarketConfig({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
      goodsId,
    })
    if (res.code === '0000') {
      const data = res.data
      activityId.value = data.activityId || 100123

      // 构建商品信息
      const pool = goodsPool[goodsId] || { title: '商品', image: 'https://img.yzcdn.cn/vant/cat.jpeg', targetCount: 3, originalPrice: '99.0', payPrice: '49.0', deductionPrice: '50.0', desc: '' }
      product.value = {
        goodsId,
        ...pool,
        originalPrice: data.goods?.originalPrice || pool.originalPrice,
        payPrice: data.goods?.payPrice || pool.payPrice,
        deductionPrice: data.goods?.deductionPrice || pool.deductionPrice,
        activityId: activityId.value,
      }

      // 队伍列表
      if (data.teamList && data.teamList.length > 0) {
        teamList.value = data.teamList.map(t => ({
          ...t,
          leaderName: t.userId,
          leaderAvatar: null,
          endTime: t.validTimeCountdown
            ? Date.now() + parseCountdown(t.validTimeCountdown)
            : Date.now() + 2 * 3600000,
        }))
      } else {
        // 没有队伍时，给一个演示队伍
        teamList.value = [{
          teamId: 'demo-team-' + goodsId,
          userId: 'xfg02',
          leaderName: 'xfg02',
          leaderAvatar: null,
          completeCount: 1,
          targetCount: pool.targetCount || 3,
          endTime: Date.now() + 1.5 * 3600000,
        }]
      }
    }
  } catch (e) {
    // 接口不可用时使用本地数据
    const pool = goodsPool[goodsId]
    if (pool) {
      product.value = { goodsId, ...pool, activityId: 100123 }
      teamList.value = [{
        teamId: 'demo-team-' + goodsId,
        userId: 'xfg02',
        leaderName: 'xfg02',
        leaderAvatar: null,
        completeCount: 1,
        targetCount: pool.targetCount || 3,
        endTime: Date.now() + 1.5 * 3600000,
      }]
    } else {
      ElMessage.error('商品不存在')
      router.back()
    }
  } finally {
    loading.value = false
  }
})

// 解析倒计时字符串 "02:30:00" -> ms
function parseCountdown(str) {
  if (!str) return 2 * 3600000
  const parts = str.split(':').map(Number)
  if (parts.length === 3) {
    return ((parts[0] * 60 + parts[1]) * 60 + parts[2]) * 1000
  }
  return 2 * 3600000
}

const discountPercent = computed(() => {
  if (!product.value) return 0
  const orig = parseFloat(product.value.originalPrice)
  const pay = parseFloat(product.value.payPrice)
  if (!orig) return 0
  return Math.round((pay / orig) * 10)
})

// 开团
function handleOpenGroup() {
  actionType.value = 'open'
  joiningGroup.value = null
  showActionSheet.value = true
}

// 参团
function handleJoinGroup(group) {
  actionType.value = 'join'
  joiningGroup.value = group
  showActionSheet.value = true
}

// 确认开团/参团
async function confirmAction() {
  submitting.value = true
  try {
    // 生成交易单号
    let outTradeNo = ''
    for (let i = 0; i < 12; i++) {
      outTradeNo += Math.floor(Math.random() * 10)
    }

    const payload = {
      userId: currentUserId.value,
      teamId: actionType.value === 'join' ? joiningGroup.value?.teamId : null,
      activityId: Number(activityId.value),
      goodsId,
      source: 's01',
      channel: 'c01',
      outTradeNo,
      notifyType: 'HTTP',
      notifyUrl: 'http://127.0.0.1:8091/api/v1/test/group_buy_notify',
      notifyConfigVO: {
        notifyType: 'HTTP',
        notifyUrl: 'http://127.0.0.1:8091/api/v1/test/group_buy_notify',
      },
    }

    const res = await lockMarketPayOrder(payload)
    if (res.code === '0000') {
      const msg = actionType.value === 'join' ? '参团成功！' : '开团成功！'
      ElMessage.success(msg)
      showActionSheet.value = false
      // 跳转到拼团详情
      const teamId = res.data?.teamId || payload.teamId || 'new-' + outTradeNo
      router.replace(`/group/${teamId}?goodsId=${goodsId}&outTradeNo=${res.data?.outTradeNo || outTradeNo}&userId=${currentUserId.value}`)
    } else {
      ElMessage.error(res.info || '操作失败')
    }
  } catch (e) {
    // 接口不可用也模拟成功
    const teamId = actionType.value === 'join' ? joiningGroup.value?.teamId : 'new-' + Date.now()
    const outTradeNo = 'mock' + Date.now()
    ElMessage.success(actionType.value === 'join' ? '参团成功！' : '开团成功！')
    showActionSheet.value = false
    router.replace(`/group/${teamId}?goodsId=${goodsId}&outTradeNo=${outTradeNo}&userId=${currentUserId.value}`)
  } finally {
    submitting.value = false
  }
}

function goGroup(group) {
  router.push(`/group/${group.teamId}?goodsId=${goodsId}`)
}
</script>

<template>
  <div class="product-detail" v-loading="loading">
    <template v-if="product">
      <!-- 返回按钮 -->
      <div class="nav-bar">
        <div class="nav-back" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <span class="nav-title">商品详情</span>
        <div class="nav-share">
          <el-icon><Share /></el-icon>
        </div>
      </div>

      <!-- 商品图 -->
      <div class="product-image">
        <img :src="product.image" :alt="product.title" />
        <span v-if="product.tag" class="img-tag">{{ product.tag }}</span>
      </div>

      <!-- 价格区 -->
      <div class="price-section">
        <div class="price-row">
          <div class="pintuan-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ product.payPrice }}</span>
            <span class="price-label">拼团价</span>
          </div>
          <div class="discount-badge">{{ discountPercent }}折</div>
        </div>
        <div class="original-row">
          <span class="price-original">原价 ¥{{ product.originalPrice }}</span>
          <span class="sales-count">{{ product.sales }}人已拼</span>
        </div>
      </div>

      <!-- 商品标题 -->
      <div class="title-section">
        <h1 class="product-title-main">{{ product.title }}</h1>
        <p v-if="product.desc" class="product-desc">{{ product.desc }}</p>
      </div>

      <!-- 拼团规则 -->
      <div class="rule-section">
        <div class="rule-item">
          <el-icon class="rule-icon"><User /></el-icon>
          <span>需 {{ product.targetCount }} 人成团</span>
        </div>
        <div class="rule-item">
          <el-icon class="rule-icon"><Clock /></el-icon>
          <span>24小时内未成团自动退款</span>
        </div>
        <div class="rule-item">
          <el-icon class="rule-icon"><Van /></el-icon>
          <span>成团后48小时内发货</span>
        </div>
      </div>

      <!-- 正在拼团 -->
      <div class="group-section">
        <div class="group-section-header">
          <span class="group-section-title">🔥 正在拼团 ({{ teamList.length }})</span>
        </div>
        <div v-if="teamList.length > 0" class="group-list">
          <GroupCard
            v-for="group in teamList"
            :key="group.teamId"
            :group="group"
            @click="goGroup"
            @join="handleJoinGroup"
          />
        </div>
        <el-empty v-else description="暂无进行中的队伍，快来开团吧！" :image-size="80" />
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-bar safe-bottom">
        <div class="bar-price">
          <span class="bar-price-symbol">¥</span>
          <span class="bar-price-value">{{ product.payPrice }}</span>
          <span class="bar-price-label">拼团价</span>
        </div>
        <button class="bar-btn bar-btn-open" @click="handleOpenGroup">
          <span class="btn-title">单独购买</span>
          <span class="btn-sub">¥{{ product.originalPrice }}</span>
        </button>
        <button class="bar-btn bar-btn-pintuan" @click="handleOpenGroup">
          <span class="btn-title">开团拼团</span>
          <span class="btn-sub">¥{{ product.payPrice }} 省¥{{ product.deductionPrice }}</span>
        </button>
      </div>

      <!-- 开团/参团弹窗 -->
      <el-dialog
        v-model="showActionSheet"
        :title="actionType === 'join' ? '确认参团' : '确认开团'"
        width="88%"
        align-center
        :close-on-click-modal="false"
      >
        <div class="confirm-dialog">
          <div class="confirm-img">
            <img :src="product.image" />
          </div>
          <div class="confirm-info">
            <p class="confirm-title text-ellipsis-2">{{ product.title }}</p>
            <div class="confirm-price">
              <span class="price"><span class="price-symbol">¥</span>{{ product.payPrice }}</span>
              <span class="price-original">¥{{ product.originalPrice }}</span>
            </div>
            <p v-if="actionType === 'join' && joiningGroup" class="confirm-tip">
              参团 <strong>{{ joiningGroup.leaderName }}</strong> 的队伍，
              还差 <strong class="lack">{{ joiningGroup.targetCount - joiningGroup.completeCount }}</strong> 人成团
            </p>
            <p v-else class="confirm-tip">
              开一个新团，邀请好友参团，{{ product.targetCount }}人即成团
            </p>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <button class="btn-outline btn-lg" @click="showActionSheet = false">取消</button>
            <button class="btn-primary btn-lg" :disabled="submitting" @click="confirmAction">
              {{ submitting ? '处理中...' : '确认支付 ¥' + product.payPrice }}
            </button>
          </div>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<style scoped>
.product-detail {
  padding-bottom: 70px;
}

/* ========== 导航栏 ========== */
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: white;
  border-bottom: 1px solid var(--border);
}
.nav-back,
.nav-share {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
}
.nav-title {
  font-size: 16px;
  font-weight: 600;
}

/* ========== 商品图 ========== */
.product-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: #f8f8f8;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.img-tag {
  position: absolute;
  top: 12px;
  left: 0;
  background: var(--primary);
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 0 12px 12px 0;
  font-weight: 500;
}

/* ========== 价格区 ========== */
.price-section {
  background: linear-gradient(135deg, #FF4D4F, #E02020);
  padding: 14px 16px;
  color: white;
}
.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.pintuan-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.price-symbol {
  font-size: 16px;
  font-weight: 700;
}
.price-value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}
.price-label {
  font-size: 12px;
  background: rgba(255,255,255,0.25);
  padding: 2px 6px;
  border-radius: 3px;
  margin-left: 4px;
}
.discount-badge {
  background: var(--yellow);
  color: #222;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
}
.original-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255,255,255,0.8);
}
.sales-count {
  margin-left: auto;
}

/* ========== 标题区 ========== */
.title-section {
  background: white;
  padding: 14px 16px;
}
.product-title-main {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text);
}
.product-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 8px;
  line-height: 1.5;
}

/* ========== 规则区 ========== */
.rule-section {
  background: white;
  margin-top: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.rule-icon {
  color: var(--primary);
  font-size: 15px;
}

/* ========== 拼团列表 ========== */
.group-section {
  background: white;
  margin-top: 8px;
  padding: 14px 16px;
}
.group-section-header {
  margin-bottom: 12px;
}
.group-section-title {
  font-size: 15px;
  font-weight: 700;
}
.group-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ========== 底部操作栏 ========== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background: white;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 8px 12px;
  z-index: 100;
  gap: 8px;
}
.bar-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  flex-shrink: 0;
}
.bar-price-symbol {
  font-size: 12px;
  color: var(--price);
  font-weight: 700;
}
.bar-price-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--price);
}
.bar-price-label {
  font-size: 10px;
  color: var(--price);
  margin-left: 2px;
}
.bar-btn {
  flex: 1;
  border: none;
  border-radius: 22px;
  padding: 8px 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}
.btn-title {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}
.btn-sub {
  font-size: 10px;
  opacity: 0.9;
  line-height: 1.2;
}
.bar-btn-open {
  background: var(--orange-light);
  color: var(--orange);
}
.bar-btn-pintuan {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
  flex: 1.4;
}

/* ========== 弹窗 ========== */
.confirm-dialog {
  display: flex;
  gap: 12px;
}
.confirm-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}
.confirm-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.confirm-info {
  flex: 1;
  min-width: 0;
}
.confirm-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
.confirm-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 8px;
}
.confirm-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}
.lack {
  color: var(--primary);
}
.dialog-footer {
  display: flex;
  gap: 10px;
}
.btn-lg {
  flex: 1;
  padding: 10px 0;
  font-size: 14px;
  border-radius: 22px;
}
</style>
