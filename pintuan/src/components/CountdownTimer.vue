<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  // 目标时间（时间戳 ms 或 ISO 字符串）
  targetTime: { type: [Number, String], required: true },
  // 格式：full 显示天时分秒，short 只显示时分秒
  format: { type: String, default: 'short' },
})

const emit = defineEmits(['finish'])

const now = ref(Date.now())
let timer = null

const diff = computed(() => {
  const target = typeof props.targetTime === 'number'
    ? props.targetTime
    : new Date(props.targetTime).getTime()
  return Math.max(0, target - now.value)
})

const days = computed(() => Math.floor(diff.value / 86400000))
const hours = computed(() => Math.floor((diff.value % 86400000) / 3600000))
const minutes = computed(() => Math.floor((diff.value % 3600000) / 60000))
const seconds = computed(() => Math.floor((diff.value % 60000) / 1000))

function pad(n) {
  return String(n).padStart(2, '0')
}

onMounted(() => {
  timer = setInterval(() => {
    now.value = Date.now()
    if (diff.value <= 0) {
      clearInterval(timer)
      emit('finish')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="countdown">
    <template v-if="format === 'full' && days > 0">
      <span class="cd-block">{{ pad(days) }}</span>
      <span class="cd-sep">天</span>
    </template>
    <span class="cd-block">{{ pad(hours) }}</span>
    <span class="cd-sep">:</span>
    <span class="cd-block">{{ pad(minutes) }}</span>
    <span class="cd-sep">:</span>
    <span class="cd-block">{{ pad(seconds) }}</span>
  </div>
</template>

<style scoped>
.countdown {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
}

.cd-block {
  background: #222;
  color: white;
  padding: 1px 4px;
  border-radius: 3px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.cd-sep {
  color: #222;
  font-weight: 700;
  margin: 0 1px;
}
</style>
