package dev.cashlo.robohongpt.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ChatGptApiClient {
    private const val BASE_URL = "https://api.openai.com/v1/"
    private const val API_KEY = "c0SV0qk2EryrtAOw9iyaT3BlbkFJknuWMIzgmkgsLoVZApmH"
    private const val MODEL = "gpt-3.5-turbo"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService = retrofit.create(ChatGptApi::class.java)


    fun getResponse(prompt: String): String? {
        val messages = listOf(ChatGptRequest.Message("user", prompt))
        val request = ChatGptRequest(MODEL, messages)

        try {
            val response = apiService.getResponse(
                request
            ).execute()

            return response?.body()?.choices?.get(0)?.message?.content
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}