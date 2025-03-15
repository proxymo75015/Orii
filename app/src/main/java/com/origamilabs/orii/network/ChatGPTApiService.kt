package com.origamilabs.orii.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatGPTApiService {

    @POST("chat/intermediate")
    suspend fun sendIntermediateText(
        @Body request: ChatGPTIntermediateRequest
    ): Response<Unit>

    @POST("chat/final")
    suspend fun sendFinalText(
        @Body request: ChatGPTFinalRequest
    ): Response<ChatGPTFinalResponse>
}
