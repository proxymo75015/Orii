package com.origamilabs.orii.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
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
 * (Remplace les singletons statiques précédents pour Bluetooth)
 */
@Singleton
class OriiBluetoothManager @Inject constructor(
    private val context: Context,
    private val gaiaManager: GaiaManager
) {
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    // UUID du service SPP (ou GAIA) de la bague ORII. Utilisation de SPP par défaut.
    private val ORII_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /** Établir la connexion Bluetooth avec la bague (opération effectuée en background via coroutine). */
    @Throws(IOException::class)
    suspend fun connect(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            // Récupère l’appareil Bluetooth par adresse MAC
            val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
            // Création du socket RFCOMM (profil SPP ou spécifique GAIA)
            socket = device.createRfcommSocketToServiceRecord(ORII_UUID)
            try {
                socket?.connect()  // tentative de connexion (bloquant)
            } catch (e: IOException) {
                socket?.close()
                socket = null
                throw e  // propagation de l’erreur de connexion
            }
            // Si la connexion réussit, on prépare les flux de communication
            socket?.let {
                inputStream = it.inputStream
                outputStream = it.outputStream
            }
        }
    }

    /** Envoi d’une commande GAIA à la bague via le socket connecté. */
    suspend fun sendGaiaCommand(commandId: Int, payload: ByteArray? = null) {
        withContext(Dispatchers.IO) {
            val packet: ByteArray = gaiaManager.createCommandPacket(commandId, payload)
            outputStream?.write(packet)
            // (La réponse pourra être lue via inputStream dans une coroutine séparée si besoin)
        }
    }

    /** Déconnexion et nettoyage du socket Bluetooth. */
    fun disconnect() {
        try {
            socket?.close()
        } catch (_: IOException) { }
        socket = null
        inputStream = null
        outputStream = null
    }

    // (Éventuellement, on pourrait ajouter une fonction pour écouter les réponses de la bague
    // en utilisant inputStream et gaiaManager.parseResponsePacket, et poster des événements.)
}
