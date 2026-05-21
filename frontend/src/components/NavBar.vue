<script setup lang="ts">
import { RouterLink } from 'vue-router'
import NotificationBell from './NotificationBell.vue'
</script>

<template>
  <header class="navbar">
    <div class="navbar-inner">
      <RouterLink to="/" class="brand">
        <span class="brand-mark">📈</span>
        <span>StockScope</span>
      </RouterLink>
      <nav class="nav-links">
        <RouterLink to="/" exact-active-class="active">Dashboard</RouterLink>
        <RouterLink to="/stocks" active-class="active">Aktsiad</RouterLink>
        <RouterLink to="/watchlist" active-class="active">Watchlist</RouterLink>
        <RouterLink to="/alerts" active-class="active">Alerts</RouterLink>
        <RouterLink to="/portfolio" active-class="active">Portfell</RouterLink>
        <RouterLink to="/insights" active-class="active">Turuülevaated</RouterLink>
      </nav>
      <NotificationBell />
    </div>
  </header>
</template>

<style scoped>
.navbar {
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.nav-links {
  flex: 1;
  justify-content: center;
}

.brand {
  font-weight: 700;
  font-size: 18px;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-mark {
  font-size: 20px;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-links a {
  color: var(--text-muted);
  padding: 8px 14px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}

.nav-links a:hover {
  background: var(--bg-card);
  color: var(--text);
}

.nav-links a.active {
  background: var(--bg-card);
  color: var(--accent);
}

@media (max-width: 600px) {
  .navbar-inner {
    flex-direction: column;
    align-items: flex-start;
  }
  .nav-links {
    flex-wrap: wrap;
  }
}
</style>
