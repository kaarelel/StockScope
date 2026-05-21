export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Stock {
  symbol: string
  name: string
  sector: string
  price: number
  previousClose: number
  dayChangePct: number
  weekChangePct: number
  monthChangePct: number
  yearChangePct: number
  marketCapBillions: number
  volumeMillions: number
  peRatio: number
  dividendYield: number
  risk: RiskLevel
  summary: string
}

export interface MarketIndex {
  name: string
  value: number
  dayChangePct: number
}

export interface MarketOverview {
  summary: string
  sentiment: string
  indices: MarketIndex[]
  topGainers: Stock[]
  topLosers: Stock[]
  mostActive: Stock[]
}

export interface MarketInsight {
  title: string
  category: string
  summary: string
  impact: string
}

export interface PortfolioAllocation {
  symbol: string
  weightPct: number
}

export interface PortfolioRequest {
  totalAmount: number
  allocations: PortfolioAllocation[]
}

export interface PortfolioHolding {
  symbol: string
  name: string
  sector: string
  weightPct: number
  allocatedAmount: number
  shares: number
  currentValue: number
  returnPct: number
}

export interface PortfolioResponse {
  invested: number
  currentValue: number
  absoluteReturn: number
  returnPct: number
  holdings: PortfolioHolding[]
  sectorAllocation: Record<string, number>
  overallRisk: RiskLevel
  commentary: string
}

export interface Watchlist {
  id: number
  name: string
  createdAt: string
  updatedAt: string
  symbols: string[]
}

export interface WatchlistItemView {
  symbol: string
  name: string
  sector: string
  price: number
  dayChangePct: number
  known: boolean
}

export interface CreateWatchlistPayload {
  name: string
}

export interface RenameWatchlistPayload {
  name: string
}

export interface AddItemPayload {
  symbol: string
}

export type AlertCondition = 'ABOVE' | 'BELOW'

export interface Alert {
  id: number
  symbol: string
  condition: AlertCondition
  targetPrice: number
  active: boolean
  createdAt: string
  lastTriggeredAt: string | null
}

export interface CreateAlertPayload {
  symbol: string
  condition: AlertCondition
  targetPrice: number
}

export interface Notification {
  id: number
  alertId: number
  symbol: string
  condition: AlertCondition
  targetPrice: number
  triggeredPrice: number
  message: string
  read: boolean
  createdAt: string
}
