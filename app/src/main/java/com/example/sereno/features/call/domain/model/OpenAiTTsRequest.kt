package com.example.sereno.features.call.domain.model
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiTTsRequest(
    val model: String,
    val input: String,
    val voice: String
)