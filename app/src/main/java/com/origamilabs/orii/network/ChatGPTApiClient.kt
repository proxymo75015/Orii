package com.origamilabs.orii.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

object ChatGPTApiClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://assisantorii.onrender.com") // Mon URL serveur
        .addConverterFactory(GsonConverterFactory.create()) // Utilisation de Gson
        .build()

    private val apiService = retrofit.create(ChatGPTApiService::class.java)

    private val sessionId = UUID.randomUUID().toString()

    suspend fun sendIntermediateRequest(partialText: String) {
        try {
            apiService.sendIntermediateText(ChatGPTIntermediateRequest(sessionId, partialText))
        } catch (e: Exception) {
            println("Erreur lors de l'envoi du texte intermédiaire : ${e.message}")
        }
    }

    suspend fun sendFinalRequest(finalText: String): String {
        return try {
            val response = apiService.sendFinalText(ChatGPTFinalRequest(sessionId, finalText))
            response.body()?.answer ?: "Erreur de réponse ChatGPT"
        } catch (e: Exception) {
            "Erreur lors de la communication avec ChatGPT."
        }
    }
}
