package com.origamilabs.orii.utils

import android.util.Log
import java.io.File
import kotlin.text.Charsets

object FileUtils {
    private const val TAG = "FileUtils"

    fun getStringFromFile(file: File): String {
        // Utilisation de l'extension 'bufferedReader' et de 'use' pour gérer automatiquement la fermeture du flux.
        val text = file.inputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }
        Log.d(TAG, text)
        return text
    }
}
