package com.origamilabs.orii.ui.main.help

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * ViewModel pour capturer et gérer le log de l'application.
 *
 * Ce ViewModel permet de récupérer les logs via la commande "logcat -d",
 * de les effacer avec "logcat -c" et de les sauvegarder dans un fichier.
 *
 * Remarque : La propriété [mContext] doit être initialisée avant utilisation.
 */
class CatchLogViewModel : ViewModel() {

    // Contexte de l'application ; à initialiser avant usage.
    lateinit var mContext: Context

    // LiveData contenant le texte du log capturé.
    val oriiLog = MutableLiveData<String>()

    /**
     * Capture les logs du système en exécutant la commande "logcat -d"
     * et met à jour [oriiLog] avec le contenu récupéré.
     */
    fun catchLog() {
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val logText = process.inputStream.bufferedReader().use { it.readText() }
            oriiLog.postValue(logText)
        } catch (e: Exception) {
            // En cas d'erreur, on peut poster un message d'erreur dans le LiveData.
            oriiLog.postValue("Error capturing log: ${e.message}")
        }
    }

    /**
     * Efface le log système en exécutant la commande "logcat -c"
     * et réinitialise [oriiLog] à une chaîne vide.
     */
    fun clearLog() {
        try {
            Runtime.getRuntime().exec("logcat -c")
            oriiLog.postValue("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sauvegarde le contenu de [oriiLog] dans le fichier "Log.txt" situé
     * dans le dossier "/sdcard/Download".
     */
    fun saveLog() {
        try {
            val file = File("/sdcard/Download", "Log.txt")
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            OutputStreamWriter(FileOutputStream(file)).use { writer ->
                writer.append(oriiLog.value ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
