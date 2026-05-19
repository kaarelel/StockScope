import axios from 'axios'
import type {
  MarketInsight,
  MarketOverview,
  PortfolioRequest,
  PortfolioResponse,
  Stock,
} from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10_000,
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

export default api
