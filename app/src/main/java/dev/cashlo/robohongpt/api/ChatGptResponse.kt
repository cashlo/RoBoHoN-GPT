package dev.cashlo.robohongpt.api

import com.google.gson.annotations.SerializedName

data class ChatGptResponse(
    val id: String?,
    @SerializedName("object")
    val responseObj: String?,
    val created: Long?,
    val choices: List<Choice>?,
    val usage: Usage?
) {
    data class Choice(
        val index: Int?,
        val message: ChatGptRequest.Message?,
        val finish_reason: String?
    )

    data class Usage(
        val prompt_tokens: Int?,
        val completion_tokens: Int?,
        val total_tokens: Int?
    )
}
