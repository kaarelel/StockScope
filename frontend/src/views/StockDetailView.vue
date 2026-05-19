<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { stockApi } from '../api/client'
import type { Stock } from '../types'
import ChangeCell from '../components/ChangeCell.vue'
import RiskBadge from '../components/RiskBadge.vue'

const route = useRoute()
const router = useRouter()
const stock = ref<Stock | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function load(symbol: string) {
  loading.value = true
  error.value = null
  stock.value = null
  try {
    stock.value = await stockApi.detail(symbol)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(() => load(String(route.params.symbol)))
watch(() => route.params.symbol, (s) => s && load(String(s)))

function commentary(s: Stock): string {
  const parts: string[] = []

  if (s.dayChangePct >= 2) {
    parts.push('Päevane liikumine on tugevalt positiivne — võimalik momentum.')
  } else if (s.dayChangePct <= -2) {
    parts.push('Päevane liikumine on tugevalt negatiivne — vaadake uudiseid.')
  } else {
    parts.push('Päevane liikumine on tagasihoidlik.')
  }

  if (s.yearChangePct > 25) {
    parts.push(`Aastane tootlus +${s.yearChangePct.toFixed(1)}% on muljetavaldav.`)
  } else if (s.yearChangePct < -10) {
    parts.push(`Aastane tootlus ${s.yearChangePct.toFixed(1)}% on negatiivne — pikem nõrkus.`)
  }

  if (s.peRatio > 50) {
    parts.push('P/E suhe viitab kõrgele hindamisele — kasvuootus on suur.')
  } else if (s.peRatio > 0 && s.peRatio < 15) {
    parts.push('P/E suhe on suhteliselt madal — väärtusaktsia kandidaat.')
  }

  if (s.dividendYield >= 3) {
    parts.push(`Dividenditootlus ${s.dividendYield.toFixed(2)}% pakub korralikku rahavoogu.`)
  }

  if (s.risk === 'HIGH') {
    parts.push('Hoiatus: kõrge volatiilsus, sobib vaid suurema riskitaluvusega investorile.')
  } else if (s.risk === 'LOW') {
    parts.push('Madal riskitase — sobib defensiivsesse osasse portfellist.')
  }

  return parts.join(' ')
}
</script>

<template>
  <div>
    <button class="btn btn-secondary" @click="router.back()" style="margin-bottom: 16px">
      ← Tagasi
    </button>

    <div v-if="loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Laadin...</span>
    </div>

    <div v-else-if="error" class="error">Viga: {{ error }}</div>

    <template v-else-if="stock">
      <div class="card section">
        <div class="header">
          <div>
            <div class="symbol">{{ stock.symbol }}</div>
            <h1>{{ stock.name }}</h1>
            <div class="row" style="gap: 8px">
              <span class="badge badge-sector">{{ stock.sector }}</span>
              <RiskBadge :risk="stock.risk" />
            </div>
          </div>
          <div class="price">
            <div class="price-value">${{ stock.price.toFixed(2) }}</div>
            <div class="price-change">
              <ChangeCell :value="stock.dayChangePct" />
              <span class="muted" style="margin-left: 8px">eelmine: ${{ stock.previousClose.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-4 section">
        <div class="card kpi">
          <span class="kpi-label">Nädal</span>
          <span class="kpi-value-sm"><ChangeCell :value="stock.weekChangePct" /></span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Kuu</span>
          <span class="kpi-value-sm"><ChangeCell :value="stock.monthChangePct" /></span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Aasta</span>
          <span class="kpi-value-sm"><ChangeCell :value="stock.yearChangePct" /></span>
        </div>
        <div class="card kpi">
          <span class="kpi-label">Käive (M)</span>
          <span class="kpi-value-sm">{{ stock.volumeMillions }}M</span>
        </div>
      </div>

      <div class="grid grid-2 section">
        <div class="card">
          <h3>Põhinäitajad</h3>
          <dl class="metrics">
            <div><dt>Market cap</dt><dd>${{ stock.marketCapBillions }}B</dd></div>
            <div><dt>P/E suhe</dt><dd>{{ stock.peRatio > 0 ? stock.peRatio.toFixed(1) : '—' }}</dd></div>
            <div><dt>Dividenditootlus</dt><dd>{{ stock.dividendYield > 0 ? stock.dividendYield.toFixed(2) + '%' : '—' }}</dd></div>
            <div><dt>Eelmine sulgemine</dt><dd>${{ stock.previousClose.toFixed(2) }}</dd></div>
            <div><dt>Sektor</dt><dd>{{ stock.sector }}</dd></div>
            <div><dt>Riskitase</dt><dd><RiskBadge :risk="stock.risk" /></dd></div>
          </dl>
        </div>

        <div class="card">
          <h3>Ülevaade</h3>
          <p>{{ stock.summary }}</p>
          <div class="commentary">
            <strong>Kommentaar:</strong>
            <p style="margin-top: 8px">{{ commentary(stock) }}</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.symbol {
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.price {
  text-align: right;
}

.price-value {
  font-size: 32px;
  font-weight: 700;
}

.kpi-value-sm {
  font-size: 18px;
  font-weight: 600;
}

.metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  margin: 0;
}

.metrics > div {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px dashed var(--border);
  padding: 6px 0;
}

.metrics dt {
  color: var(--text-muted);
  font-size: 13px;
}

.metrics dd {
  margin: 0;
  font-weight: 500;
}

.commentary {
  margin-top: 16px;
  padding: 12px;
  background: var(--bg-elevated);
  border-radius: 6px;
  border-left: 3px solid var(--accent);
}
</style>
