import axios from 'axios'
import type {
  AddItemPayload,
  Alert,
  CreateAlertPayload,
  CreateWatchlistPayload,
  MarketInsight,
  MarketOverview,
  Notification,
  PortfolioRequest,
  PortfolioResponse,
  RenameWatchlistPayload,
  Stock,
  Watchlist,
  WatchlistItemView,
} from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10_000,
  withCredentials: true,
})

export const marketApi = {
  overview: () => api.get<MarketOverview>('/market/overview').then((r) => r.data),
}

export interface StockListParams {
  search?: string
  sector?: string
  sort?: string
  direction?: 'asc' | 'desc'
}

export const stockApi = {
  list: (params: StockListParams = {}) =>
    api.get<Stock[]>('/stocks', { params }).then((r) => r.data),
  detail: (symbol: string) =>
    api.get<Stock>(`/stocks/${encodeURIComponent(symbol)}`).then((r) => r.data),
  sectors: () => api.get<string[]>('/stocks/sectors').then((r) => r.data),
  gainers: (limit = 5) => api.get<Stock[]>('/stocks/gainers', { params: { limit } }).then((r) => r.data),
  losers: (limit = 5) => api.get<Stock[]>('/stocks/losers', { params: { limit } }).then((r) => r.data),
}

export const portfolioApi = {
  simulate: (request: PortfolioRequest) =>
    api.post<PortfolioResponse>('/portfolio/simulate', request).then((r) => r.data),
}

export const insightsApi = {
  all: () => api.get<MarketInsight[]>('/insights').then((r) => r.data),
}

export const watchlistApi = {
  list: () => api.get<Watchlist[]>('/watchlist').then((r) => r.data),
  get: (id: number) => api.get<Watchlist>(`/watchlist/${id}`).then((r) => r.data),
  create: (payload: CreateWatchlistPayload) =>
    api.post<Watchlist>('/watchlist', payload).then((r) => r.data),
  rename: (id: number, payload: RenameWatchlistPayload) =>
    api.put<Watchlist>(`/watchlist/${id}`, payload).then((r) => r.data),
  remove: (id: number) => api.delete<void>(`/watchlist/${id}`).then(() => undefined),
  addItem: (id: number, payload: AddItemPayload) =>
    api.put<Watchlist>(`/watchlist/${id}/items`, payload).then((r) => r.data),
  removeItem: (id: number, symbol: string) =>
    api.delete<Watchlist>(`/watchlist/${id}/items/${encodeURIComponent(symbol)}`).then((r) => r.data),
  aggregatedItems: (limit = 5) =>
    api.get<WatchlistItemView[]>('/watchlist/items', { params: { limit } }).then((r) => r.data),
}

export const alertApi = {
  list: () => api.get<Alert[]>('/alerts').then((r) => r.data),
  create: (payload: CreateAlertPayload) =>
    api.post<Alert>('/alerts', payload).then((r) => r.data),
  remove: (id: number) => api.delete<void>(`/alerts/${id}`).then(() => undefined),
}

export const notificationApi = {
  list: (unreadOnly = false) =>
    api.get<Notification[]>('/notifications', { params: { unread: unreadOnly } }).then((r) => r.data),
  unreadCount: () =>
    api.get<{ count: number }>('/notifications/unread-count').then((r) => r.data.count),
  markRead: (id: number) =>
    api.patch<Notification>(`/notifications/${id}`).then((r) => r.data),
  markAllRead: () =>
    api.post<{ updated: number }>('/notifications/read-all').then((r) => r.data),
}

export default api
