package com.origamilabs.orii.core.bluetooth.manager

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ScanManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : IManager {

    private val tag = "ScanManager"
    private val scanPeriod = 30000L

    suspend fun scanForOrii(): BluetoothDevice? {
        // Retourne immédiatement le périphérique pré-appairé s'il existe.
        findBondedOrii()?.let {
            Timber.d("Périphérique pré-appairé trouvé : ${it.address}")
            return it
        }

        return withTimeoutOrNull(scanPeriod) {
            suspendCancellableCoroutine<BluetoothDevice> { cont ->
                // Vérifie que la permission BLUETOOTH_SCAN est accordée
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                    cont.resumeWith(Result.failure(SecurityException("Permission BLUETOOTH_SCAN non accordée")))
                    return@suspendCancellableCoroutine
                }
                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        val device = result.device
                        Timber.d("Périphérique scanné : ${result.scanRecord?.deviceName}:${device.address}")
                        if (BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                            Timber.d("Périphérique ORII trouvé")
                            try {
                                bluetoothAdapter.bluetoothLeScanner?.stopScan(this)
                            } catch (e: SecurityException) {
                                Timber.e(e, "Erreur lors de l'arrêt du scan")
                            }
                            if (cont.isActive) {
                                cont.resume(device)
                            }
                        }
                    }
                    override fun onScanFailed(errorCode: Int) {
                        if (cont.isActive) {
                            cont.resumeWith(Result.failure(RuntimeException("Échec du scan avec le code d'erreur $errorCode")))
                        }
                    }
                }
                try {
                    bluetoothAdapter.bluetoothLeScanner?.startScan(
                        null,
                        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build(),
                        scanCallback
                    )
                } catch (e: SecurityException) {
                    if (cont.isActive) {
                        cont.resumeWith(Result.failure(e))
                    }
                    return@suspendCancellableCoroutine
                }
                cont.invokeOnCancellation {
                    try {
                        bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
                    } catch (e: SecurityException) {
                        Timber.e(e, "Erreur lors de l'arrêt du scan en cas d'annulation")
                    }
                }
            }
        }
    }

    private fun findBondedOrii(): BluetoothDevice? {
        // Vérifie la permission BLUETOOTH_CONNECT avant d'accéder aux périphériques appairés
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée")
            return null
        }
        val bondedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
        Timber.d("Nombre de périphériques appairés : ${bondedDevices.size}")
        return bondedDevices.firstOrNull { device ->
            BluetoothHelper.isOriiMacAddressInRange(device.address)
        }
    }

    override fun initialize(): Boolean {
        // Initialisation complémentaire si nécessaire
        return true
    }

    override fun start() {
        // ScanManager n'a pas de démarrage continu par défaut.
    }

    override fun close() {
        // Nettoyage optionnel.
    }
}
