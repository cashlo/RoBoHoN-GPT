package dev.cashlo.robohongpt.api


import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        val response = apiClient.getResponse("", ChatGptApiClient.Prompt.EN)
        assertNull(response)
    }

    @Test
    fun `getResponse should return a non-empty response when prompt is valid`() {
        val prompt = "Hello, how are you?"
        val response = apiClient.getResponse(prompt, ChatGptApiClient.Prompt.EN)
        response!!.forEach {
            assert(it.text.isNotEmpty())
        }
    }

    @Test
    fun `getResponse should return a name of Robohon`() {
        val prompt = "Hello, do you know each other's name?"
        val response = apiClient.getResponse(prompt, ChatGptApiClient.Prompt.JP_PAIR)
        response!!.forEach {
            assert(it.name!!.isNotEmpty())
        }

    }


}