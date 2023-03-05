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
        val response = apiClient.getResponse("")
        assertNull(response)
    }

    @Test
    fun `getResponse should return a non-empty response when prompt is valid`() {
        val prompt = "Hello, how are you?"
        val response = apiClient.getResponse(prompt)
        assertNotNull(response)
        assert(response!!.isNotEmpty())
    }
}