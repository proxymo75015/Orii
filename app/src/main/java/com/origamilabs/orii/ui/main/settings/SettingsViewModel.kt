package com.origamilabs.orii.ui.main.settings

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.enum.CustomCommandAction
import com.origamilabs.orii.models.enum.GestureType
import com.origamilabs.orii.utils.toBinaryString
import com.origamilabs.orii.utils.toBoolean
import com.origamilabs.orii.utils.toDigitInt
import com.origamilabs.orii.utils.toInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @androidx.annotation.ApplicationContext private val context: Context
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val MAXIMUM_GESTURE_MODE = 127
        const val MINIMUM_GESTURE_MODE = 0
        const val TAG = "SettingsViewModel"
    }

    private var appGestureMode: Int = 0

    val micMode = MutableLiveData<Boolean>()
    val upDoubleTapMode = MutableLiveData<Boolean>()
    val downDoubleTapMode = MutableLiveData<Boolean>()
    val flatDoubleTapMode = MutableLiveData<Boolean>()
    val sideDoubleTapMode = MutableLiveData<Boolean>()
    val flatThreeTapMode = MutableLiveData<Boolean>()
    val reverseDoubleTapMode = MutableLiveData<Boolean>()
    val callControlMode = MutableLiveData<Boolean>()
    val sensitivity = MutableLiveData<Int>()
    val showFlatTripleTapWebHook = MutableLiveData<Boolean>()
    val showReverseDoubleTapWebHook = MutableLiveData<Boolean>()

    init {
        // Enregistrement de l'écouteur des changements de préférence
        AppManager.INSTANCE.sharedPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this)
        initLiveDataFromSharedPreferences()
    }

    private fun initLiveDataFromSharedPreferences() {
        micMode.postValue(AppManager.INSTANCE.sharedPreferences.getMicMode().toBoolean())
        sensitivity.postValue(AppManager.INSTANCE.sharedPreferences.getSensitivityOfGesture())
        initAppGestureMode()
        showFlatTripleTapWebHook.postValue(
            AppManager.INSTANCE.sharedPreferences.getFlatTripleTapAction() == CustomCommandAction.WEB_HOOK
        )
        showReverseDoubleTapWebHook.postValue(
            AppManager.INSTANCE.sharedPreferences.getReverseDoubleTapAction() == CustomCommandAction.WEB_HOOK
        )
    }

    private fun initAppGestureMode() {
        appGestureMode = AppManager.INSTANCE.sharedPreferences.getGestureMode()
        updateGestureMode(appGestureMode)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == com.origamilabs.orii.db.SharedPreferences.MIC_MODE) {
            checkMicMode(AppManager.INSTANCE.sharedPreferences.getMicMode())
        }
    }

    override fun onCleared() {
        AppManager.INSTANCE.sharedPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onCleared()
    }

    private fun checkMicMode(ringMicMode: Int) {
        val currentMicModeValue = micMode.value ?: false
        if (currentMicModeValue.toInt() != ringMicMode) {
            viewModelScope.launch {
                CommandManager.getInstance().putCallSwitchMicModeTask(currentMicModeValue.toInt())
            }
        }
    }

    private fun checkGestureMode(ringGestureMode: Int) {
        if (ringGestureMode != appGestureMode) {
            viewModelScope.launch {
                CommandManager.getInstance().putCallChangeGestureModeTask(appGestureMode)
            }
        }
    }

    fun setMicMode(mic: MicMode) {
        if (ConnectionManager.getInstance().isOriiConnected()) {
            if (AppManager.INSTANCE.sharedPreferences.getMicMode() != mic.ordinal) {
                micMode.postValue(mic.ordinal.toBoolean())
                viewModelScope.launch {
                    CommandManager.getInstance().putCallSwitchMicModeTask(mic.ordinal)
                }
            }
        } else {
            Toast.makeText(context, R.string.havent_connected_to_orii, Toast.LENGTH_SHORT).show()
        }
    }

    fun setGestureMode(newAppGestureMode: Int) {
        if (ConnectionManager.getInstance().isOriiConnected()) {
            appGestureMode = newAppGestureMode
            viewModelScope.launch {
                CommandManager.getInstance().putCallChangeGestureModeTask(newAppGestureMode)
            }
            updateGestureMode(newAppGestureMode)
        }
    }

    fun checkGesturesModeAreOn(): Boolean = (appGestureMode == MAXIMUM_GESTURE_MODE)
    fun checkGesturesModeAreOff(): Boolean = (appGestureMode == MINIMUM_GESTURE_MODE)

    private fun updateGestureMode(newAppGestureMode: Int) {
        // Convertit newAppGestureMode en une chaîne binaire d'une longueur égale au nombre de GestureType
        val binaryString = newAppGestureMode.toBinaryString(GestureType.values().size)
        var index = 0
        for (char in binaryString) {
            when (index) {
                0 -> callControlMode.postValue(char.toDigitInt().toBoolean())
                1 -> flatThreeTapMode.postValue(char.toDigitInt().toBoolean())
                2 -> reverseDoubleTapMode.postValue(char.toDigitInt().toBoolean())
                3 -> sideDoubleTapMode.postValue(char.toDigitInt().toBoolean())
                4 -> flatDoubleTapMode.postValue(char.toDigitInt().toBoolean())
                5 -> downDoubleTapMode.postValue(char.toDigitInt().toBoolean())
                6 -> upDoubleTapMode.postValue(char.toDigitInt().toBoolean())
            }
            index++
        }
    }

    fun setSensitivityOfGesture(sensitivityValue: Int) {
        if (ConnectionManager.getInstance().isOriiConnected()) {
            viewModelScope.launch {
                CommandManager.getInstance().putCallChangeSensitivityOfGestureTask(sensitivityValue)
            }
        }
    }

    fun saveReadoutSpeedToPreferences(value: Float) {
        AppManager.INSTANCE.sharedPreferences.setReadoutSpeed(value)
    }

    fun saveFlatTripleTapActionToPreferences(customCommandAction: CustomCommandAction) {
        showFlatTripleTapWebHook.postValue(customCommandAction == CustomCommandAction.WEB_HOOK)
        AppManager.INSTANCE.sharedPreferences.setFlatTripleTapAction(customCommandAction)
    }

    fun getFlatTripleTapActionFromPreferences(): CustomCommandAction {
        return AppManager.INSTANCE.sharedPreferences.getFlatTripleTapAction()
    }

    fun saveReverseDoubleTapActionToPreferences(customCommandAction: CustomCommandAction) {
        showReverseDoubleTapWebHook.postValue(customCommandAction == CustomCommandAction.WEB_HOOK)
        AppManager.INSTANCE.sharedPreferences.setReverseDoubleTapAction(customCommandAction)
    }

    fun getReverseDoubleTapActionFromPreferences(): CustomCommandAction {
        return AppManager.INSTANCE.sharedPreferences.getReverseDoubleTapAction()
    }

    fun saveFlatTripleTapWebHookUrlToPreferences(url: String) {
        AppManager.INSTANCE.sharedPreferences.setFlatTripleTapWebHookUrl(url)
    }

    fun getFlatTripleTapWebHookUrlFromPreferences(): String {
        return AppManager.INSTANCE.sharedPreferences.getFlatTripleTapWebHookUrl()
    }

    fun saveReverseDoubleTapWebHookUrlToPreferences(url: String) {
        AppManager.INSTANCE.sharedPreferences.setReverseDoubleTapWebHookUrl(url)
    }

    fun getReverseDoubleTapWebHookUrlFromPreferences(): String {
        return AppManager.INSTANCE.sharedPreferences.getReverseDoubleTapWebHookUrl()
    }

    enum class MicMode {
        RIGHT, LEFT
    }
