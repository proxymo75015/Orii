package com.origamilabs.orii.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// --------------------------
// Modèles de données pour l'API ChatGPT
// --------------------------
data class ChatGPTMessage(
    val role: String,
    val content: String
)

data class ChatGPTRequest(
    val model: String,
    val messages: List<ChatGPTMessage>
)

data class ChatGPTChoice(
    val message: ChatGPTMessage
)

data class ChatGPTUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class ChatGPTResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val choices: List<ChatGPTChoice>,
    val usage: ChatGPTUsage
)

// --------------------------
// Définition du service Retrofit pour l'API ChatGPT
// --------------------------
interface ChatGPTService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatGPTRequest
    ): Response<ChatGPTResponse>
}

// --------------------------
// Client API ChatGPT (singleton)
// --------------------------
object ChatGPTApiClient {
    private const val BASE_URL = "https://api.openai.com/"
    // Remplacez YOUR_API_KEY par votre clé API OpenAI.
    private const val API_KEY = "Bearer YOUR_API_KEY"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val service: ChatGPTService by lazy {
        retrofit.create(ChatGPTService::class.java)
    }

    /**
     * Envoie une requête à ChatGPT avec le prompt de l'utilisateur et renvoie la réponse textuelle.
     */
    suspend fun sendRequest(userPrompt: String): String {
        val requestBody = ChatGPTRequest(
            model = "gpt-3.5-turbo", // ou un autre modèle disponible
            messages = listOf(
                ChatGPTMessage("system", "Vous êtes un assistant utile."),
                ChatGPTMessage("user", userPrompt)
            )
        )
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getChatCompletion(API_KEY, requestBody)
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    // On récupère le contenu du premier choix retourné
                    chatResponse?.choices?.firstOrNull()?.message?.content ?: "Aucune réponse reçue"
                } else {
                    "Erreur API : ${response.code()} ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                "Exception lors de l'appel : ${e.localizedMessage}"
            }
        }
    }
}
