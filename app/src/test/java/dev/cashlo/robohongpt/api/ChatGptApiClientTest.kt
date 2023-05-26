package dev.cashlo.robohongpt.api


import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ChatGptApiClientTest {
    private lateinit var apiClient: ChatGptApiClient

    @Before
    fun setUp() {
        apiClient = ChatGptApiClient
    }

    @Test
    fun `getResponse should return null when prompt is empty`() {
        val response = apiClient.getResponse("", ChatGptPrompt.Prompt.EN, 1)
        assertNull(response)
    }

    @Test
    fun `getResponse should return a non-empty response when prompt is valid`() {
        val prompt = "Hello, how are you?"
        val response = apiClient.getResponse(prompt, ChatGptPrompt.Prompt.EN, 1)
        response!!.forEach {
            assert(it.text.isNotEmpty())
        }
    }

    @Test
    fun `getResponse should return a name of Robohon`() {
        val prompt = "Hello, do you know each other's name?"
        val response = apiClient.getResponse(prompt, ChatGptPrompt.Prompt.JP_PAIR, 1)
        response!!.forEach {
            assert(it.name!!.isNotEmpty())
        }
    }

    @Test
    fun `streamChatResponse should stream the response`() = runBlocking {
        val prompt = "Hello, how are you?"
        val chatFlow = apiClient.streamChatResponse(prompt, ChatGptPrompt.Prompt.EN, 1)
        chatFlow.collect{
            println(it)
        }
    }


}