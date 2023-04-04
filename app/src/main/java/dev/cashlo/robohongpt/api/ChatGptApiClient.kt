package dev.cashlo.robohongpt.api
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit


object ChatGptApiClient {
    private const val BASE_URL = "https://api.openai.com/v1/"
    private const val MODEL = "gpt-3.5-turbo"




    private val retrofit by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val apiService = retrofit.create(ChatGptApi::class.java)
    val gson = Gson()


    fun getResponse(userPrompt: String, systemPrompt: ChatGptPrompt.Prompt, numberOfAgents: Int): List<Speech>? {
        val messages = ChatGptPrompt.getPrompt(systemPrompt, numberOfAgents) ?: throw IllegalArgumentException("bad systemPrompt")
        messages.add(ChatGptRequest.Message("user", userPrompt))
        val request = ChatGptRequest(MODEL, messages)


        var success = false
        var waitTime = 1000L
        var response: Response<ChatGptResponse>? = null
        while (!success) {
            try {
                response = apiService.getResponse(
                    request
                ).execute()

            } catch (exception: IOException) {
                exception.printStackTrace()
                Log.d("getResponse()", "ChatGPT API call failed, retrying in $waitTime ms")
                sleep(waitTime)
                waitTime *= 2
                continue
            }
            success = true
        }

        val responseContent = response?.body()?.choices?.get(0)?.message?.content
        messages.add(ChatGptRequest.Message("assistant", responseContent!!))
        if (messages.size > 20) {
            messages.removeAt(3)
            messages.removeAt(4)
        }
        if (systemPrompt == ChatGptPrompt.Prompt.JP_PAIR || systemPrompt == ChatGptPrompt.Prompt.EN_PAIR) {
            val itemType = object : TypeToken<List<Speech>>() {}.type
            var speechList: List<Speech>? = null
            try {
                speechList = gson.fromJson<List<Speech>>(responseContent, itemType)

                return speechList
            } catch( e: Exception ) {
                Log.d("getResponse()", "non-JSON response")
                messages.removeLast()
                messages.removeLast()
                return listOf(Speech(responseContent, null, null, null))
            }
            return speechList
        }
        val splitContent = responseContent!!.split("|")
        return if (splitContent.size == 3) {
            listOf(Speech(splitContent[0], splitContent[1], splitContent[2], null))
        }
        else {
            listOf(Speech(responseContent, null, null, null))
        }
    }

    data class Speech(
        val text: String,
        val emotion: String?,
        val level: String?,
        val name: String?
    )
}