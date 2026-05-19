<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { insightsApi } from '../api/client'
import type { MarketInsight } from '../types'

const insights = ref<MarketInsight[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    insights.value = await insightsApi.all()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <div class="section">
      <h1>Turuülevaated</h1>
      <p class="muted">Demo-trendid ja sektoripõhised tähelepanekud.</p>
    </div>

    <div v-if="loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Laadin...</span>
    </div>

    <div v-else-if="error" class="error">Viga: {{ error }}</div>

    <div v-else class="grid grid-2">
      <div v-for="(item, i) in insights" :key="i" class="card insight-card">
        <div class="row-between" style="margin-bottom: 8px">
          <span class="badge badge-sector">{{ item.category }}</span>
        </div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.summary }}</p>
        <div class="impact">
          <strong>Mõju:</strong> {{ item.impact }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.insight-card {
  display: flex;
  flex-direction: column;
}

.impact {
  margin-top: 12px;
  padding: 10px 12px;
  background: var(--bg-elevated);
  border-radius: 6px;
  font-size: 13px;
  border-left: 3px solid var(--accent);
}
</style>
