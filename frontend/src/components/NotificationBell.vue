<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAlertsStore } from '../stores/alerts'

const store = useAlertsStore()
const router = useRouter()
const open = ref(false)
const root = ref<HTMLElement | null>(null)

const badge = computed(() => (store.unreadCount > 99 ? '99+' : String(store.unreadCount)))

function toggle() {
  open.value = !open.value
}

function close() {
  open.value = false
}

function onMarkRead(id: number) {
  void store.markRead(id)
}

function onMarkAll() {
  void store.markAllRead()
}

function goToAlerts() {
  close()
  router.push({ name: 'alerts' })
}

function handleClickOutside(event: MouseEvent) {
  if (!root.value) return
  if (!root.value.contains(event.target as Node)) {
    close()
  }
}

onMounted(() => {
  store.startPolling()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  store.stopPolling()
  document.removeEventListener('click', handleClickOutside)
})

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString()
}
</script>

<template>
  <div ref="root" class="bell-wrap">
    <button
      type="button"
      class="bell-btn"
      :aria-label="`Notifications, ${store.unreadCount} unread`"
      @click="toggle"
    >
      <span class="bell-icon">🔔</span>
      <span v-if="store.unreadCount > 0" class="badge">{{ badge }}</span>
    </button>

    <div v-if="open" class="dropdown">
      <div class="dropdown-header">
        <strong>Notifications</strong>
        <button
          v-if="store.unreadCount > 0"
          class="link-btn"
          @click="onMarkAll"
        >Mark all read</button>
      </div>

      <div v-if="store.recentNotifications.length === 0" class="empty">
        <span class="muted">No notifications yet</span>
      </div>

      <ul v-else class="notification-list">
        <li
          v-for="n in store.recentNotifications"
          :key="n.id"
          :class="{ unread: !n.read }"
          @click="onMarkRead(n.id)"
        >
          <div class="row-between">
            <strong>{{ n.symbol }}</strong>
            <span class="muted" style="font-size: 12px">{{ formatTime(n.createdAt) }}</span>
          </div>
          <div class="msg">{{ n.message }}</div>
        </li>
      </ul>

      <div class="dropdown-footer">
        <button class="link-btn" @click="goToAlerts">Manage alerts →</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bell-wrap {
  position: relative;
}

.bell-btn {
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  color: var(--text);
  position: relative;
}

.bell-btn:hover {
  background: var(--bg-card);
}

.bell-icon {
  font-size: 18px;
}

.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 999px;
  padding: 1px 6px;
  min-width: 18px;
  text-align: center;
}

.dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  width: 340px;
  max-width: 90vw;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  z-index: 100;
  overflow: hidden;
}

.dropdown-header,
.dropdown-footer {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-footer {
  border-bottom: none;
  border-top: 1px solid var(--border);
  text-align: right;
  justify-content: flex-end;
}

.link-btn {
  background: transparent;
  border: none;
  color: var(--accent);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.link-btn:hover {
  text-decoration: underline;
}

.empty {
  padding: 24px;
  text-align: center;
}

.notification-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 360px;
  overflow-y: auto;
}

.notification-list li {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
}

.notification-list li:hover {
  background: var(--bg-card);
}

.notification-list li.unread {
  background: rgba(59, 130, 246, 0.08);
}

.notification-list li.unread strong {
  color: var(--accent);
}

.msg {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text);
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
