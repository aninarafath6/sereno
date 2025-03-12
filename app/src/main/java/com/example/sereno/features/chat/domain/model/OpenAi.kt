package com.example.sereno.features.chat.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiRequest(
    val model: String,
    val messages: List<Message>
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiResponse(
    val id: String,
    val choices: List<Choice>
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: Message,
    @Json(name = "finish_reason")
    val finishReason: String
)
