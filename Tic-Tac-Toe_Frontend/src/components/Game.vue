<template>
  <section class="game" v-if="game">
    <div class="game__info">
      <p class="game__status">
        {{ statusText }}
      </p>

      <p class="game__marker" v-if="currentUserMarkerText">
        Ваш знак: {{ currentUserMarkerText }}
      </p>
    </div>

    <GameBoard
      :cells="game.gameField.cells"
      :disabled="!canMove"
      @cell-click="makeMove"
    />

    <p v-if="errorMessage" class="error">
      {{ errorMessage }}
    </p>
  </section>
</template>

<script>
import GameBoard from './GameBoard.vue'
import { MARKERS, getMarkerText } from '../models/markers.js'
import {
  buildGameWithMove,
  canCurrentUserMove,
  getPlayerMarker,
  getStatusText
} from '../services/gameViewService.js'

export default {
  name: 'Game',

  components: {
    GameBoard
  },

  props: {
    game: {
      type: Object,
      required: true
    },

    userId: {
      type: String,
      required: true
    },

    opponentLogin: {
      type: String,
      default: ''
    }
  },

  emits: ['move'],

  data() {
    return {
      errorMessage: ''
    }
  },

  computed: {
    canMove() {
      return canCurrentUserMove(this.game, this.userId)
    },

    currentUserMarker() {
      return getPlayerMarker(this.game, this.userId)
    },

    currentUserMarkerText() {
      return getMarkerText(this.currentUserMarker)
    },

    statusText() {
      return getStatusText(this.game, this.userId, this.opponentLogin)
    }
  },

  methods: {
    makeMove(cell) {
      this.errorMessage = ''

      if (!this.canMove) {
        this.errorMessage = 'Сейчас не ваш ход'
        return
      }

      if (this.currentUserMarker === MARKERS.EMPTY) {
        this.errorMessage = 'Пользователь не является участником игры'
        return
      }

      const nextGame = buildGameWithMove(
        this.game,
        cell.rowIndex,
        cell.columnIndex,
        this.currentUserMarker
      )

      this.$emit('move', nextGame)
    }
  }
}
</script>
