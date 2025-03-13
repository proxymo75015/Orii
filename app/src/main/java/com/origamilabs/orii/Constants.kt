package com.origamilabs.orii

import com.origamilabs.orii.models.Application

object Constants {
    const val ACTION_INCOMING_CALL_RECEIVED = "com.origamilabs.orii.ACTION_INCOMING_CALL_RECEIVED"
    const val ACTION_NOTIFICATION_RECEIVED = "com.origamilabs.orii.ACTION_NOTIFICATION_RECEIVED"
    const val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    const val APP_VERSION_INFO_DOWNLOADED_BROADCAST = "APP_VERSION_INFO_DOWNLOADED_BROADCAST"

    // Ajoutez cette constante pour corriger l'erreur Unresolved reference: ORII_NOTIFICATION_LOW_BATTERY
    const val ORII_NOTIFICATION_LOW_BATTERY = 1001

    val SUPPORTED_APPS: ArrayList<Application> = arrayListOf(
        Application("phonecall", 1, 0, "Phone Call"),
        Application("sms", 2, 1, "SMS"),
        Application("com.whatsapp", 3, 2, "WhatsApp"),
        Application("jp.naver.line.android", 4, 3, "Line"),
        Application("com.facebook.orca", 5, 4, "Messenger"),
        Application("com.tencent.mm", 3, 1, "WeChat")
    )
}
