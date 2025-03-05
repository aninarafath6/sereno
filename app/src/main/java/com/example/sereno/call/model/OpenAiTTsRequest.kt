package com.example.sereno.call.model
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiTTsRequest(
    val model: String,
    val input: String,
    val voice: String
)