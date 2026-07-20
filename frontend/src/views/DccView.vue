<script setup>
import { ref } from 'vue';
import { updateConfig } from '@/api/index';
import { ElMessage } from 'element-plus';

const configs = ref([
    {
        key: 'downgradeSwitch',
        label: '降级开关',
        desc: '控制营销活动的整体降级，1=降级，0=正常',
        value: '',
        placeholder: '0 或 1',
    },
    {
        key: 'cutRange',
        label: '切量范围',
        desc: '控制人群标签的切量范围',
        value: '',
        placeholder: '0-100',
    },
    {
        key: 'rateLimiterSwitch',
        label: '限流开关',
        desc: '控制接口限流开关',
        value: '',
        placeholder: 'open 或 close',
    },
]);

const result = ref(null);
const loading = ref(false);

async function handleUpdate(item) {
    if (!item.value.trim()) {
        ElMessage.warning('请输入配置值');
        return;
    }
    loading.value = true;
    try {
        const res = await updateConfig(item.key, item.value.trim());
        result.value = res;
        if (res.code === '0000') {
            ElMessage.success(`配置 ${item.label} 更新成功`);
            item.value = '';
        } else {
            ElMessage.error(res.info || '配置更新失败');
        }
    } finally {
        loading.value = false;
    }
}
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <h2>DCC 动态配置</h2>
            <p>运行时动态调整系统配置（降级开关、限流等）</p>
        </div>

        <el-card shadow="never">
            <template #header>
                <span class="section-title">配置项管理</span>
            </template>

            <div class="dcc-grid">
                <div v-for="item in configs" :key="item.key" class="dcc-item">
                    <div class="dcc-info">
                        <label>{{ item.label }}</label>
                        <span class="dcc-key">({{ item.key }})</span>
                        <p class="dcc-desc">{{ item.desc }}</p>
                    </div>
                    <div class="dcc-control">
                        <el-input v-model="item.value" :placeholder="item.placeholder" style="flex: 1;" />
                        <el-button type="primary" :loading="loading" @click="handleUpdate(item)">更新</el-button>
                    </div>
                </div>
            </div>

            <div v-if="result" :class="['result-box', result.code === '0000' ? 'success' : 'error']">
                {{ JSON.stringify(result, null, 2) }}
            </div>
        </el-card>
    </div>
</template>

<style scoped>
.dcc-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 20px;
}

.dcc-item {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 20px;
}

.dcc-info label {
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
}

.dcc-key {
    font-size: 12px;
    color: #94a3b8;
    margin-left: 6px;
}

.dcc-desc {
    font-size: 13px;
    color: #64748b;
    margin: 8px 0 12px;
    line-height: 1.5;
}

.dcc-control {
    display: flex;
    gap: 8px;
}

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
