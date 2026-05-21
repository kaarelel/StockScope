<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { stockApi } from '../api/client'
import { useAlertsStore } from '../stores/alerts'
import type { AlertCondition, Stock } from '../types'

const store = useAlertsStore()
const route = useRoute()
const router = useRouter()

const stocks = ref<Stock[]>([])
const symbolQuery = ref('')
const condition = ref<AlertCondition>('ABOVE')
const targetPrice = ref<number | null>(null)
const formError = ref<string | null>(null)
const showSuggestions = ref(false)

const filteredStocks = computed(() => {
  const q = symbolQuery.value.trim().toUpperCase()
  if (!q) return stocks.value.slice(0, 8)
  return stocks.value
    .filter((s) => s.symbol.includes(q) || s.name.toUpperCase().includes(q))
    .slice(0, 8)
})

const selectedStock = computed<Stock | null>(() => {
  const q = symbolQuery.value.trim().toUpperCase()
  return stocks.value.find((s) => s.symbol === q) ?? null
})

onMounted(async () => {
  stocks.value = await stockApi.list()
  await store.fetchAlerts()
  const prefill = route.query.symbol
  if (typeof prefill === 'string') {
    symbolQuery.value = prefill.toUpperCase()
    const stock = stocks.value.find((s) => s.symbol === symbolQuery.value)
    if (stock) {
      targetPrice.value = round(stock.price)
    }
  }
})

function pickSymbol(symbol: string) {
  symbolQuery.value = symbol
  showSuggestions.value = false
  const stock = stocks.value.find((s) => s.symbol === symbol)
  if (stock && targetPrice.value == null) {
    targetPrice.value = round(stock.price)
  }
}

function deferHideSuggestions() {
  window.setTimeout(() => {
    showSuggestions.value = false
  }, 150)
}

function round(value: number): number {
  return Math.round(value * 100) / 100
}

async function submit() {
  formError.value = null
  const symbol = symbolQuery.value.trim().toUpperCase()
  if (!selectedStock.value) {
    formError.value = 'Pick a known symbol from the list'
    return
  }
  if (targetPrice.value == null || targetPrice.value <= 0) {
    formError.value = 'Target price must be positive'
    return
  }
  const created = await store.createAlert({
    symbol,
    condition: condition.value,
    targetPrice: targetPrice.value,
  })
  if (created) {
    symbolQuery.value = ''
    targetPrice.value = null
    condition.value = 'ABOVE'
  } else if (store.error) {
    formError.value = store.error
  }
}

function openStock(symbol: string) {
  router.push({ name: 'stock-detail', params: { symbol } })
}
</script>

<template>
  <div>
    <div class="section">
      <h1>Alerts</h1>
      <p class="muted">Get notified when a symbol crosses a price threshold. Mock prices drift every 30 seconds.</p>
    </div>

    <div class="card section">
      <h3>Create alert</h3>
      <form class="form-grid" @submit.prevent="submit">
        <label class="field" style="position: relative">
          <span>Symbol</span>
          <input
            v-model="symbolQuery"
            class="input"
            placeholder="AAPL, MSFT..."
            maxlength="20"
            autocomplete="off"
            @focus="showSuggestions = true"
            @blur="deferHideSuggestions"
          />
          <ul v-if="showSuggestions && filteredStocks.length > 0" class="autocomplete">
            <li
              v-for="s in filteredStocks"
              :key="s.symbol"
              @mousedown.prevent="pickSymbol(s.symbol)"
            >
              <strong>{{ s.symbol }}</strong>
              <span class="muted"> — {{ s.name }} (${{ s.price.toFixed(2) }})</span>
            </li>
          </ul>
        </label>

        <label class="field">
          <span>Condition</span>
          <select v-model="condition" class="select">
            <option value="ABOVE">Price ABOVE</option>
            <option value="BELOW">Price BELOW</option>
          </select>
        </label>

        <label class="field">
          <span>Target price</span>
          <input
            v-model.number="targetPrice"
            type="number"
            min="0.01"
            step="0.01"
            class="input"
            placeholder="e.g. 250.00"
          />
        </label>

        <div class="field" style="align-self: end">
          <button class="btn" type="submit">Create alert</button>
        </div>
      </form>

      <div v-if="formError" class="error" style="margin-top: 12px">{{ formError }}</div>
      <div v-if="selectedStock" class="muted" style="margin-top: 8px">
        Current price for <strong>{{ selectedStock.symbol }}</strong>: ${{ selectedStock.price.toFixed(2) }}
      </div>
    </div>

    <div v-if="store.loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Loading...</span>
    </div>

    <div v-else-if="store.alerts.length === 0" class="card" style="text-align: center; padding: 32px">
      <p class="muted">No alerts yet. Create one above.</p>
    </div>

    <div v-else class="card section">
      <h3>Your alerts ({{ store.alerts.length }})</h3>
      <table class="table">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Condition</th>
            <th>Target</th>
            <th>Status</th>
            <th>Created</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="alert in store.alerts" :key="alert.id">
            <td>
              <span class="symbol-chip" @click="openStock(alert.symbol)">{{ alert.symbol }}</span>
            </td>
            <td>{{ alert.condition === 'ABOVE' ? '≥' : '≤' }}</td>
            <td>${{ alert.targetPrice.toFixed(2) }}</td>
            <td>
              <span v-if="alert.active" class="badge badge-active">Active</span>
              <span v-else class="badge badge-fired">Triggered</span>
            </td>
            <td class="muted">{{ new Date(alert.createdAt).toLocaleString() }}</td>
            <td>
              <button class="btn-danger" @click="store.deleteAlert(alert.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr auto;
  gap: 12px;
  margin-top: 8px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.autocomplete {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  z-index: 50;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 6px;
  list-style: none;
  margin: 0;
  padding: 4px;
  max-height: 240px;
  overflow-y: auto;
}

.autocomplete li {
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text);
}

.autocomplete li:hover {
  background: var(--bg-card);
}

.symbol-chip {
  cursor: pointer;
  font-weight: 600;
  color: var(--accent);
}

.symbol-chip:hover {
  text-decoration: underline;
}

.badge-active {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.badge-fired {
  background: rgba(148, 163, 184, 0.2);
  color: var(--text-muted);
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
