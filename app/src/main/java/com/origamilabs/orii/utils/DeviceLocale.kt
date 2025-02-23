package com.origamilabs.orii.utils

import android.util.Log
import java.util.Locale

object DeviceLocale {
    private const val TAG = "DeviceLocale"

    fun getDeviceLocale(): Locale {
        var locale = Locale.getDefault()
        if (locale.language.equals("zh", ignoreCase = true) &&
            locale.country.equals("HK", ignoreCase = true)) {
            locale = Locale("yue", "HK")
        }
        Log.d(TAG, "Selected locale: $locale")
        Log.d(TAG, "Selected language: ${locale.language}")
        return locale
    }
}
