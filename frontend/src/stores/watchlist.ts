import { defineStore } from 'pinia'
import { watchlistApi } from '../api/client'
import type { Watchlist, WatchlistItemView } from '../types'

interface State {
  lists: Watchlist[]
  aggregated: WatchlistItemView[]
  loading: boolean
  error: string | null
}

function extractMessage(e: unknown, fallback: string): string {
  const err = e as { response?: { data?: { error?: string } }; message?: string }
  return err.response?.data?.error || err.message || fallback
}

export const useWatchlistStore = defineStore('watchlist', {
  state: (): State => ({
    lists: [],
    aggregated: [],
    loading: false,
    error: null,
  }),
  getters: {
    findById: (state) => (id: number) => state.lists.find((l) => l.id === id) ?? null,
  },
  actions: {
    async fetchAll() {
      this.loading = true
      this.error = null
      try {
        this.lists = await watchlistApi.list()
      } catch (e) {
        this.error = extractMessage(e, 'Failed to load watchlists')
      } finally {
        this.loading = false
      }
    },
    async createList(name: string): Promise<Watchlist | null> {
      this.error = null
      try {
        const created = await watchlistApi.create({ name })
        this.lists = [created, ...this.lists]
        return created
      } catch (e) {
        this.error = extractMessage(e, 'Failed to create watchlist')
        return null
      }
    },
    async renameList(id: number, name: string) {
      this.error = null
      try {
        const updated = await watchlistApi.rename(id, { name })
        this.lists = this.lists.map((l) => (l.id === id ? updated : l))
      } catch (e) {
        this.error = extractMessage(e, 'Failed to rename watchlist')
      }
    },
    async deleteList(id: number) {
      this.error = null
      try {
        await watchlistApi.remove(id)
        this.lists = this.lists.filter((l) => l.id !== id)
      } catch (e) {
        this.error = extractMessage(e, 'Failed to delete watchlist')
      }
    },
    async addItem(id: number, symbol: string) {
      this.error = null
      try {
        const updated = await watchlistApi.addItem(id, { symbol })
        this.lists = this.lists.map((l) => (l.id === id ? updated : l))
      } catch (e) {
        this.error = extractMessage(e, 'Failed to add symbol')
      }
    },
    async removeItem(id: number, symbol: string) {
      this.error = null
      try {
        const updated = await watchlistApi.removeItem(id, symbol)
        this.lists = this.lists.map((l) => (l.id === id ? updated : l))
      } catch (e) {
        this.error = extractMessage(e, 'Failed to remove symbol')
      }
    },
    async fetchAggregated(limit = 5) {
      try {
        this.aggregated = await watchlistApi.aggregatedItems(limit)
      } catch (e) {
        this.error = extractMessage(e, 'Failed to load aggregated items')
      }
    },
  },
})
