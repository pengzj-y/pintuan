<script setup>
import CountdownTimer from './CountdownTimer.vue'

defineProps({
  group: { type: Object, required: true },
})

const emit = defineEmits(['click', 'join'])
</script>

<template>
  <div class="group-card" @click="emit('click', group)">
    <!-- 团长头像 -->
    <div class="leader-avatar">
      <img v-if="group.leaderAvatar" :src="group.leaderAvatar" />
      <span v-else class="avatar-placeholder">{{ group.leaderName?.[0] || '团' }}</span>
    </div>

    <!-- 队伍信息 -->
    <div class="group-info">
      <div class="group-leader">
        <span class="leader-name">{{ group.leaderName || group.userId || '团长' }}</span>
        <span class="group-label">的团</span>
      </div>

      <!-- 进度条 -->
      <div class="group-progress-row">
        <el-progress
          :percentage="Math.round((group.completeCount / group.targetCount) * 100)"
          :stroke-width="6"
          :show-text="false"
          color="var(--primary)"
          class="progress"
        />
        <span class="progress-text">{{ group.completeCount }}/{{ group.targetCount }}人</span>
      </div>

      <!-- 倒计时 + 还差 -->
      <div class="group-status">
        <span class="status-label">还差 <strong class="lack">{{ group.targetCount - group.completeCount }}</strong> 人</span>
        <CountdownTimer :target-time="group.validTime || group.endTime" format="short" />
      </div>
    </div>

    <!-- 参团按钮 -->
    <button class="btn-join" @click.stop="emit('join', group)">去参团</button>
  </div>
</template>

<style scoped>
.group-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform 0.15s;
}
.group-card:active {
  transform: scale(0.98);
}

/* 头像 */
.leader-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  display: flex;
  align-items: center;
  justify-content: center;
}
.leader-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  color: white;
  font-size: 16px;
  font-weight: 700;
}

/* 队伍信息 */
.group-info {
  flex: 1;
  min-width: 0;
}
.group-leader {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 6px;
}
.leader-name {
  font-weight: 600;
}
.group-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.group-progress-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.progress {
  flex: 1;
}
.progress-text {
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.group-status {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.lack {
  color: var(--primary);
  font-weight: 700;
}

/* 参团按钮 */
.btn-join {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
  border: none;
  border-radius: 16px;
  padding: 7px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
}
.btn-join:active {
  opacity: 0.85;
}
</style>
