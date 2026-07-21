<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { lockMarketPayOrder, settlementMarketPayOrder, refundMarketPayOrder } from '@/api/index'
import { ElMessage } from 'element-plus'
import CountdownTimer from '@/components/CountdownTimer.vue'

const router = useRouter()
const route = useRoute()
const currentUserId = inject('currentUserId')

const teamId = route.params.teamId
const query = route.query

// 拼团状态
const groupInfo = ref(null)
const members = ref([])
const loading = ref(false)
const submitting = ref(false)

// 商品池
const goodsPool = {
  '9890001': { title: '新疆阿克苏苹果 5kg 脆甜多汁 产地直发', image: 'https://img.yzcdn.cn/vant/cat.jpeg', payPrice: '29.9', originalPrice: '59.8', targetCount: 3 },
  '9890002': { title: '海南金煌芒果 2.5kg 核小肉厚 香甜软糯', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', payPrice: '35.8', originalPrice: '69.9', targetCount: 2 },
  '9890003': { title: '四川丑橘 5kg 皮薄汁多 酸甜爽口', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', payPrice: '25.8', originalPrice: '49.9', targetCount: 5 },
  '9890004': { title: '云南红宝石车厘子 2kg 当季新鲜水果', image: 'https://img.yzcdn.cn/vant/cat.jpeg', payPrice: '68.0', originalPrice: '128.0', targetCount: 3 },
  '9890005': { title: '福建琯溪蜜柚 4个装 红心柚子 清甜润肺', image: 'https://img.yzcdn.cn/vant/apple-1.jpg', payPrice: '22.9', originalPrice: '45.8', targetCount: 2 },
  '9890006': { title: '赣南脐橙 5kg 橙子 皮薄汁多 维C满满', image: 'https://img.yzcdn.cn/vant/apple-2.jpg', payPrice: '28.8', originalPrice: '55.8', targetCount: 3 },
}

onMounted(() => {
  buildGroupInfo()
})

function buildGroupInfo() {
  const goodsId = query.goodsId || '9890001'
  const pool = goodsPool[goodsId] || goodsPool['9890001']
  const targetCount = pool.targetCount

  // 模拟拼团信息
  const isNew = teamId.startsWith('new-') || teamId.startsWith('mock')
  const completeCount = isNew ? 1 : Math.min(2, targetCount)
  const leaderId = query.userId || 'xfg01'

  groupInfo.value = {
    teamId,
    goodsId,
    ...pool,
    targetCount,
    completeCount,
    leaderId,
    endTime: Date.now() + 1.5 * 3600000,
    status: completeCount >= targetCount ? 'success' : 'ongoing',
    outTradeNo: query.outTradeNo || '',
  }

  // 构建成员列表
  const memberList = []
  // 团长
  memberList.push({
    userId: leaderId,
    name: leaderId,
    avatar: '😀',
    isLeader: true,
    joinTime: '2026-07-21 14:30',
  })
  // 其他成员
  const otherMembers = [
    { userId: 'xfg02', name: 'xfg02', avatar: '🥰' },
    { userId: 'xfg03', name: 'xfg03', avatar: '😎' },
    { userId: 'xfg04', name: 'xfg04', avatar: '🤩' },
    { userId: 'xfg05', name: 'xfg05', avatar: '😄' },
  ]
  for (let i = 0; i < completeCount - 1; i++) {
    if (otherMembers[i]) {
      memberList.push({
        ...otherMembers[i],
        isLeader: false,
        joinTime: '2026-07-21 14:3' + (i + 5),
      })
    }
  }
  members.value = memberList
}

const lackCount = computed(() => {
  if (!groupInfo.value) return 0
  return groupInfo.value.targetCount - groupInfo.value.completeCount
})

const progressPercent = computed(() => {
  if (!groupInfo.value) return 0
  return Math.round((groupInfo.value.completeCount / groupInfo.value.targetCount) * 100)
})

const isLeader = computed(() => {
  return groupInfo.value?.leaderId === currentUserId.value
})

const isMember = computed(() => {
  return members.value.some(m => m.userId === currentUserId.value)
})

// 邀请参团
function handleInvite() {
  ElMessage.success('分享链接已复制，快去邀请好友吧！')
}

// 我要参团
async function handleJoin() {
  submitting.value = true
  try {
    let outTradeNo = ''
    for (let i = 0; i < 12; i++) {
      outTradeNo += Math.floor(Math.random() * 10)
    }

    const payload = {
      userId: currentUserId.value,
      teamId: teamId,
      activityId: 100123,
      goodsId: groupInfo.value.goodsId,
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
      ElMessage.success('参团成功！')
      // 刷新
      groupInfo.value.completeCount++
      members.value.push({
        userId: currentUserId.value,
        name: currentUserId.value,
        avatar: '🙂',
        isLeader: false,
        joinTime: new Date().toLocaleString('zh-CN'),
      })
    } else {
      ElMessage.error(res.info || '参团失败')
    }
  } catch (e) {
    // 模拟成功
    ElMessage.success('参团成功！')
    groupInfo.value.completeCount++
    members.value.push({
      userId: currentUserId.value,
      name: currentUserId.value,
      avatar: '🙂',
      isLeader: false,
      joinTime: new Date().toLocaleString('zh-CN'),
    })
  } finally {
    submitting.value = false
  }
}

// 模拟结算
async function handleSettle() {
  if (!groupInfo.value.outTradeNo) {
    ElMessage.warning('缺少交易单号')
    return
  }
  submitting.value = true
  try {
    const res = await settlementMarketPayOrder({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
      outTradeNo: groupInfo.value.outTradeNo,
      outTradeTime: Date.now(),
    })
    if (res.code === '0000') {
      ElMessage.success('支付成功！')
      groupInfo.value.status = 'success'
    } else {
      ElMessage.error(res.info || '支付失败')
    }
  } catch (e) {
    ElMessage.success('支付成功！')
    groupInfo.value.status = 'success'
  } finally {
    submitting.value = false
  }
}

// 模拟退款
async function handleRefund() {
  if (!groupInfo.value.outTradeNo) {
    ElMessage.warning('缺少交易单号')
    return
  }
  submitting.value = true
  try {
    const res = await refundMarketPayOrder({
      userId: currentUserId.value,
      source: 's01',
      channel: 'c01',
      outTradeNo: groupInfo.value.outTradeNo,
    })
    if (res.code === '0000') {
      ElMessage.success('退款成功！')
      router.replace('/orders')
    } else {
      ElMessage.error(res.info || '退款失败')
    }
  } catch (e) {
    ElMessage.success('退款成功！')
    router.replace('/orders')
  } finally {
    submitting.value = false
  }
}

function goHome() {
  router.push('/home')
}
</script>

<template>
  <div class="group-detail" v-loading="loading">
    <template v-if="groupInfo">
      <!-- 导航栏 -->
      <div class="nav-bar">
        <div class="nav-back" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <span class="nav-title">拼团详情</span>
        <div class="nav-share" @click="handleInvite">
          <el-icon><Share /></el-icon>
        </div>
      </div>

      <!-- 拼团状态横幅 -->
      <div class="status-banner" :class="groupInfo.status">
        <template v-if="groupInfo.status === 'success'">
          <div class="status-icon">🎉</div>
          <div class="status-text">
            <div class="status-title">拼团成功！</div>
            <div class="status-sub">商家将尽快为您发货</div>
          </div>
        </template>
        <template v-else>
          <div class="status-icon">🔥</div>
          <div class="status-text">
            <div class="status-title">拼团进行中</div>
            <div class="status-sub">还差 <strong>{{ lackCount }}</strong> 人，邀请好友来参团吧！</div>
          </div>
        </template>
      </div>

      <!-- 商品信息 -->
      <div class="product-row">
        <div class="product-row-img">
          <img :src="groupInfo.image" />
        </div>
        <div class="product-row-info">
          <p class="product-row-title text-ellipsis-2">{{ groupInfo.title }}</p>
          <div class="product-row-price">
            <span class="price"><span class="price-symbol">¥</span>{{ groupInfo.payPrice }}</span>
            <span class="price-original">¥{{ groupInfo.originalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- 拼团进度 -->
      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">拼团进度</span>
          <span class="progress-count">{{ groupInfo.completeCount }}/{{ groupInfo.targetCount }}人</span>
        </div>
        <el-progress
          :percentage="progressPercent"
          :stroke-width="10"
          :show-text="false"
          color="var(--primary)"
        />
        <!-- 成员头像 -->
        <div class="member-avatars">
          <div
            v-for="(member, idx) in members"
            :key="idx"
            class="member-avatar"
            :class="{ leader: member.isLeader }"
          >
            <span class="avatar-emoji">{{ member.avatar }}</span>
            <span v-if="member.isLeader" class="leader-badge">团长</span>
          </div>
          <!-- 空位 -->
          <div
            v-for="i in lackCount"
            :key="'empty-' + i"
            class="member-avatar empty"
          >
            <span class="avatar-emoji">?</span>
          </div>
        </div>
      </div>

      <!-- 成员列表 -->
      <div class="member-section">
        <div class="member-section-title">参团成员</div>
        <div class="member-list">
          <div v-for="(member, idx) in members" :key="idx" class="member-item">
            <div class="member-left">
              <span class="member-avatar-small">{{ member.avatar }}</span>
              <div>
                <span class="member-name">{{ member.name }}</span>
                <span v-if="member.isLeader" class="tag tag-red">团长</span>
              </div>
            </div>
            <span class="member-time">{{ member.joinTime }}</span>
          </div>
        </div>
      </div>

      <!-- 倒计时 -->
      <div v-if="groupInfo.status === 'ongoing'" class="countdown-section">
        <span class="cd-label">距拼团结束还剩：</span>
        <CountdownTimer :target-time="groupInfo.endTime" format="full" />
      </div>

      <!-- 拼团规则 -->
      <div class="rule-box">
        <div class="rule-title">拼团规则</div>
        <div class="rule-content">
          1. 支付成功后即可开团或参团<br />
          2. 需在24小时内邀请好友参团，达到人数即成团<br />
          3. 成团后商家48小时内发货<br />
          4. 未成团将自动退款至原支付账户
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-bar safe-bottom">
        <button class="bar-home" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </button>

        <template v-if="groupInfo.status === 'success'">
          <button class="bar-btn bar-btn-primary" @click="goHome">继续拼团</button>
        </template>
        <template v-else>
          <button class="bar-btn bar-btn-share" @click="handleInvite">
            <el-icon><Share /></el-icon>
            邀请好友
          </button>
          <button
            v-if="!isMember"
            class="bar-btn bar-btn-primary"
            :disabled="submitting"
            @click="handleJoin"
          >
            {{ submitting ? '处理中...' : '立即参团 ¥' + groupInfo.payPrice }}
          </button>
          <template v-else-if="isLeader">
            <button class="bar-btn bar-btn-share" @click="handleSettle" :disabled="submitting">
              模拟支付
            </button>
            <button class="bar-btn bar-btn-refund" @click="handleRefund" :disabled="submitting">
              退款
            </button>
          </template>
          <button v-else class="bar-btn bar-btn-primary" @click="handleSettle" :disabled="submitting">
            立即支付 ¥{{ groupInfo.payPrice }}
          </button>
        </template>
      </div>
    </template>
  </div>
</template>

<style scoped>
.group-detail {
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

/* ========== 状态横幅 ========== */
.status-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #FF4D4F, #E02020);
  color: white;
}
.status-banner.success {
  background: linear-gradient(135deg, #00B578, #009A63);
}
.status-icon {
  font-size: 40px;
}
.status-title {
  font-size: 20px;
  font-weight: 800;
}
.status-sub {
  font-size: 13px;
  margin-top: 4px;
  opacity: 0.9;
}
.status-sub strong {
  font-weight: 700;
  font-size: 15px;
}

/* ========== 商品行 ========== */
.product-row {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: white;
  margin-top: 8px;
}
.product-row-img {
  width: 70px;
  height: 70px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}
.product-row-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.product-row-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
.product-row-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

/* ========== 进度区 ========== */
.progress-section {
  background: white;
  margin-top: 8px;
  padding: 16px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.progress-title {
  font-size: 15px;
  font-weight: 700;
}
.progress-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.member-avatars {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.member-avatar {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
}
.member-avatar.leader {
  border-color: var(--yellow);
}
.member-avatar.empty {
  background: #F0F0F0;
  border: 2px dashed #DDD;
}
.avatar-emoji {
  font-size: 24px;
}
.leader-badge {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--yellow);
  color: #222;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 6px;
  white-space: nowrap;
}

/* ========== 成员列表 ========== */
.member-section {
  background: white;
  margin-top: 8px;
  padding: 16px;
}
.member-section-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 12px;
}
.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.member-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.member-avatar-small {
  font-size: 28px;
}
.member-name {
  font-size: 14px;
  font-weight: 500;
  margin-right: 6px;
}
.member-time {
  font-size: 12px;
  color: var(--text-muted);
}

/* ========== 倒计时 ========== */
.countdown-section {
  background: white;
  margin-top: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cd-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ========== 规则 ========== */
.rule-box {
  background: white;
  margin-top: 8px;
  padding: 16px;
}
.rule-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
}
.rule-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
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
.bar-home {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 10px;
  cursor: pointer;
  padding: 4px 8px;
}
.bar-home .el-icon {
  font-size: 18px;
}
.bar-btn {
  flex: 1;
  border: none;
  border-radius: 22px;
  padding: 11px 0;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.bar-btn-primary {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
}
.bar-btn-share {
  background: var(--orange-light);
  color: var(--orange);
}
.bar-btn-refund {
  background: #F0F0F0;
  color: var(--text-secondary);
}
.bar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
