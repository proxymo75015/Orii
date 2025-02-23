package com.origamilabs.orii.core

import android.os.ParcelUuid

object Constants {
    val A2DP_SINK_PROFILE_UUIDS: Array<ParcelUuid> = arrayOf(
        ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB"),
        ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB")
    )

    val A2DP_SRC_PROFILE_UUIDS: Array<ParcelUuid> = arrayOf(
        ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB")
    )

    val HEADSET_PROFILE_UUIDS: Array<ParcelUuid> = arrayOf(
        ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB"),
        ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB")
    )

    var ORII_CLOSE_NOTIFICATION_TIME: Long = 43200000
    var ORII_NOTIFICATION_CONNECTION_STATE: Int = 1
    var ORII_NOTIFICATION_LOW_BATTERY: Int = 3
    var ORII_NOTIFICATION_PUSH: Int = 4
    var ORII_NOTIFICATION_UPDATE: Int = 2
}
