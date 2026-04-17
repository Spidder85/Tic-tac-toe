export const MARKERS = {
    EMPTY: 0,
    FIRST_PLAYER: 1,
    SECOND_PLAYER: 2
}

export function getMarkerText(marker) {
    if (marker === MARKERS.FIRST_PLAYER) {
        return 'X'
    }

    if (marker === MARKERS.SECOND_PLAYER) {
        return 'O'
    }

    return ''
}
