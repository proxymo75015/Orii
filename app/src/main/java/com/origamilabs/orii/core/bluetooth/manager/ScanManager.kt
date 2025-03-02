package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ScanManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {

    private val TAG = "ScanManager"
    private val scanPeriod = 30000L // 30 secondes

    /**
     * Lance le scan Bluetooth pour trouver un appareil ORII.
     * Si un appareil pré-appairé est trouvé, il est retourné immédiatement.
     * Sinon, un scan est effectué avec un timeout de 30 secondes.
     *
     * @return L'appareil Bluetooth trouvé ou null en cas de timeout.
     */
    suspend fun scanForOrii(): BluetoothDevice? {
        // Vérifie si un appareil pré-appairé correspondant existe déjà
        findBondedOrii()?.let {
            Log.d(TAG, "Appareil pré-appairé trouvé: ${it.address}")
            return it
        }

        // Lancement du scan avec un timeout
        return withTimeoutOrNull(scanPeriod) {
            suspendCancellableCoroutine<BluetoothDevice> { cont ->
                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        super.onScanResult(callbackType, result)
                        val device = result.device
                        Log.d(TAG, "Appareil scanné: ${result.scanRecord?.deviceName}:${device.address}")
                        if (BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                            Log.d(TAG, "Appareil ORII trouvé")
                            bluetoothAdapter.bluetoothLeScanner?.stopScan(this)
                            if (cont.isActive) {
                                cont.resume(device)
                            }
                        }
                    }

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        if (cont.isActive) {
                            cont.resumeWithException(RuntimeException("Échec du scan avec le code d'erreur $errorCode"))
                        }
                    }
                }

                bluetoothAdapter.bluetoothLeScanner?.startScan(
                    null,
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build(),
                    scanCallback
                )

                // Arrêter le scan si la coroutine est annulée
                cont.invokeOnCancellation {
                    bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
                }
            }
        }
    }

    /**
     * Recherche parmi les appareils appairés un appareil correspondant aux critères ORII.
     */
    private fun findBondedOrii(): BluetoothDevice? {
        val bondedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
        Log.d(TAG, "Nombre d'appareils appairés: ${bondedDevices.size}")
        return bondedDevices.firstOrNull { device ->
            BluetoothHelper.isOriiMacAddressInRange(device.address)
        }
    }
}
