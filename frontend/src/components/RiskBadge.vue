<script setup lang="ts">
import type { RiskLevel } from '../types'

const props = defineProps<{ risk: RiskLevel }>()

const labelMap: Record<RiskLevel, string> = {
  LOW: 'madal',
  MEDIUM: 'keskmine',
  HIGH: 'kõrge',
}

const classMap: Record<RiskLevel, string> = {
  LOW: 'badge-low',
  MEDIUM: 'badge-medium',
  HIGH: 'badge-high',
}
</script>

<template>
  <span class="badge" :class="classMap[props.risk]">{{ labelMap[props.risk] }}</span>
</template>
