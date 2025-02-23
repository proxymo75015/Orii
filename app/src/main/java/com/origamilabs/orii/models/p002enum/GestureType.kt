package com.origamilabs.orii.models.`enum`

/**
 * Enumération représentant les types de gestes et leur valeur binaire associée.
 *
 * @property bit La valeur binaire associée au geste.
 */
enum class GestureType(val bit: Int) {
    CALL_CONTROL(64),
    FLAT_TRIPLE_TAP(32),
    REVERSE_DOUBLE_TAP(16),
    SIDE_DOUBLE_TAP(8),
    FLAT_DOUBLE_TAP(4),
    DOWN_DOUBLE_TAP(2),
    UP_DOUBLE_TAP(1)
}
