package com.origamilabs.orii.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire central de la connexion Bluetooth à la bague ORII et des communications GAIA.
 * Version mise à jour pour Android 14 (API 34) et compatible avec API 21+
 */
@Singleton
class OriiBluetoothManager @Inject constructor(
    private val context: Context,
    private val gaiaManager: GaiaManager
) {
    // Récupération de l'adaptateur Bluetooth via BluetoothManager en utilisant la méthode compatible API 21+
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    // UUID du service SPP (ou GAIA) de la bague ORII. Utilisation de SPP par défaut.
    private val oriiUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Établit la connexion Bluetooth avec la bague ORII.
     * (Exécuté en background via coroutine)
     */
    @Throws(IOException::class)
    suspend fun connect(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            // Vérifier que la permission BLUETOOTH_CONNECT est accordée (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Permission BLUETOOTH_CONNECT non accordée")
            }

            // Vérifier que l'adaptateur Bluetooth est disponible
            val bluetoothAdapter = adapter ?: throw IllegalStateException("Bluetooth adapter non disponible")

            // Récupérer l’appareil Bluetooth via son adresse MAC
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

            // Création du socket RFCOMM (profil SPP ou spécifique GAIA)
            socket = device.createRfcommSocketToServiceRecord(oriiUuid)
            try {
                socket?.connect()  // tentative de connexion (bloquant)
            } catch (e: IOException) {
                socket?.close()
                socket = null
                throw e  // propagation de l’erreur de connexion
            }
            // Préparation des flux de communication en cas de succès
            socket?.let {
                inputStream = it.inputStream
                outputStream = it.outputStream
            }
        }
    }

    /**
     * Envoi d’une commande GAIA à la bague via le socket connecté.
     */
    suspend fun sendGaiaCommand(commandId: Int, payload: ByteArray? = null) {
        withContext(Dispatchers.IO) {
            val packet: ByteArray = gaiaManager.createCommandPacket(commandId, payload)
            outputStream?.write(packet)
            // La lecture de la réponse peut être effectuée dans une coroutine séparée si nécessaire
        }
    }

    /**
     * Déconnecte et nettoie le socket Bluetooth.
     */
    fun disconnect() {
        try {
            socket?.close()
        } catch (_: IOException) { }
        socket = null
        inputStream = null
        outputStream = null
    }
}
