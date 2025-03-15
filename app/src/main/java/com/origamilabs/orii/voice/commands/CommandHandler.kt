package com.origamilabs.orii.voice.commands

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val setAlarmCommand: SetAlarmCommand,
    private val bluetoothCommandManager: BluetoothCommandManager
) {
    fun handleCommand(command: String, activityContext: Context, speak: (String) -> Unit) {
        if (isInternetAvailable()) {
            lifecycleScope.launch {
                val response = ChatGPTApiClient.sendRequest(command)
                speak(response)
            }
        } else {
            when {
                command.contains("alarme", ignoreCase = true) -> setAlarmCommand.scheduleAlarm(context, command, speak)
                command.contains("heure", ignoreCase = true) -> GetTimeCommand.execute(context, speak)
                command.startsWith("envoyer sms", ignoreCase = true) -> SendSmsCommand.execute(command, bluetoothCommandManager, speak)
                else -> DefaultCommand.execute(command, speak)
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}