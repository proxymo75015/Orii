package com.origamilabs.orii.db

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.models.User
import com.origamilabs.orii.models.enum.CustomCommandAction

object SharedPreferences {
    const val CURRENT_USER = "current_user"
    const val GESTURE_FLAT_TRIPLE_TAP_ACTION = "gesture_flat_triple_tap_action_1"
    const val GESTURE_FLAT_TRIPLE_TAP_WEB_HOOK_URL = "gesture_flat_triple_tap_web_hook_url"
    const val GESTURE_MODE = "gesture_mode"
    const val GESTURE_REVERSE_DOUBLE_TAP_ACTION = "gesture_reverse_double_tap_action_1"
    const val GESTURE_REVERSE_DOUBLE_TAP_WEB_HOOK_URL = "gesture_reverse_double_tap_web_hook_url"
    const val MIC_MODE = "mic_mode"
    private const val PREFERENCE_FILE_KEY = "com.origamilabs.orii.db"
    const val READOUT_SPEED = "readout_speed"
    const val SENSITIVITY_OF_GESTURE = "sensitivity_of_gesture"
    const val TAG = "SharedPreferences"
    const val UUID = "uuid"

    // Contexte de l'application (initialisé via init)
    lateinit var applicationContext: Context

    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
    }

    fun getSharedPreferences(): SharedPreferences =
        applicationContext.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    fun setReadoutSpeed(speed: Float) {
        getSharedPreferences().edit().putFloat(READOUT_SPEED, speed).apply()
    }

    fun getReadoutSpeed(): Float =
        getSharedPreferences().getFloat(READOUT_SPEED, 1.0f)

    fun setMicMode(mode: Int) {
        if (getMicMode() != mode) {
            getSharedPreferences().edit().putInt(MIC_MODE, mode).apply()
        }
        // On appelle la commande si ORii est connecté
        if (ConnectionManager.getInstance().isOriiConnected()) {
            CommandManager.getInstance().putCallSwitchMicModeTask(mode)
        }
    }

    fun getMicMode(): Int =
        getSharedPreferences().getInt(MIC_MODE, 1)

    fun setGestureMode(mode: Int) {
        if (getGestureMode() != mode) {
            getSharedPreferences().edit().putInt(GESTURE_MODE, mode).apply()
        }
    }

    fun getGestureMode(): Int =
        getSharedPreferences().getInt(GESTURE_MODE, 0)

    fun setFlatTripleTapAction(action: CustomCommandAction) {
        if (getFlatTripleTapAction() != action) {
            getSharedPreferences().edit().putInt(GESTURE_FLAT_TRIPLE_TAP_ACTION, action.ordinal).apply()
        }
    }

    fun getFlatTripleTapAction(): CustomCommandAction =
        CustomCommandAction.values()[getSharedPreferences().getInt(GESTURE_FLAT_TRIPLE_TAP_ACTION, 0)]

    fun setReverseDoubleTapAction(action: CustomCommandAction) {
        if (getReverseDoubleTapAction() != action) {
            getSharedPreferences().edit().putInt(GESTURE_REVERSE_DOUBLE_TAP_ACTION, action.ordinal).apply()
        }
    }

    fun getReverseDoubleTapAction(): CustomCommandAction =
        CustomCommandAction.values()[getSharedPreferences().getInt(GESTURE_REVERSE_DOUBLE_TAP_ACTION, 0)]

    fun getFlatTripleTapWebHookUrl(): String =
        getSharedPreferences().getString(GESTURE_FLAT_TRIPLE_TAP_WEB_HOOK_URL, "") ?: ""

    fun setFlatTripleTapWebHookUrl(url: String) {
        if (getFlatTripleTapWebHookUrl() != url) {
            getSharedPreferences().edit().putString(GESTURE_FLAT_TRIPLE_TAP_WEB_HOOK_URL, url).apply()
        }
    }

    fun getReverseDoubleTapWebHookUrl(): String =
        getSharedPreferences().getString(GESTURE_REVERSE_DOUBLE_TAP_WEB_HOOK_URL, "") ?: ""

    fun setReverseDoubleTapWebHookUrl(url: String) {
        if (getReverseDoubleTapWebHookUrl() != url) {
            getSharedPreferences().edit().putString(GESTURE_REVERSE_DOUBLE_TAP_WEB_HOOK_URL, url).apply()
        }
    }

    fun setSensitivityOfGesture(sensitivity: Int) {
        if (getSensitivityOfGesture() != sensitivity) {
            getSharedPreferences().edit().putInt(SENSITIVITY_OF_GESTURE, sensitivity).apply()
        }
    }

    fun getSensitivityOfGesture(): Int =
        getSharedPreferences().getInt(SENSITIVITY_OF_GESTURE, 0)

    fun setUser(user: User) {
        val json = Gson().toJson(user)
        getSharedPreferences().edit().putString(CURRENT_USER, json).apply()
    }

    fun getUser(): User? {
        val json = getSharedPreferences().getString(CURRENT_USER, null)
        return if (json != null) Gson().fromJson(json, User::class.java) else null
    }

    fun setUuid(uuid: String) {
        getSharedPreferences().edit().putString(UUID, uuid).apply()
    }

    fun getUuid(): String? =
        getSharedPreferences().getString(UUID, null)
}
