import { defineStore } from 'pinia'
import { stockApi, type StockListParams } from '../api/client'
import type { Stock } from '../types'

interface State {
  stocks: Stock[]
  sectors: string[]
  loading: boolean
  error: string | null
}

export const useStocksStore = defineStore('stocks', {
  state: (): State => ({
    stocks: [],
    sectors: [],
    loading: false,
    error: null,
  }),
  actions: {
    async fetch(params: StockListParams = {}) {
      this.loading = true
      this.error = null
      try {
        this.stocks = await stockApi.list(params)
      } catch (e) {
        this.error = (e as Error).message
      } finally {
        this.loading = false
      }
    },
    async fetchSectors() {
      if (this.sectors.length > 0) return
      try {
        this.sectors = await stockApi.sectors()
      } catch (e) {
        this.error = (e as Error).message
      }
    },
  },
})
