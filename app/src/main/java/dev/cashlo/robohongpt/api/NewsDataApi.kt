package dev.cashlo.robohongpt.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsDataApi {
    @GET("news")
    fun getNews(
        @Query("apiKey") apiKey: String,
        @Query("country") country: String? = null,
        @Query("language") language: String? = null,
        @Query("category") category: String? = "top",
        @Query("domain") domain: String? = null,
        @Query("q") query: String? = null,
        @Query("qInTitle") queryInTitle: String? = null
    ): Call<NewsDataResponse>
}
