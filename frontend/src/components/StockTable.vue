<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Stock } from '../types'
import ChangeCell from './ChangeCell.vue'
import RiskBadge from './RiskBadge.vue'

const props = defineProps<{
  stocks: Stock[]
  sortBy?: string
  direction?: 'asc' | 'desc'
}>()

const emit = defineEmits<{ (e: 'sort', column: string): void }>()

const router = useRouter()

function open(symbol: string) {
  router.push({ name: 'stock-detail', params: { symbol } })
}

function sortClick(col: string) {
  emit('sort', col)
}

function indicator(col: string) {
  if (props.sortBy !== col) return ''
  return props.direction === 'asc' ? ' ↑' : ' ↓'
}
</script>

<template>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th @click="sortClick('symbol')">Sümbol{{ indicator('symbol') }}</th>
          <th @click="sortClick('name')">Nimi{{ indicator('name') }}</th>
          <th @click="sortClick('sector')">Sektor{{ indicator('sector') }}</th>
          <th @click="sortClick('price')" style="text-align: right">Hind{{ indicator('price') }}</th>
          <th @click="sortClick('change')" style="text-align: right">Muutus{{ indicator('change') }}</th>
          <th @click="sortClick('marketcap')" style="text-align: right">Market cap{{ indicator('marketcap') }}</th>
          <th @click="sortClick('risk')">Risk{{ indicator('risk') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in props.stocks" :key="s.symbol" @click="open(s.symbol)">
          <td><strong>{{ s.symbol }}</strong></td>
          <td>{{ s.name }}</td>
          <td><span class="badge badge-sector">{{ s.sector }}</span></td>
          <td style="text-align: right">${{ s.price.toFixed(2) }}</td>
          <td style="text-align: right"><ChangeCell :value="s.dayChangePct" /></td>
          <td style="text-align: right">${{ s.marketCapBillions }}B</td>
          <td><RiskBadge :risk="s.risk" /></td>
        </tr>
        <tr v-if="props.stocks.length === 0">
          <td colspan="7" class="muted" style="text-align: center; padding: 24px">
            Tulemusi pole.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrap {
  overflow-x: auto;
}
</style>
