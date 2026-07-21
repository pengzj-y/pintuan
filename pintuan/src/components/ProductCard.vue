<script setup>
defineProps({
  product: { type: Object, required: true },
})

const emit = defineEmits(['click'])
</script>

<template>
  <div class="product-card" @click="emit('click', product)">
    <!-- 商品图 -->
    <div class="product-img">
      <img :src="product.image" :alt="product.title" loading="lazy" />
      <span v-if="product.tag" class="product-tag">{{ product.tag }}</span>
    </div>
    <!-- 商品信息 -->
    <div class="product-info">
      <h3 class="product-title text-ellipsis-2">{{ product.title }}</h3>
      <div class="product-sales">
        <span v-if="product.sales">{{ product.sales }}人已拼</span>
        <span v-if="product.targetCount" class="target">需 {{ product.targetCount }} 人成团</span>
      </div>
      <div class="product-bottom">
        <div class="product-price">
          <span class="price">
            <span class="price-symbol">¥</span>{{ product.payPrice }}
          </span>
          <span v-if="product.originalPrice" class="price-original">
            ¥{{ product.originalPrice }}
          </span>
        </div>
        <button class="btn-go" @click.stop="emit('click', product)">去拼团</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform 0.15s;
}
.product-card:active {
  transform: scale(0.98);
}

/* 商品图 */
.product-img {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
}
.product-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-tag {
  position: absolute;
  top: 0;
  left: 0;
  background: var(--primary);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: var(--radius-sm) 0 var(--radius-sm) 0;
  font-weight: 500;
}

/* 商品信息 */
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.product-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--text);
}
.product-sales {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}
.target {
  color: var(--orange);
}

/* 底部价格+按钮 */
.product-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.product-price {
  display: flex;
  align-items: baseline;
}
.btn-go {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
  border: none;
  border-radius: 14px;
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
}
.btn-go:active {
  opacity: 0.85;
}
</style>
