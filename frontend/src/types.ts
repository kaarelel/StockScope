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
