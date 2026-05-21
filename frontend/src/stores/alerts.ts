import { defineStore } from 'pinia'
import { alertApi, notificationApi } from '../api/client'
import type { Alert, CreateAlertPayload, Notification } from '../types'

interface State {
  alerts: Alert[]
  notifications: Notification[]
  unreadCount: number
  loading: boolean
  error: string | null
}

function extractMessage(e: unknown, fallback: string): string {
  const err = e as { response?: { data?: { error?: string } }; message?: string }
  return err.response?.data?.error || err.message || fallback
}

const POLL_INTERVAL_MS = 15000

let pollHandle: number | null = null

export const useAlertsStore = defineStore('alerts', {
  state: (): State => ({
    alerts: [],
    notifications: [],
    unreadCount: 0,
    loading: false,
    error: null,
  }),
  getters: {
    recentNotifications: (state) => state.notifications.slice(0, 10),
    hasUnread: (state) => state.unreadCount > 0,
  },
  actions: {
    async fetchAlerts() {
      this.loading = true
      this.error = null
      try {
        this.alerts = await alertApi.list()
      } catch (e) {
        this.error = extractMessage(e, 'Failed to load alerts')
      } finally {
        this.loading = false
      }
    },
    async createAlert(payload: CreateAlertPayload): Promise<Alert | null> {
      this.error = null
      try {
        const created = await alertApi.create(payload)
        this.alerts = [created, ...this.alerts]
        return created
      } catch (e) {
        this.error = extractMessage(e, 'Failed to create alert')
        return null
      }
    },
    async deleteAlert(id: number) {
      this.error = null
      try {
        await alertApi.remove(id)
        this.alerts = this.alerts.filter((a) => a.id !== id)
      } catch (e) {
        this.error = extractMessage(e, 'Failed to delete alert')
      }
    },
    async fetchNotifications() {
      try {
        this.notifications = await notificationApi.list(false)
        this.unreadCount = this.notifications.filter((n) => !n.read).length
      } catch (e) {
        this.error = extractMessage(e, 'Failed to load notifications')
      }
    },
    async fetchUnreadCount() {
      try {
        this.unreadCount = await notificationApi.unreadCount()
      } catch {
        // best-effort polling; ignore transient errors
      }
    },
    async markRead(id: number) {
      try {
        const updated = await notificationApi.markRead(id)
        this.notifications = this.notifications.map((n) => (n.id === id ? updated : n))
        await this.fetchUnreadCount()
      } catch (e) {
        this.error = extractMessage(e, 'Failed to mark read')
      }
    },
    async markAllRead() {
      try {
        await notificationApi.markAllRead()
        this.notifications = this.notifications.map((n) => ({ ...n, read: true }))
        this.unreadCount = 0
      } catch (e) {
        this.error = extractMessage(e, 'Failed to mark all read')
      }
    },
    startPolling() {
      this.stopPolling()
      void this.fetchNotifications()
      pollHandle = window.setInterval(() => {
        void this.fetchNotifications()
      }, POLL_INTERVAL_MS)
    },
    stopPolling() {
      if (pollHandle != null) {
        window.clearInterval(pollHandle)
        pollHandle = null
      }
    },
  },
})
