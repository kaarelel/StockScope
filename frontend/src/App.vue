<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavBar from './components/NavBar.vue'
</script>

<template>
  <div class="app-shell">
    <NavBar />
    <main class="app-main">
      <RouterView />
    </main>
    <footer class="app-footer">
      <span>StockScope &middot; demo dashboard &middot; mock-andmed</span>
    </footer>
  </div>
</template>
