package com.origamilabs.orii

import com.origamilabs.orii.models.Application

object Constants {
    const val ACTION_INCOMING_CALL_RECEIVED = "com.origamilabs.orii.ACTION_INCOMING_CALL_RECEIVED"
    const val ACTION_NOTIFICATION_RECEIVED = "com.origamilabs.orii.ACTION_NOTIFICATION_RECEIVED"
    const val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    const val APP_VERSION_INFO_DOWNLOADED_BROADCAST = "APP_VERSION_INFO_DOWNLOADED_BROADCAST"
    const val DEV_APP_VERSION_INFORMATION_URL =
        "https://firebasestorage.googleapis.com/v0/b/orii-dev-8695f.appspot.com/o/app_version_info%2Fdev_orii_version_info.json?alt=media&token=0867182d-998a-4d54-922a-998cca9bca77"
    const val FIRMWARE_DOWNLOADED_BROADCAST = "FIRMWARE_DOWNLOADED_BROADCAST"
    const val FIRMWARE_FORCE_DOWNLOADED_BROADCAST = "FIRMWARE_FORCE_DOWNLOADED_BROADCAST"
    const val GOOGLE_ID_TOKEN_DEV =
        "853565450082-m1ks3vjhta5nmkup01f5gin9ng8r4rhr.apps.googleusercontent.com"
    const val GOOGLE_ID_TOKEN_PRO =
        "374563545903-ogpccvh50b571mm39bgsjecjbkv1r967.apps.googleusercontent.com"
    const val PUBLIC_APP_VERSION_INFORMATION_URL =
        "https://firebasestorage.googleapis.com/v0/b/orii-203005.appspot.com/o/app_version_info%2Fpublic_orii_version_info.json?alt=media&token=331b9a3c-3da0-4718-b6b4-fffc6b884635"
    const val REQUEST_GOOGLE_SIGN_IN = 3
    const val REQUEST_PERMISSIONS = 1
    const val REQUEST_PICK_CONTACT = 2

    val SUPPORTED_APPS: ArrayList<Application> = arrayListOf(
        Application("phonecall", 1, 0, "Phone Call"),
        Application("sms", 2, 1, "SMS"),
        Application("com.whatsapp", 3, 2, "WhatsApp"),
        Application("jp.naver.line.android", 4, 3, "Line"),
        Application("com.facebook.orca", 5, 4, "Messenger"),
        Application("com.tencent.mm", 3, 1, "WeChat")
    )
}
