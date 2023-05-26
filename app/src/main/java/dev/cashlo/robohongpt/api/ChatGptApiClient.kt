package dev.cashlo.robohongpt.api
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.cashlo.robohongpt.api.ChatGptPrompt.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.platform.android.AndroidLogHandler.close
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit


object ChatGptApiClient {
    private const val BASE_URL = "https://api.openai.com/v1/"
    private const val MODEL = "gpt-3.5-turbo"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val apiService = retrofit.create(ChatGptApi::class.java)
    val gson = Gson()

    var currentChatMessages = mutableListOf<ChatGptRequest.Message>()

    fun streamChatResponse(userPrompt: String, systemPrompt: Prompt, numberOfAgents: Int): Flow<String> {
        if (currentChatMessages.isEmpty()) {
            currentChatMessages = ChatGptPrompt.getPrompt(systemPrompt, numberOfAgents) ?: throw IllegalArgumentException("bad systemPrompt")
        }
        currentChatMessages.add(ChatGptRequest.Message("user", userPrompt))
        val requestJson = gson.toJson(ChatGptRequest(MODEL, currentChatMessages, stream = true))
        Log.d("streamChatResponse()", requestJson)


        val request = Request.Builder()
            .url(BASE_URL + "chat/completions")
            .addHeader("Authorization", "Bearer ${ChatGptApi.API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(requestJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return callbackFlow<String> {
            val listener = object : EventSourceListener() {
                private val responseBuilder = StringBuilder()

                override fun onClosed(eventSource: EventSource) {
                    close()
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    if (data == "[DONE]") {
                        eventSource.cancel()
                        val responseContent = responseBuilder.toString()
                        // Process the response content as before
                        // ...
                        currentChatMessages.add(ChatGptRequest.Message("assistant", responseContent))
                        if (currentChatMessages.size > 20) {
                            currentChatMessages.removeAt(3)
                            currentChatMessages.removeAt(4)
                        }
                        close()
                    } else {
                        val apiChunkResponse = gson.fromJson(data, ApiChunkResponse::class.java)
                        val content = apiChunkResponse.choices[0].delta.content
                        if (content != null) {
                            sendBlocking(content)
                            responseBuilder.append(content)
                        }
                    }
                }
            }

            val eventSource =
                EventSources.createFactory(okHttpClient).newEventSource(request, listener)
            awaitClose { eventSource.cancel() }
        }
    }

    fun getResponse(userPrompt: String, systemPrompt: Prompt, numberOfAgents: Int): List<Speech>? {
        if (currentChatMessages.isEmpty()) {
            currentChatMessages = ChatGptPrompt.getPrompt(systemPrompt, numberOfAgents) ?: throw IllegalArgumentException("bad systemPrompt")
        }
        currentChatMessages.add(ChatGptRequest.Message("user", userPrompt))
        val request = ChatGptRequest(MODEL, currentChatMessages)


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
        currentChatMessages.add(ChatGptRequest.Message("assistant", responseContent!!))
        if (currentChatMessages.size > 20) {
            currentChatMessages.removeAt(3)
            currentChatMessages.removeAt(4)
        }
        if (systemPrompt == Prompt.JP_PAIR || systemPrompt == Prompt.EN_PAIR) {
            val itemType = object : TypeToken<List<Speech>>() {}.type
            var speechList: List<Speech>? = null
            try {
                speechList = gson.fromJson<List<Speech>>(responseContent, itemType)

                return speechList
            } catch( e: Exception ) {
                Log.d("getResponse()", "non-JSON response")
                Log.d("getResponse()", responseContent)
                currentChatMessages.removeLast()
                currentChatMessages.removeLast()
                return getResponse(userPrompt, systemPrompt, numberOfAgents)
            }
        }
        val splitContent = responseContent!!.split("|")
        return if (splitContent.size == 3) {
            listOf(Speech(splitContent[2], splitContent[1], splitContent[0], null))
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