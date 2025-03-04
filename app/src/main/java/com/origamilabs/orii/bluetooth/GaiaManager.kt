package com.origamilabs.orii.bluetooth

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestion du protocole GAIA (Generic Application Interface Architecture) utilisé par la bague ORII.
 * Permet de formater les commandes et de parser les réponses selon le protocole.
 * Les codes de commande utilisés ici sont repris du document "Analyse projet GAIAControl.pdf".
 */
@Singleton
class GaiaManager @Inject constructor() {

    /**
     * Définition des codes de commande GAIA.
     * Les commandes sont regroupées par catégorie selon le document.
     */
    object Command {
        // Commandes de configuration (0x01nn)
        const val SET_RAW_CONFIGURATION           = 0x0100
        const val SET_LED_CONFIGURATION           = 0x0101
        const val SET_TONE_CONFIGURATION          = 0x0102
        const val SET_DEFAULT_VOLUME              = 0x0103
        const val FACTORY_DEFAULT_RESET           = 0x0104
        const val SET_VIBRATOR_CONFIGURATION      = 0x0105
        const val SET_VOICE_PROMPT_CONFIGURATION  = 0x0106
        const val SET_FEATURE_CONFIGURATION       = 0x0107
        const val SET_USER_EVENT_CONFIGURATION    = 0x0108
        const val SET_TIMER_CONFIGURATION         = 0x0109
        const val SET_AUDIO_GAIN_CONFIGURATION    = 0x010A
        const val SET_VOLUME_CONFIGURATION        = 0x010B
        const val SET_POWER_CONFIGURATION         = 0x010C
        const val SET_USER_TONE_CONFIGURATION     = 0x010E
        const val SET_DEVICE_NAME                 = 0x010F
        const val SET_WLAN_CREDENTIALS            = 0x0110
        const val SET_PEER_PERMITTED_ROUTING      = 0x0111
        const val SET_PERMITTED_NEXT_AUDIO_SOURCE = 0x0112
        const val SET_ONE_TOUCH_DIAL_STRING       = 0x0116

        // Commandes de contrôle (0x02nn)
        const val CHANGE_VOLUME                   = 0x0201
        const val DEVICE_RESET                    = 0x0202
        const val SET_PIO_CONTROL                 = 0x0203
        const val SET_POWER_STATE                 = 0x0204
        const val SET_VOLUME_ORIENTATION          = 0x0205
        const val SET_VIBRATOR_CONTROL            = 0x0206
        const val SET_LED_CONTROL                 = 0x0207
        const val FM_CONTROL                      = 0x0208
        const val PLAY_TONE                       = 0x0209
        const val SET_VOICE_PROMPT_CONTROL        = 0x020A
        const val CHANGE_AUDIO_PROMPT_LANGUAGE    = 0x020B
        const val SET_SPEECH_RECOGNITION_CONTROL  = 0x020C
        const val ALERT_LEDS                      = 0x020D
        const val ALERT_TONE                      = 0x020E
        const val ALERT_EVENT                     = 0x0210
        const val ALERT_VOICE                     = 0x0211
        const val SET_AUDIO_PROMPT_LANGUAGE       = 0x0212
        const val START_SPEECH_RECOGNITION        = 0x0213
        const val SET_EQ_CONTROL                  = 0x0214
        const val SET_BASS_BOOST_CONTROL          = 0x0215
        const val SET_3D_ENHANCEMENT_CONTROL      = 0x0216
        const val SWITCH_EQ_CONTROL               = 0x0217
        const val TOGGLE_BASS_BOOST_CONTROL       = 0x0218
        const val TOGGLE_3D_ENHANCEMENT_CONTROL   = 0x0219
        const val SET_EQ_PARAMETER                = 0x021A
        const val SET_EQ_GROUP_PARAMETER          = 0x021B
        const val DISPLAY_CONTROL                 = 0x021C
        const val ENTER_BLUETOOTH_PAIRING_MODE    = 0x021D
        const val SET_AUDIO_SOURCE                = 0x021E
        const val AV_REMOTE_CONTROL               = 0x021F
        const val SET_USER_EQ_CONTROL             = 0x0220
        const val TOGGLE_USER_EQ_CONTROL          = 0x0221
        const val SET_SPEAKER_EQ_CONTROL          = 0x0222
        const val TOGGLE_SPEAKER_EQ_CONTROL       = 0x0223
        const val SET_TWS_AUDIO_ROUTING           = 0x0224
        const val SET_TWS_VOLUME                  = 0x0225
        const val TRIM_TWS_VOLUME                 = 0x0226
        const val SET_PEER_LINK_RESERVED          = 0x0227

        // Commandes de statut ponctuel (0x03nn)
        const val GET_API_VERSION                 = 0x0300
        const val GET_CURRENT_RSSI                = 0x0301
        const val GET_CURRENT_BATTERY_LEVEL       = 0x0302
        const val GET_MODULE_ID                   = 0x0303
        const val GET_APPLICATION_VERSION         = 0x0304
        const val GET_PIO_STATE                   = 0x0306
        const val READ_ADC                        = 0x0307
        const val GET_PEER_ADDRESS                = 0x030A
        const val GET_DFU_STATUS                  = 0x0310
        const val GET_HOST_FEATURE_INFORMATION    = 0x0320

        // Commandes de contrôle de fonctionnalités (0x05nn)
        const val AUTHENTICATE_REQUEST            = 0x0501
        const val AUTHENTICATE_RESPONSE           = 0x0502
        const val SET_FEATURE                     = 0x0503
        const val GET_FEATURE                     = 0x0583
        const val SET_SESSION_ENABLE              = 0x0504
        const val GET_SESSION_ENABLE              = 0x0584

        // Commandes de transfert de données (0x06nn)
        const val DATA_TRANSFER_SETUP             = 0x0601
        const val DATA_TRANSFER_CLOSE             = 0x0602
        const val HOST_TO_DEVICE_DATA             = 0x0603
        const val DEVICE_TO_HOST_DATA             = 0x0604
        const val I2C_TRANSFER                    = 0x0608
        const val GET_STORAGE_PARTITION_STATUS    = 0x0610
        const val OPEN_STORAGE_PARTITION          = 0x0611
        const val OPEN_UART                       = 0x0612
        const val WRITE_STORAGE_PARTITION         = 0x0615
        const val WRITE_STREAM                    = 0x0617
        const val CLOSE_STORAGE_PARTITION         = 0x0618
        const val MOUNT_STORAGE_PARTITION         = 0x061A
        const val GET_FILE_STATUS                 = 0x0620
        const val OPEN_FILE                       = 0x0621
        const val READ_FILE                       = 0x0624
        const val CLOSE_FILE                      = 0x0628
        const val DFU_REQUEST                     = 0x0630
        const val DFU_BEGIN                       = 0x0631
        const val DFU_WRITE                       = 0x0632
        const val DFU_COMMIT                      = 0x0633
        const val DFU_GET_RESULT                  = 0x0634
        const val VM_UPGRADE_CONNECT              = 0x0640
        const val VM_UPGRADE_DISCONNECT           = 0x0641
        const val VM_UPGRADE_CONTROL              = 0x0642
        const val VM_UPGRADE_DATA                 = 0x0643

        // Commandes de débogage (0x07nn)
        const val NO_OPERATION                    = 0x0700
        const val GET_DEBUG_FLAGS                 = 0x0701
        const val SET_DEBUG_FLAGS                 = 0x0702
        const val RETRIEVE_PS_KEY                 = 0x0710
        const val RETRIEVE_FULL_PS_KEY            = 0x0711
        const val STORE_PS_KEY                    = 0x0712
        const val FLOOD_PS                        = 0x0713
        const val STORE_FULL_PS_KEY              = 0x0714
        const val SEND_DEBUG_MESSAGE              = 0x0720
        const val SEND_APPLICATION_MESSAGE        = 0x0721
        const val SEND_KALIMBA_MESSAGE            = 0x0722
        const val GET_MEMORY_SLOTS                = 0x0730
        const val GET_DEBUG_VARIABLE              = 0x0740
        const val SET_DEBUG_VARIABLE              = 0x0741
        const val DELETE_PDL                      = 0x0750
        const val SET_BLE_CONNECTION_PARAMETERS   = 0x0752

        // Commandes de notification (0x40nn)
        const val REGISTER_NOTIFICATION           = 0x4001
        const val CANCEL_NOTIFICATION             = 0x4002
        const val EVENT_NOTIFICATION              = 0x4003
    }

    // En-tête du paquet GAIA (exemple, à adapter selon les spécifications réelles)
    private val gaiaVendorId: Short = 0x000A  // Exemple de Vendor ID
    private val gaiaProtocolVersion: Byte = 0x01

    /**
     * Construit un paquet GAIA à partir d’un ID de commande et d’une charge utile optionnelle.
     */
    fun createCommandPacket(commandId: Int, payload: ByteArray? = null): ByteArray {
        val payloadLength = payload?.size ?: 0
        val packet = ByteArray(8 + payloadLength)  // Taille d’en-tête supposée 8 octets
        packet[0] = 0xFF.toByte()                   // Début de paquet GAIA
        packet[1] = gaiaProtocolVersion             // Version du protocole
        // Octets 2-3 : Vendor ID
        packet[2] = (gaiaVendorId.toInt() shr 8).toByte()
        packet[3] = gaiaVendorId.toByte()
        // Octets 4-5 : Command ID
        packet[4] = (commandId shr 8).toByte()
        packet[5] = commandId.toByte()
        // Octet 6 : Flag indiquant la présence d’une payload (1 si présente, 0 sinon)
        packet[6] = if (payloadLength > 0) 0x01.toByte() else 0x00.toByte()
        // Octet 7 : Longueur de la payload
        packet[7] = payloadLength.toByte()
        // Octets suivants : Données de la payload, si présentes
        payload?.copyInto(packet, 8)
        // Optionnel : Calcul et ajout d’un checksum si requis par le protocole
        return packet
    }

    /**
     * Analyse un paquet de réponse GAIA reçu du dispositif.
     *
     * Extrait l’ID de commande (octets 4-5), le statut (octet 6) et la payload (le reste du paquet).
     */
    fun parseResponsePacket(packet: ByteArray): GaiaResponse {
        if (packet.isEmpty()) {
            throw IllegalArgumentException("Empty GAIA packet")
        }
        // Extraction de l'ID de commande
        val commandId = (packet[4].toInt() shl 8) or (packet[5].toInt() and 0xFF)
        // Extraction du statut (octet 6)
        val status = packet.getOrNull(6)?.toInt() ?: -1
        // Récupération de la payload (si présente)
        val payload = if (packet.size > 7) packet.copyOfRange(7, packet.size) else ByteArray(0)
        return GaiaResponse(commandId, status, payload)
    }
}

/**
 * Modèle de données pour une réponse GAIA (simplifié).
 * Redéfinition de equals() et hashCode() pour une comparaison correcte du ByteArray.
 */
data class GaiaResponse(val commandId: Int, val status: Int, val payload: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GaiaResponse) return false

        if (commandId != other.commandId) return false
        if (status != other.status) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commandId
        result = 31 * result + status
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
