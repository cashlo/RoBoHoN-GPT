package dev.cashlo.robohongpt.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGptApi {
    @Headers("Content-Type: application/json", "Authorization: Bearer ")
    @POST("chat/completions")
    fun getResponse(@Body request: ChatGptRequest): Call<ChatGptResponse>
}