<script setup>
import { ref } from 'vue';

// 本地订单记录
const orders = ref([]);

// 添加订单（由 TradeView 调用）
function addOrder(order) {
    orders.value.unshift(order);
}

// 订单状态映射
const statusMap = {
    0: { text: '待支付', type: 'warning' },
    1: { text: '已结算', type: 'success' },
    2: { text: '已退款', type: 'danger' },
};

function fillSettle(outTradeNo) {
    emit('fill-settle', outTradeNo);
}

function fillRefund(outTradeNo) {
    emit('fill-refund', outTradeNo);
}

const emit = defineEmits(['fill-settle', 'fill-refund']);

defineExpose({ addOrder });
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <h2>订单管理</h2>
            <p>已创建的订单记录</p>
        </div>

        <el-card shadow="never">
            <template #header>
                <span class="section-title">订单列表</span>
            </template>

            <el-table :data="orders" style="width: 100%" stripe>
                <el-table-column prop="outTradeNo" label="外部交易单号" width="140" />
                <el-table-column prop="activityId" label="活动ID" width="100" />
                <el-table-column label="原价" width="100">
                    <template #default="{ row }">¥{{ row.originalPrice }}</template>
                </el-table-column>
                <el-table-column label="优惠" width="100">
                    <template #default="{ row }">-¥{{ row.deductionPrice }}</template>
                </el-table-column>
                <el-table-column label="实付" width="100">
                    <template #default="{ row }">
                        <strong>¥{{ row.payPrice }}</strong>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
                            {{ statusMap[row.status]?.text || '未知' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="teamId" label="组队ID" width="120">
                    <template #default="{ row }">{{ row.teamId || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                    <template #default="{ row }">
                        <el-button type="success" size="small" @click="fillSettle(row.outTradeNo)">结算</el-button>
                        <el-button type="danger" size="small" @click="fillRefund(row.outTradeNo)">退款</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="orders.length === 0" description="暂无订单，请先锁单" />
        </el-card>
    </div>
</template>
