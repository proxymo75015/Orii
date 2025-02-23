package com.origamilabs.orii.models

import com.origamilabs.orii.models.Alert
import kotlin.jvm.internal.Intrinsics

open class Alert(var ledColor: Int, var vibration: Int)

class Application(
    var pid: Int,
    ledColor: Int,
    vibration: Int,
    var personName: String
) : Alert(ledColor, vibration) {

    init {
        Intrinsics.checkParameterIsNotNull(personName, "personName")
    }

    constructor() : this(-1, 0, 0, "")

    fun copy(
        pid: Int = this.pid,
        ledColor: Int = this.ledColor,
        vibration: Int = this.vibration,
        personName: String = this.personName
    ): Application {
        Intrinsics.checkParameterIsNotNull(personName, "personName")
        return Application(pid, ledColor, vibration, personName)
    }

    override fun toString(): String {
        return "Application(pid=$pid, ledColor=$ledColor, vibration=$vibration, personName=$personName)"
    }
}
