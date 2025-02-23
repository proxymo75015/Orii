package com.origamilabs.orii.models

class Alert(var ledColor: Int, var vibration: Int) {

    fun changeLedColor(newValue: Int) {
        var value = newValue
        if (value > 5) {
            value -= 6
        }
        if (value < 0) {
            value += 6
        }
        ledColor = value
    }

    fun changeVibration(newValue: Int) {
        var value = newValue
        if (value > 4) {
            value -= 5
        }
        if (value < 0) {
            value += 5
        }
        vibration = value
    }
}
