package dev.cashlo.robohongpt.api

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val stream: Boolean? = null,
    val functions: List<Function>? = null,
    val function_call: FunctionCall? = null
) {
    data class Message(
        val role: String?,
        val content: String?,
        val name: String? = null,
        val function_call: FunctionCall? = null
    ) {
        data class FunctionCall(
            val name: String?,
            val arguments: String?
        )
    }

    data class Function(
        val name: String,
        val description: String? = null,
        val parameters: Parameters
    ) {
        data class Parameters(
            val type: String,
            val properties: Map<String, Property>
        ) {
            data class Property(
                val type: String,
                val items: Item? = null
            ) {
                data class Item(
                    val type: String,
                    val properties: Map<String, Property>
                )
            }
        }
    }

    data class FunctionCall(
        val name: String? = null
    )
}