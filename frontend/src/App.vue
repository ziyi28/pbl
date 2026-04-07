<template>
  <div id="app">
    <Navbar />
    <main class="main-container">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import Navbar from './components/Navbar.vue'
import { useUserStore } from './stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUser()
  }
})
</script>

<style scoped>
.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>
