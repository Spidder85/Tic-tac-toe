<template>
  <section class="game-create-page">
    <div class="actions">
      <button
        type="button"
        :disabled="loading"
        @click="createGameWithComputer"
      >
        Создать игру с компьютером
      </button>

      <button
        type="button"
        :disabled="loading"
        @click="createGameWithPlayer"
      >
        Создать игру с другим игроком
      </button>
    </div>

    <p v-if="loading">
      Создание игры...
    </p>

    <p v-if="errorMessage" class="error">
      {{ errorMessage }}
    </p>
  </section>
</template>

<script>
import { getErrorMessage } from '../services/api.js'

export default {
  name: 'GameCreate',

  data() {
    return {
      loading: false,
      errorMessage: ''
    }
  },

  methods: {
    async createGame(computerOpponent) {
      this.loading = true
      this.errorMessage = ''

      try {
        const game = await this.$store.dispatch('createGame', computerOpponent)
        this.$router.push(`/games/${game.id}`)
      } catch (error) {
        this.errorMessage = getErrorMessage(error, 'Ошибка создания игры')
      } finally {
        this.loading = false
      }
    },

    createGameWithComputer() {
      this.createGame(true)
    },

    createGameWithPlayer() {
      this.createGame(false)
    }
  }
}
</script>
