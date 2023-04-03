package dev.cashlo.robohongpt.api

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NewsApiClientTest {

    private lateinit var apiClient: NewsApiClient

    @Before
    fun setUp() {
        apiClient = NewsApiClient
    }

    @Test
    fun getNews() {
        val response = apiClient.getNews("jp", "jp")
        Assert.assertNotNull(response)
    }
}