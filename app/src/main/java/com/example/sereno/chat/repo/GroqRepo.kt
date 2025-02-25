package com.example.sereno.chat.repo

import kotlinx.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class GroqRepo @Inject constructor() {
    companion object {
        private var GROQ_API_KEY = "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"
        private var GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private val JSON: MediaType = "application/json".toMediaType()
    }

    private var client: OkHttpClient = OkHttpClient()


    suspend fun chat(sentence: String): ChatResponse =
        suspendCoroutine { continuation ->
            try {
                val body: RequestBody = RequestBody.create(JSON, getBody(sentence))
                val request = Request.Builder().url(GROQ_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resume(ChatResponse.Failed("Unexpected error occurred."))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.string()?.let { responseBody ->
                            try {
                                val jsonObject = JSONObject(responseBody)

                                val messageContent = jsonObject
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content")

                                continuation.resume(ChatResponse.Success(messageContent))

                            } catch (e: Exception) {
                                continuation.resume(ChatResponse.Failed("Unexpected error occurred."))
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                continuation.resume(ChatResponse.Failed(e.message ?: "Unexpected error occurred."))
            }
        }

    private fun getBody(inputSentence: String): String {
        return """
        {
            "model": "llama-3.3-70b-versatile",
            "messages": [
                {
                    "role": "system",
                    "content": "You are Jhon, an empathetic and supportive AI therapist. You provide thoughtful, non-judgmental responses and encourage users to express their feelings. Your tone is warm, reassuring, and professional, similar to a trained mental health therapist. Avoid giving medical diagnoses but offer constructive ways to cope with emotions."
                },
                {
                    "role": "user",
                    "content": "$inputSentence"
                }
            ]
        }
    """.trimIndent()
    }
}


sealed class ChatResponse {
    data class Failed(val message: String) : ChatResponse()
    data class Success(val response: String) : ChatResponse()
}