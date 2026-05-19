<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { marketApi } from '../api/client'
import { useWatchlistStore } from '../stores/watchlist'
import type { MarketOverview, Stock } from '../types'
import ChangeCell from '../components/ChangeCell.vue'

const router = useRouter()
const watchlistStore = useWatchlistStore()
const overview = ref<MarketOverview | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    overview.value = await marketApi.overview()
    await watchlistStore.fetchAggregated(5)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
})

function openStock(s: Stock) {
  router.push({ name: 'stock-detail', params: { symbol: s.symbol } })
}

function openSymbol(symbol: string) {
  router.push({ name: 'stock-detail', params: { symbol } })
}
</script>

<template>
  <div>
    <div class="section">
      <h1>Dashboard</h1>
      <p class="muted">Turu üldseis, top aktsiad ja päevane kokkuvõte.</p>
    </div>

    <div v-if="loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Laadin...</span>
    </div>

    <div v-else-if="error" class="error">Viga: {{ error }}</div>

    <template v-else-if="overview">
      <div class="card section">
        <div class="row-between">
          <h2>Turu kokkuvõte</h2>
          <span class="badge badge-sector">{{ overview.sentiment }}</span>
        </div>
        <p>{{ overview.summary }}</p>
      </div>

      <div class="grid grid-4 section">
        <div v-for="idx in overview.indices" :key="idx.name" class="card kpi">
          <span class="kpi-label">{{ idx.name }}</span>
          <span class="kpi-value">{{ idx.value.toFixed(2) }}</span>
          <span class="kpi-change"><ChangeCell :value="idx.dayChangePct" /></span>
        </div>
      </div>

      <div class="grid grid-2 section">
        <div class="card">
          <div class="row-between" style="margin-bottom: 12px">
            <h3>Top tõusjad</h3>
            <span class="badge badge-low">gainers</span>
          </div>
          <div class="movers">
            <div
              v-for="s in overview.topGainers"
              :key="s.symbol"
              class="mover"
              @click="openStock(s)"
            >
              <div>
                <div><strong>{{ s.symbol }}</strong></div>
                <div class="muted" style="font-size: 12px">{{ s.name }}</div>
              </div>
              <div style="text-align: right">
                <div>${{ s.price.toFixed(2) }}</div>
                <ChangeCell :value="s.dayChangePct" />
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="row-between" style="margin-bottom: 12px">
            <h3>Top langejad</h3>
            <span class="badge badge-high">losers</span>
          </div>
          <div class="movers">
            <div
              v-for="s in overview.topLosers"
              :key="s.symbol"
              class="mover"
              @click="openStock(s)"
            >
              <div>
                <div><strong>{{ s.symbol }}</strong></div>
                <div class="muted" style="font-size: 12px">{{ s.name }}</div>
              </div>
              <div style="text-align: right">
                <div>${{ s.price.toFixed(2) }}</div>
                <ChangeCell :value="s.dayChangePct" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-2 section">
        <div class="card">
          <div class="row-between" style="margin-bottom: 12px">
            <h3>Kõige aktiivsemad</h3>
            <span class="muted" style="font-size: 12px">käive miljardites $</span>
          </div>
          <div class="movers">
            <div
              v-for="s in overview.mostActive"
              :key="s.symbol"
              class="mover"
              @click="openStock(s)"
            >
              <div>
                <div><strong>{{ s.symbol }}</strong></div>
                <div class="muted" style="font-size: 12px">{{ s.name }}</div>
              </div>
              <div style="text-align: right">
                <div>${{ s.price.toFixed(2) }}</div>
                <span class="muted" style="font-size: 12px">{{ s.volumeMillions }}M</span>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="row-between" style="margin-bottom: 12px">
            <h3>Sinu Watchlist</h3>
            <RouterLink to="/watchlist" class="muted" style="font-size: 12px">Halda →</RouterLink>
          </div>
          <div v-if="watchlistStore.aggregated.length === 0" class="muted" style="text-align: center; padding: 16px">
            Watchlist on tühi. <RouterLink to="/watchlist">Lisa aktsiad →</RouterLink>
          </div>
          <div v-else class="movers">
            <div
              v-for="item in watchlistStore.aggregated"
              :key="item.symbol"
              class="mover"
              @click="openSymbol(item.symbol)"
            >
              <div>
                <div><strong>{{ item.symbol }}</strong></div>
                <div class="muted" style="font-size: 12px">{{ item.name }}</div>
              </div>
              <div style="text-align: right">
                <div v-if="item.known">${{ item.price.toFixed(2) }}</div>
                <div v-else class="muted" style="font-size: 12px">unknown</div>
                <ChangeCell v-if="item.known" :value="item.dayChangePct" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.movers {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mover {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: var(--bg-elevated);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.mover:hover {
  background: rgba(56, 189, 248, 0.08);
}
</style>
