<script setup>
import { ref } from 'vue';
import { groupBuyNotify } from '@/api/index';
import { ElMessage } from 'element-plus';

const form = ref({
    teamId: '',
    outTradeNos: '',
});

const result = ref(null);
const logs = ref([]);
const loading = ref(false);

async function handleNotify() {
    if (!form.value.teamId || !form.value.outTradeNos) {
        ElMessage.warning('请填写完整的回调参数');
        return;
    }

    const payload = {
        teamId: form.value.teamId,
        outTradeNoList: form.value.outTradeNos.split(',').map(s => s.trim()).filter(Boolean),
    };

    loading.value = true;
    try {
        const res = await groupBuyNotify(payload);
        result.value = res;
        ElMessage.success('回调发送成功');

        // 添加日志
        logs.value.unshift({
            time: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
            teamId: payload.teamId,
            outTradeNoList: payload.outTradeNoList,
            response: res,
        });
    } catch (error) {
        ElMessage.error('回调发送失败');
        logs.value.unshift({
            time: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
            teamId: payload.teamId,
            outTradeNoList: payload.outTradeNoList,
            response: '失败: ' + error.message,
            isError: true,
        });
    } finally {
        loading.value = false;
    }
}
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <h2>模拟第三方回调</h2>
            <p>模拟拼团服务端接收第三方回调通知</p>
        </div>

        <el-card shadow="never">
            <template #header>
                <span class="section-title">回调参数</span>
            </template>
            <el-form label-width="120px">
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="组队ID">
                            <el-input v-model="form.teamId" placeholder="队伍ID" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="交易单号">
                            <el-input v-model="form.outTradeNos" placeholder="多个用逗号分隔" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item>
                            <el-button type="primary" :loading="loading" @click="handleNotify">发送回调</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>

            <div v-if="result" :class="['result-box', 'success']">
                {{ JSON.stringify(result, null, 2) }}
            </div>
        </el-card>

        <el-card shadow="never" style="margin-top: 16px;">
            <template #header>
                <span class="section-title">回调日志</span>
            </template>

            <div class="log-panel">
                <div v-if="logs.length === 0" class="log-empty">等待回调日志...</div>
                <div
                    v-for="(log, index) in logs"
                    :key="index"
                    :class="['log-entry', log.isError ? 'log-error' : 'log-success']"
                >
                    <span class="log-time">[{{ log.time }}]</span>
                    <span>回调接收 | 组队ID: {{ log.teamId }} | 单号: [{{ log.outTradeNoList.join(', ') }}] | 响应: {{ log.response }}</span>
                </div>
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
    border-left: 4px solid #10b981;
    font-family: 'Monaco', 'Consolas', monospace;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-all;
}

.log-panel {
    background: #1e293b;
    border-radius: 8px;
    padding: 16px;
    max-height: 300px;
    overflow-y: auto;
    font-family: 'Monaco', 'Consolas', monospace;
    font-size: 12px;
    line-height: 1.8;
}

.log-empty {
    color: #64748b;
    text-align: center;
    padding: 20px;
}

.log-entry {
    padding: 4px 0;
    border-bottom: 1px solid #334155;
}

.log-entry:last-child {
    border-bottom: none;
}

.log-time {
    color: #94a3b8;
    margin-right: 8px;
}

.log-success {
    color: #4ade80;
}

.log-error {
    color: #f87171;
}
</style>
