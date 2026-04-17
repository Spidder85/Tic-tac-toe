<template>
  <div class="game-board">
    <button
      v-for="cell in flatCells"
      :key="cell.key"
      type="button"
      class="game-board__cell"
      :disabled="disabled || cell.value !== MARKERS.EMPTY"
      @click="selectCell(cell.rowIndex, cell.columnIndex)"
    >
      {{ getMarkerText(cell.value) }}
    </button>
  </div>
</template>

<script>
import { MARKERS, getMarkerText } from '../models/markers.js'

export default {
  name: 'GameBoard',

  props: {
    cells: {
      type: Array,
      required: true
    },

    disabled: {
      type: Boolean,
      default: false
    }
  },

  emits: ['cell-click'],

  computed: {
    MARKERS() {
      return MARKERS
    },

    flatCells() {
      return this.cells.flatMap((row, rowIndex) =>
        row.map((value, columnIndex) => ({
          key: `${rowIndex}-${columnIndex}`,
          rowIndex,
          columnIndex,
          value
        }))
      )
    }
  },

  methods: {
    getMarkerText,

    selectCell(rowIndex, columnIndex) {
      this.$emit('cell-click', {
        rowIndex,
        columnIndex
      })
    }
  }
}
</script>
