package com.origamilabs.orii.core.bluetooth.connection

import android.app.Activity

/**
 * Interface unique pour demander la permission BLUETOOTH_CONNECT
 * (ou autres permissions si besoin).
 */
interface PermissionRequestDelegate {
    /**
     * Retourne l'Activity hôte qui va lancer la demande de permission.
     */
    fun getHostActivity(): Activity?

    /**
     * Méthode à implémenter dans l’Activity (ou le Fragment)
     * pour réellement lancer ActivityCompat.requestPermissions()
     * ou bien un ActivityResultLauncher.
     */
    fun requestBluetoothPermission(requestCode: Int)
}
