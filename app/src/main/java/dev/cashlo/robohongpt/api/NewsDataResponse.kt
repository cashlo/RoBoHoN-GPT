package dev.cashlo.robohongpt.api

data class NewsDataResponse(
    val status: String?,
    val totalResults: Int?,
    val results: List<NewsArticle>?
) {
    data class NewsArticle(
        val title: String?,
        val link: String?,
        val description: String?,
        val content: String?,
        val pubDate: String?,
        val imageUrl: String?,
        val sourceId: String?,
        val categories: List<String>?,
        val countries: List<String>?,
        val language: String?
    )
}