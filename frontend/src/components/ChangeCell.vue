<script setup lang="ts">
const props = defineProps<{ value: number; suffix?: string }>()
</script>

<template>
  <span :class="props.value >= 0 ? 'positive' : 'negative'">
    {{ props.value >= 0 ? '+' : '' }}{{ props.value.toFixed(2) }}{{ props.suffix ?? '%' }}
  </span>
</template>
