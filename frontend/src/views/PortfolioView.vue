<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { portfolioApi, stockApi } from '../api/client'
import type { PortfolioResponse, Stock } from '../types'
import ChangeCell from '../components/ChangeCell.vue'
import RiskBadge from '../components/RiskBadge.vue'

interface Row {
  symbol: string
  weightPct: number
}

const stocks = ref<Stock[]>([])
const totalAmount = ref(10000)
const rows = ref<Row[]>([
  { symbol: 'AAPL', weightPct: 30 },
  { symbol: 'MSFT', weightPct: 25 },
  { symbol: 'NVDA', weightPct: 20 },
  { symbol: 'JPM', weightPct: 15 },
  { symbol: 'KO', weightPct: 10 },
])
const result = ref<PortfolioResponse | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const totalWeight = computed(() =>
  rows.value.reduce((sum, r) => sum + (Number(r.weightPct) || 0), 0),
)

onMounted(async () => {
  stocks.value = await stockApi.list()
})

function addRow() {
  const used = new Set(rows.value.map((r) => r.symbol))
  const next = stocks.value.find((s) => !used.has(s.symbol))
  if (next) rows.value.push({ symbol: next.symbol, weightPct: 0 })
}

function removeRow(idx: number) {
  rows.value.splice(idx, 1)
}

async function simulate() {
  if (totalAmount.value <= 0) {
    error.value = 'Sisesta investeerimissumma > 0'
    return
  }
  if (rows.value.length === 0) {
    error.value = 'Lisa vähemalt üks aktsia'
    return
  }
  loading.value = true
  error.value = null
  try {
    result.value = await portfolioApi.simulate({
      totalAmount: totalAmount.value,
      allocations: rows.value.map((r) => ({
        symbol: r.symbol,
        weightPct: Number(r.weightPct) || 0,
      })),
    })
  } catch (e: unknown) {
    const errObj = e as { response?: { data?: { error?: string } }; message?: string }
    error.value = errObj.response?.data?.error || errObj.message || 'Tundmatu viga'
  } finally {
    loading.value = false
  }
}

function fmt(n: number): string {
  return n.toLocaleString('et-EE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<template>
  <div>
    <div class="section">
      <h1>Portfelli simulaator</h1>
      <p class="muted">Sisesta kujuteldav investeering ja vaata jaotust ning tootlust.</p>
    </div>

    <div class="card section">
      <h3>Konfiguratsioon</h3>

      <div class="row" style="gap: 12px; margin-bottom: 16px">
        <label style="flex: 1">
          <span class="muted" style="font-size: 12px">Investeerimissumma ($)</span>
          <input v-model.number="totalAmount" type="number" min="1" class="input" />
        </label>
        <label style="flex: 1">
          <span class="muted" style="font-size: 12px">Kaalud kokku</span>
          <div class="input" style="display: flex; align-items: center;">
            {{ totalWeight.toFixed(1) }}%
            <span v-if="Math.abs(totalWeight - 100) > 0.01" class="muted" style="margin-left: 8px; font-size: 12px">
              (normaliseeritakse 100%-ks)
            </span>
          </div>
        </label>
      </div>

      <div class="alloc-rows">
        <div v-for="(row, idx) in rows" :key="idx" class="alloc-row">
          <select v-model="row.symbol" class="select">
            <option v-for="s in stocks" :key="s.symbol" :value="s.symbol">
              {{ s.symbol }} — {{ s.name }}
            </option>
          </select>
          <input
            v-model.number="row.weightPct"
            type="number"
            min="0"
            class="input"
            style="max-width: 110px"
            placeholder="kaal %"
          />
          <button class="btn btn-danger" @click="removeRow(idx)">×</button>
        </div>
      </div>

      <div class="row" style="margin-top: 16px; gap: 8px">
        <button class="btn btn-secondary" @click="addRow">+ Lisa aktsia</button>
        <button class="btn" @click="simulate" :disabled="loading">
          {{ loading ? 'Arvutan...' : 'Simuleeri' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="error section">{{ error }}</div>

    <template v-if="result">
      <div class="grid grid-4 section">
        <div class="card kpi">
          <span class="kpi-label">Investeering</span>
          <span class="kpi-value">${{ fmt(result.invested) }}</span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Praegune väärtus</span>
          <span class="kpi-value">${{ fmt(result.currentValue) }}</span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Absoluutne tootlus</span>
          <span class="kpi-value" :class="result.absoluteReturn >= 0 ? 'positive' : 'negative'">
            {{ result.absoluteReturn >= 0 ? '+' : '' }}${{ fmt(result.absoluteReturn) }}
          </span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Tootlus %</span>
          <span class="kpi-value"><ChangeCell :value="result.returnPct" /></span>
        </div>
      </div>

      <div class="grid grid-2 section">
        <div class="card">
          <h3>Sektorite jaotus</h3>
          <div class="sector-bars">
            <div v-for="(weight, name) in result.sectorAllocation" :key="name" class="sector-bar">
              <div class="row-between" style="margin-bottom: 4px">
                <span>{{ name }}</span>
                <span class="muted">{{ weight.toFixed(1) }}%</span>
              </div>
              <div class="bar">
                <div class="bar-fill" :style="{ width: weight + '%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <h3>Üldhinnang</h3>
          <div class="row" style="margin-bottom: 12px">
            <span class="muted">Üldine risk:</span>
            <RiskBadge :risk="result.overallRisk" />
          </div>
          <p>{{ result.commentary }}</p>
        </div>
      </div>

      <div class="card section">
        <h3>Positsioonid</h3>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sümbol</th>
                <th>Nimi</th>
                <th>Sektor</th>
                <th style="text-align: right">Kaal</th>
                <th style="text-align: right">Investeeritud</th>
                <th style="text-align: right">Aktsiaid</th>
                <th style="text-align: right">Praegune väärtus</th>
                <th style="text-align: right">Tootlus</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="h in result.holdings" :key="h.symbol">
                <td><strong>{{ h.symbol }}</strong></td>
                <td>{{ h.name }}</td>
                <td><span class="badge badge-sector">{{ h.sector }}</span></td>
                <td style="text-align: right">{{ h.weightPct.toFixed(1) }}%</td>
                <td style="text-align: right">${{ fmt(h.allocatedAmount) }}</td>
                <td style="text-align: right">{{ h.shares.toFixed(3) }}</td>
                <td style="text-align: right">${{ fmt(h.currentValue) }}</td>
                <td style="text-align: right"><ChangeCell :value="h.returnPct" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.alloc-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alloc-row {
  display: grid;
  grid-template-columns: 1fr 110px auto;
  gap: 8px;
  align-items: center;
}

.sector-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar {
  height: 8px;
  background: var(--bg-elevated);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent), var(--accent-hover));
  transition: width 0.3s;
}
</style>
