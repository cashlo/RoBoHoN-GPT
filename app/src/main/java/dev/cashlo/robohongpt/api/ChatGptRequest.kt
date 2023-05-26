package dev.cashlo.robohongpt.api

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val stream: Boolean? = null
) {
    data class Message(
        val role: String,
        val content: String
    )
}