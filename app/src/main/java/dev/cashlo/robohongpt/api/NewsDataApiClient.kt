package dev.cashlo.robohongpt.api

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsApiClient {
    private const val BASE_URL = "https://newsdata.io/api/1/"
    private const val API_KEY = ""

    private val retrofit by lazy {
        val okHttpClient = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val apiService = retrofit.create(NewsDataApi::class.java)

    fun getNews(country: String, language: String): List<NewsDataResponse.NewsArticle>? {
        val response = apiService.getNews(
            API_KEY,
            country,
            language
        ).execute()

        return response.body()!!.results
    }
}