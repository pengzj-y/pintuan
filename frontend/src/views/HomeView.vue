<script setup>
import { ref } from 'vue';
import { queryGroupBuyMarketConfig } from '@/api/index';
import { ElMessage } from 'element-plus';

const form = ref({
    userId: 'xfg01',
    source: 's01',
    channel: 'c01',
    goodsId: '9890001',
});

const loading = ref(false);
const result = ref(null);

async function handleQuery() {
    loading.value = true;
    try {
        const res = await queryGroupBuyMarketConfig(form.value);
        if (res.code === '0000') {
            result.value = res.data;
            ElMessage.success('查询成功');
        }
    } finally {
        loading.value = false;
    }
}

function joinTeam(teamId) {
    // Emit event to parent to switch tab and fill teamId
    emit('join-team', teamId);
}

const emit = defineEmits(['join-team']);
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <h2>营销首页</h2>
            <p>查询拼团营销配置，试算营销优惠价格</p>
        </div>

        <el-card shadow="never">
            <template #header>
                <span class="section-title">查询商品营销配置</span>
            </template>
            <el-form :model="form" label-width="100px">
                <el-row :gutter="16">
                    <el-col :span="6">
                        <el-form-item label="商品ID">
                            <el-input v-model="form.goodsId" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="渠道">
                            <el-input v-model="form.source" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="来源">
                            <el-input v-model="form.channel" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="用户ID">
                            <el-input v-model="form.userId" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item>
                    <el-button type="primary" :loading="loading" @click="handleQuery">查询配置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <!-- 查询结果 -->
        <div v-if="result" class="result-section">
            <!-- 价格试算 -->
            <el-card shadow="never" class="price-card">
                <template #header>
                    <div class="card-header">
                        <span class="section-title">试算结果</span>
                        <el-tag>活动 {{ result.activityId }}</el-tag>
                    </div>
                </template>
                <div class="price-comparison">
                    <div class="price-item original">
                        <span class="price-label">原价</span>
                        <span class="price-value">¥{{ result.goods?.originalPrice }}</span>
                    </div>
                    <div class="price-arrow">→</div>
                    <div class="price-item discount">
                        <span class="price-label">优惠</span>
                        <span class="price-value">-¥{{ result.goods?.deductionPrice }}</span>
                    </div>
                    <div class="price-arrow">→</div>
                    <div class="price-item final">
                        <span class="price-label">实付</span>
                        <span class="price-value">¥{{ result.goods?.payPrice }}</span>
                    </div>
                </div>
            </el-card>

            <!-- 组队统计 -->
            <el-row :gutter="16" class="statistics-row">
                <el-col :span="8">
                    <el-card shadow="never" class="stat-card">
                        <div class="stat-content">
                            <span class="stat-num">{{ result.teamStatistic?.allTeamCount || 0 }}</span>
                            <span class="stat-label">开团队伍</span>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="8">
                    <el-card shadow="never" class="stat-card">
                        <div class="stat-content">
                            <span class="stat-num">{{ result.teamStatistic?.allTeamCompleteCount || 0 }}</span>
                            <span class="stat-label">成团队伍</span>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="8">
                    <el-card shadow="never" class="stat-card">
                        <div class="stat-content">
                            <span class="stat-num">{{ result.teamStatistic?.allTeamUserCount || 0 }}</span>
                            <span class="stat-label">参团人数</span>
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <!-- 拼团队伍列表 -->
            <el-card shadow="never">
                <template #header>
                    <span class="section-title">正在拼团的队伍</span>
                </template>
                <div v-if="result.teamList && result.teamList.length > 0">
                    <div v-for="team in result.teamList" :key="team.teamId" class="team-card">
                        <div class="team-info">
                            <div class="team-field">
                                <label>组队ID:</label>
                                <span>{{ team.teamId }}</span>
                            </div>
                            <div class="team-field">
                                <label>发起人:</label>
                                <span>{{ team.userId }}</span>
                            </div>
                            <div class="team-field">
                                <label>进度:</label>
                                <el-progress
                                    :percentage="Math.round((team.completeCount / team.targetCount) * 100)"
                                    :format="() => `${team.completeCount}/${team.targetCount}`"
                                    style="flex: 1; min-width: 120px;"
                                />
                            </div>
                            <div class="team-field">
                                <label>倒计时:</label>
                                <span class="countdown">{{ team.validTimeCountdown }}</span>
                            </div>
                        </div>
                        <el-button type="primary" size="small" @click="joinTeam(team.teamId)">参团</el-button>
                    </div>
                </div>
                <el-empty v-else description="暂无正在拼团的队伍，快去开团吧！" />
            </el-card>
        </div>
    </div>
</template>

<style scoped>
.price-card {
    margin-top: 16px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.price-comparison {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-radius: 8px;
}

.price-item {
    text-align: center;
    flex: 1;
}

.price-label {
    display: block;
    font-size: 13px;
    color: #64748b;
    margin-bottom: 4px;
}

.price-value {
    font-size: 28px;
    font-weight: 700;
}

.price-item.original .price-value {
    color: #94a3b8;
    text-decoration: line-through;
}

.price-item.discount .price-value {
    color: #ef4444;
}

.price-item.final .price-value {
    color: #10b981;
    font-size: 32px;
}

.price-arrow {
    font-size: 24px;
    color: #94a3b8;
}

.statistics-row {
    margin-top: 16px;
}

.stat-card :deep(.el-card__body) {
    padding: 20px;
}

.stat-content {
    text-align: center;
}

.stat-num {
    display: block;
    font-size: 32px;
    font-weight: 700;
    color: #4f46e5;
}

.stat-label {
    font-size: 13px;
    color: #64748b;
}

.team-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    margin-bottom: 12px;
    transition: box-shadow 0.2s;
}

.team-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.team-info {
    display: flex;
    gap: 24px;
    align-items: center;
    flex-wrap: wrap;
}

.team-field {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
}

.team-field label {
    color: #64748b;
}

.countdown {
    color: #f59e0b;
    font-weight: 500;
}

.result-section {
    margin-top: 16px;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(8px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
