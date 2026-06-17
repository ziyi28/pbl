<template>
  <div id="app">
    <Navbar />
    <main class="main-container">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
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
#app {
  display: flex;
  flex-direction: column;
}

.main-container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  /* navbar height 56px + 24px breathing room */
  padding: 80px 20px 40px;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* Route Transition Animations */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
