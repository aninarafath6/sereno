package com.example.sereno.chat.repo

import android.util.Log
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.ChatRequest
import com.example.sereno.chat.model.GroqResponse
import com.example.sereno.chat.model.Message
import com.example.sereno.common.utils.DateUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroqRepo @Inject constructor(
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "GroqRepo"
        private const val GROQ_API_KEY = "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"
        private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL_NAME = "llama-3.3-70b-versatile"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val moshi = Moshi.Builder().build()

    private val chatRequestAdapter: JsonAdapter<ChatRequest> =
        moshi.adapter(ChatRequest::class.java)
    private val chatResponseAdapter: JsonAdapter<GroqResponse> =
        moshi.adapter(GroqResponse::class.java)

    suspend fun chat(
        sentence: String,
        contextChat: List<Chat> = emptyList(),
        replyTo: Chat? = null
    ): ChatResponse = withContext(Dispatchers.IO) {
        try {
            val requestBody = buildRequestBody(sentence, contextChat, replyTo)
            val request = buildRequest(requestBody)
            executeRequest(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error in chat method", e)
            ChatResponse.Failed(e.message ?: "Unexpected error occurred")
        }
    }

    private fun buildRequestBody(
        inputSentence: String,
        contextChats: List<Chat>,
        replyTo: Chat?
    ): String {
        val systemPrompt =
            """Reply in a single casual line with emojis, like a human chat. You'll receive metadata with each message—use it for better context, but don’t show it to the user""".trimIndent()

        val userMessage = if (replyTo != null) {
            "Regarding our previous conversation: '${replyTo.message}', I also wanted to add: $inputSentence"
        } else {
            inputSentence
        }

        val messages = mutableListOf<Message>()
        messages.add(Message(role = "system", content = systemPrompt))

        contextChats.forEach { chat ->
            messages.add(
                Message(
                    role = if (chat.isBot) "assistant" else "user",
                    content = chat.message + ", metadata: date_and_time : ${
                        DateUtils.formatDateAndTime(
                            chat.createdAt
                        )
                    }"
                )
            )
        }

        messages.add(Message(role = "user", content = userMessage))

        val chatRequest = ChatRequest(
            model = MODEL_NAME,
            messages = messages
        )

        val json = chatRequestAdapter.toJson(chatRequest)
        Log.d(TAG, "Request body: $json")
        return json
    }

    private fun buildRequest(bodyContent: String): Request {
        return Request.Builder()
            .url(GROQ_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $GROQ_API_KEY")
            .post(bodyContent.toRequestBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun executeRequest(request: Request): ChatResponse {
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ChatResponse.Failed("API request failed with code: ${response.code}")
                }

                val responseBody =
                    response.body?.string() ?: return ChatResponse.Failed("Empty response body")
                parseResponse(responseBody)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            ChatResponse.Failed("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing request", e)
            ChatResponse.Failed("Error: ${e.message}")
        }
    }

    private fun parseResponse(responseBody: String): ChatResponse {
        return try {
            val groqResponse = chatResponseAdapter.fromJson(responseBody)
                ?: return ChatResponse.Failed("Failed to parse response")

            val content = groqResponse.choices.firstOrNull()?.message?.content
                ?: return ChatResponse.Failed("No message content in response")

            Log.d(TAG, "Parsed: ${groqResponse.choices} ")
            val parsedParagraph = content.split("\n").filter { it.isNotBlank() }
            ChatResponse.Success(parsedParagraph)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            ChatResponse.Failed("Failed to parse response: ${e.message}")
        }
    }
}


sealed class ChatResponse {
    data class Failed(val message: String) : ChatResponse()
    data class Success(val response: List<String>) : ChatResponse()
}