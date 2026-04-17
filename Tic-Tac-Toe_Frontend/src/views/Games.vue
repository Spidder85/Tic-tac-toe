<template>
    <section class="games-page">
        <div class="actions">
            <button type="button" @click="goToCreateGame">
                Создать игру
            </button>

            <button type="button" @click="logout">
                Выход
            </button>
        </div>

        <p v-if="loading">
            Загрузка игр...
        </p>

        <p v-if="errorMessage" class="error">
            {{ errorMessage }}
        </p>

        <p v-if="!loading && games.length === 0">
            Доступных игр нет
        </p>

        <ul v-if="games.length > 0" class="games-list">
            <li
              v-for="game in games"
              :key="game.id"
              class="games-list__item"
            >
              <button
                type="button"
                class="games-list__button"
                @click="joinGame(game.id)"
              >
                <span class="games-list__id">
                    Игра: {{ game.id }}
                </span>

                <span>
                    Создатель: {{ getCreatorLogin(game.firstPlayerId) }}
                </span>
              </button>
            </li>
        </ul>
    </section>
</template>

<script>
import { getErrorMessage } from '../services/api.js'

export default {
    name: 'Games',

    data() {
        return {
            loading: false,
            errorMessage: ''
        }
    },

    computed: {
        games() {
            return this.$store.state.games
        },

        usersById() {
            return this.$store.state.usersById
        }
    },
    
    async created() {
        await this.loadGames()
    },

    methods: {
        async loadGames() {
            this.loading = true
            this.errorMessage = ''

            try {
                await this.$store.dispatch('loadGames')
            } catch (error) {
                this.errorMessage = getErrorMessage(error, 'Ошибка загрузки игр')
            } finally {
                this.loading = false
            }
        },

        async joinGame(gameId) {
            this.errorMessage = ''

            try {
                const game = await this.$store.dispatch('joinGame', gameId)
                this.$router.push(`/games/${game.id}`)
            } catch (error) {
                this.errorMessage = getErrorMessage(error, 'Ошибка присоединения к игре')
            }
        },

        getCreatorLogin(userId) {
            return this.usersById[userId]?.login || userId
        },

        goToCreateGame() {
            this.$router.push('/games/create')
        },

        logout() {
            this.$store.dispatch('logout')
            this.$router.push('/login')
        }
    }
}
</script>
