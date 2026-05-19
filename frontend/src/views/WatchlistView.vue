<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { stockApi } from '../api/client'
import { useWatchlistStore } from '../stores/watchlist'
import type { Stock } from '../types'

const router = useRouter()
const store = useWatchlistStore()
const stocks = ref<Stock[]>([])
const newListName = ref('')
const symbolInput = ref<Record<number, string>>({})

const sortedSymbols = computed(() =>
  [...stocks.value].sort((a, b) => a.symbol.localeCompare(b.symbol)),
)

onMounted(async () => {
  stocks.value = await stockApi.list()
  await store.fetchAll()
})

async function createList() {
  const name = newListName.value.trim()
  if (!name) return
  const created = await store.createList(name)
  if (created) {
    newListName.value = ''
  }
}

async function addSymbol(listId: number) {
  const symbol = (symbolInput.value[listId] || '').trim().toUpperCase()
  if (!symbol) return
  await store.addItem(listId, symbol)
  symbolInput.value[listId] = ''
}

function openStock(symbol: string) {
  router.push({ name: 'stock-detail', params: { symbol } })
}
</script>

<template>
  <div>
    <div class="section">
      <h1>Watchlists</h1>
      <p class="muted">Save stocks of interest. Lists persist across reloads via your session cookie.</p>
    </div>

    <div class="card section">
      <h3>Create new watchlist</h3>
      <form class="row" style="gap: 8px; margin-top: 8px" @submit.prevent="createList">
        <input
          v-model="newListName"
          class="input"
          placeholder="e.g. Tech picks, Dividend stocks..."
          maxlength="120"
        />
        <button class="btn" type="submit" :disabled="!newListName.trim()">Create</button>
      </form>
    </div>

    <div v-if="store.error" class="error section">{{ store.error }}</div>

    <div v-if="store.loading" class="card row" style="justify-content: center">
      <span class="spinner" /> <span class="muted">Loading...</span>
    </div>

    <div v-else-if="store.lists.length === 0" class="card" style="text-align: center; padding: 32px">
      <p class="muted">No watchlists yet. Create one above to start tracking stocks.</p>
    </div>

    <div v-else class="grid grid-2">
      <div v-for="list in store.lists" :key="list.id" class="card watchlist-card">
        <div class="row-between" style="margin-bottom: 12px">
          <h3>{{ list.name }}</h3>
          <button class="btn-danger" @click="store.deleteList(list.id)">Delete</button>
        </div>

        <div class="row" style="gap: 8px; margin-bottom: 12px">
          <select v-model="symbolInput[list.id]" class="select">
            <option value="">Pick a symbol...</option>
            <option v-for="s in sortedSymbols" :key="s.symbol" :value="s.symbol">
              {{ s.symbol }} — {{ s.name }}
            </option>
          </select>
          <button class="btn" @click="addSymbol(list.id)" :disabled="!symbolInput[list.id]">Add</button>
        </div>

        <div v-if="list.symbols.length === 0" class="muted" style="text-align: center; padding: 12px">
          Empty. Add a symbol above.
        </div>
        <ul v-else class="symbols">
          <li v-for="symbol in list.symbols" :key="symbol">
            <span class="symbol-chip" @click="openStock(symbol)">{{ symbol }}</span>
            <button class="btn-danger" @click="store.removeItem(list.id, symbol)">×</button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.watchlist-card {
  display: flex;
  flex-direction: column;
}

.symbols {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.symbols li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-elevated);
  padding: 8px 12px;
  border-radius: 6px;
}

.symbol-chip {
  cursor: pointer;
  font-weight: 600;
  color: var(--accent);
}

.symbol-chip:hover {
  color: var(--accent-hover);
  text-decoration: underline;
}
</style>
