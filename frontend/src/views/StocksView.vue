<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { stockApi } from '../api/client'
import type { Stock } from '../types'
import StockTable from '../components/StockTable.vue'

const stocks = ref<Stock[]>([])
const sectors = ref<string[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const search = ref('')
const sector = ref('all')
const sortBy = ref('symbol')
const direction = ref<'asc' | 'desc'>('asc')

let debounceTimer: ReturnType<typeof setTimeout> | null = null

async function load() {
  loading.value = true
  error.value = null
  try {
    stocks.value = await stockApi.list({
      search: search.value || undefined,
      sector: sector.value === 'all' ? undefined : sector.value,
      sort: sortBy.value,
      direction: direction.value,
    })
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  sectors.value = await stockApi.sectors()
  await load()
})

watch(search, () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(load, 250)
})

watch([sector, sortBy, direction], load)

function onSort(col: string) {
  if (sortBy.value === col) {
    direction.value = direction.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = col
    direction.value = 'asc'
  }
}
</script>

<template>
  <div>
    <div class="section">
      <h1>Aktsiad</h1>
      <p class="muted">Otsing, sortimine ja sektoripõhine filter.</p>
    </div>

    <div class="card section">
      <div class="filters">
        <input
          v-model="search"
          class="input"
          placeholder="Otsi sümboli või nime järgi (nt AAPL, Apple)..."
        />
        <select v-model="sector" class="select">
          <option value="all">Kõik sektorid</option>
          <option v-for="s in sectors" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Laadin...</span>
    </div>

    <div v-else-if="error" class="error">Viga: {{ error }}</div>

    <div v-else class="card">
      <div class="row-between" style="margin-bottom: 12px">
        <h3>{{ stocks.length }} aktsiat</h3>
        <span class="muted" style="font-size: 12px">Klõpsa veerule sortimiseks</span>
      </div>
      <StockTable
        :stocks="stocks"
        :sort-by="sortBy"
        :direction="direction"
        @sort="onSort"
      />
    </div>
  </div>
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

@media (max-width: 600px) {
  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
