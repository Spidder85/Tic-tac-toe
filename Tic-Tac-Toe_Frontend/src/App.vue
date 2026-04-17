<template>
  <Header />

  <main class="page">
    <router-view />
  </main>
</template>

<script>
import Header from './components/Header.vue'

export default {
  name: 'App',

  components: {
    Header
  }
}
</script>
