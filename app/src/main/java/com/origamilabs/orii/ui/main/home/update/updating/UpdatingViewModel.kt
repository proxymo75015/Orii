package com.origamilabs.orii.ui.main.home.update.updating

import android.content.Context
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.core.app.NotificationCompat
import com.facebook.login.widget.ToolTipPopup
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.manager.UpdateManager
import com.origamilabs.orii.models.FirmwareVersionInfo
import com.origamilabs.orii.ui.MainApplication

/**
 * ViewModel pour la mise à jour OTA.
 *
 * Il gère la progression de la mise à jour et notifie la fin de celle-ci via des LiveData.
 */
class UpdatingViewModel : ViewModel() {

    // LiveData pour suivre la progression (initialisée à 0)
    val progress: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }

    // LiveData indiquant si la mise à jour est terminée (initialisée à false)
    val updateFinished: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    // Listener pour recevoir les événements de mise à jour
    private val onUpdateListener = object : UpdateManager.OnUpdateListener {
        override fun onUpdateReady() {
            UpdateManager.INSTANCE.startUpdate()
        }

        override fun onProgressChanged(progress: Int) {
            this@UpdatingViewModel.progress.postValue(progress)
        }

        override fun onUpdateFinished() {
            updateFinished.postValue(true)
            UpdateManager.INSTANCE.setOnUpdateListener(null)
            AnalyticsManager.INSTANCE.logOtaUpdating(AnalyticsManager.ActionState.END)
        }
    }

    /**
     * Démarre le processus de mise à jour OTA.
     *
     * - Enregistre l'état de démarrage dans Analytics.
     * - Déconnecte la connexion BLE.
     * - Après un délai, initialise la mise à jour avec l'adresse orii et le numéro de version.
     */
    fun startUpdate() {
        AnalyticsManager.INSTANCE.logOtaUpdating(AnalyticsManager.ActionState.START)
        ConnectionManager.getInstance().disconnectBle()

        // Délai avant l'initialisation de la mise à jour (en millisecondes)
        Handler().postDelayed({
            val updateManager = UpdateManager.INSTANCE
            val applicationContext: Context = MainApplication.instance.applicationContext
            val connectionManager = ConnectionManager.getInstance()
            val oriiAddress = connectionManager.oriiAddress
            // Vérification : si le firmware n'est pas disponible, une exception sera levée
            val firmwareVersionInfo: FirmwareVersionInfo =
                AppManager.INSTANCE.getFirmwareVersionInfo() ?: throw NullPointerException("FirmwareVersionInfo is null")
            val versionNumber = firmwareVersionInfo.versionNumber
            updateManager.init(applicationContext, oriiAddress, versionNumber, onUpdateListener)
        }, ToolTipPopup.DEFAULT_POPUP_DISPLAY_TIME.toLong())
    }

    /**
     * Arrête le processus de mise à jour OTA.
     */
    fun stopUpdate() {
        UpdateManager.INSTANCE.stopUpdate()
    }
}
