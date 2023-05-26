package dev.cashlo.robohongpt.api

data class ApiChunkResponse(
    val id: String,
    val `object`: String,
    val created: Int,
    val model: String,
    val choices: List<ApiChoice>
)

data class ApiChoice(
    val delta: ApiDelta,
    val index: Int,
    val finish_reason: String?
)

data class ApiDelta(
    val content: String
)