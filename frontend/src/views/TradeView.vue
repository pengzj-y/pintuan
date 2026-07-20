<script setup>
import { ref, onMounted } from 'vue';
import { lockMarketPayOrder, settlementMarketPayOrder, refundMarketPayOrder } from '@/api/index';
import { ElMessage } from 'element-plus';

const props = defineProps({
    currentUserId: { type: String, default: 'xfg01' }
});

// 锁单表单
const lockForm = ref({
    userId: props.currentUserId,
    teamId: '',
    activityId: 100123,
    goodsId: '9890001',
    source: 's01',
    channel: 'c01',
    outTradeNo: '',
    notifyType: 'HTTP',
    notifyUrl: 'http://127.0.0.1:8091/api/v1/test/group_buy_notify',
});

// 结算表单
const settleForm = ref({
    userId: props.currentUserId,
    source: 's01',
    channel: 'c01',
    outTradeNo: '',
    outTradeTime: null,
});

// 退款表单
const refundForm = ref({
    userId: props.currentUserId,
    source: 's01',
    channel: 'c01',
    outTradeNo: '',
});

const lockResult = ref(null);
const settleResult = ref(null);
const refundResult = ref(null);
const loading = ref(false);

// 生成随机交易单号
function refreshOutTradeNo() {
    let result = '';
    for (let i = 0; i < 12; i++) {
        result += Math.floor(Math.random() * 10);
    }
    lockForm.value.outTradeNo = result;
    ElMessage.success('已生成新的交易单号');
}

onMounted(() => {
    refreshOutTradeNo();
});

// 监听来自首页的参团事件
function setTeamId(teamId) {
    lockForm.value.teamId = teamId;
}

defineExpose({ setTeamId });

async function handleLock() {
    if (!lockForm.value.outTradeNo) {
        ElMessage.warning('请生成交易单号');
        return;
    }
    loading.value = true;
    try {
        const payload = {
            ...lockForm.value,
            activityId: Number(lockForm.value.activityId),
            teamId: lockForm.value.teamId || null,
            notifyConfigVO: {
                notifyType: lockForm.value.notifyType,
                notifyUrl: lockForm.value.notifyType === 'HTTP' ? lockForm.value.notifyUrl : null,
                notifyMQ: lockForm.value.notifyType === 'MQ' ? 'topic.team_success' : null,
            }
        };
        const res = await lockMarketPayOrder(payload);
        lockResult.value = res;
        if (res.code === '0000') {
            ElMessage.success('锁单成功！');
            settleForm.value.outTradeNo = res.data.outTradeNo;
            refundForm.value.outTradeNo = res.data.outTradeNo;
        } else {
            ElMessage.error(res.info || '锁单失败');
        }
    } finally {
        loading.value = false;
    }
}

async function handleSettle() {
    if (!settleForm.value.outTradeNo) {
        ElMessage.warning('请填写交易单号');
        return;
    }
    loading.value = true;
    try {
        const res = await settlementMarketPayOrder({
            ...settleForm.value,
            outTradeTime: Date.now(),
        });
        settleResult.value = res;
        if (res.code === '0000') {
            ElMessage.success('结算成功！');
        } else {
            ElMessage.error(res.info || '结算失败');
        }
    } finally {
        loading.value = false;
    }
}

async function handleRefund() {
    if (!refundForm.value.outTradeNo) {
        ElMessage.warning('请填写交易单号');
        return;
    }
    loading.value = true;
    try {
        const res = await refundMarketPayOrder(refundForm.value);
        refundResult.value = res;
        if (res.code === '0000') {
            ElMessage.success('退款成功！');
        } else {
            ElMessage.error(res.info || '退款失败');
        }
    } finally {
        loading.value = false;
    }
}
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <h2>交易下单</h2>
            <p>锁单、结算、退款全流程</p>
        </div>

        <!-- 锁单 -->
        <el-card shadow="never">
            <template #header>
                <span class="section-title">锁单（创建订单）</span>
            </template>
            <el-form label-width="120px">
                <el-row :gutter="16">
                    <el-col :span="6">
                        <el-form-item label="活动ID">
                            <el-input v-model="lockForm.activityId" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="商品ID">
                            <el-input v-model="lockForm.goodsId" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="渠道">
                            <el-input v-model="lockForm.source" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="来源">
                            <el-input v-model="lockForm.channel" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="6">
                        <el-form-item label="组队ID">
                            <el-input v-model="lockForm.teamId" placeholder="为空则创建新队伍" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="外部交易单号">
                            <el-input v-model="lockForm.outTradeNo" readonly />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="回调方式">
                            <el-select v-model="lockForm.notifyType" style="width: 100%">
                                <el-option label="HTTP 回调" value="HTTP" />
                                <el-option label="MQ 消息" value="MQ" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6" v-if="lockForm.notifyType === 'HTTP'">
                        <el-form-item label="回调地址">
                            <el-input v-model="lockForm.notifyUrl" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item>
                    <el-button type="primary" :loading="loading" @click="handleLock">锁单</el-button>
                    <el-button @click="refreshOutTradeNo">刷新交易单号</el-button>
                </el-form-item>
            </el-form>

            <div v-if="lockResult" :class="['result-box', lockResult.code === '0000' ? 'success' : 'error']">
                {{ JSON.stringify(lockResult, null, 2) }}
            </div>
        </el-card>

        <!-- 结算 -->
        <el-card shadow="never" style="margin-top: 16px;">
            <template #header>
                <span class="section-title">结算（支付完成）</span>
            </template>
            <el-form label-width="120px">
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="外部交易单号">
                            <el-input v-model="settleForm.outTradeNo" placeholder="填入锁单返回的单号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="渠道">
                            <el-input v-model="settleForm.source" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="来源">
                            <el-input v-model="settleForm.channel" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item>
                    <el-button type="success" :loading="loading" @click="handleSettle">结算</el-button>
                </el-form-item>
            </el-form>

            <div v-if="settleResult" :class="['result-box', settleResult.code === '0000' ? 'success' : 'error']">
                {{ JSON.stringify(settleResult, null, 2) }}
            </div>
        </el-card>

        <!-- 退款 -->
        <el-card shadow="never" style="margin-top: 16px;">
            <template #header>
                <span class="section-title">退款</span>
            </template>
            <el-form label-width="120px">
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="外部交易单号">
                            <el-input v-model="refundForm.outTradeNo" placeholder="填入要退款的单号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="渠道">
                            <el-input v-model="refundForm.source" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="来源">
                            <el-input v-model="refundForm.channel" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item>
                    <el-button type="danger" :loading="loading" @click="handleRefund">退款</el-button>
                </el-form-item>
            </el-form>

            <div v-if="refundResult" :class="['result-box', refundResult.code === '0000' ? 'success' : 'error']">
                {{ JSON.stringify(refundResult, null, 2) }}
            </div>
        </el-card>
    </div>
</template>

<style scoped>
.result-box {
    margin-top: 16px;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    font-family: 'Monaco', 'Consolas', monospace;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 300px;
    overflow-y: auto;
}

.result-box.success {
    border-left: 4px solid #10b981;
}

.result-box.error {
    border-left: 4px solid #ef4444;
}
</style>
