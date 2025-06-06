package com.example.sereno.features.chat.data.repo

import android.content.Context
import android.util.Log
import com.example.sereno.R
import com.example.sereno.features.chat.data.model.Chat
import com.example.sereno.features.chat.domain.model.OpenAiRequest
import com.example.sereno.features.chat.domain.model.OpenAiResponse
import com.example.sereno.features.chat.domain.model.Message
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepo(
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val GROQ_API_KEY = "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"
        private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL_NAME = "llama-3.3-70b-versatile"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private const val TAG = "ChatRepository"
    }

    private val moshi = Moshi.Builder().build()

    private val chatRequestAdapter: JsonAdapter<OpenAiRequest> =
        moshi.adapter(OpenAiRequest::class.java)
    private val groqResponseAdapter: JsonAdapter<OpenAiResponse> =
        moshi.adapter(OpenAiResponse::class.java)

    suspend fun chat(
        context: Context,
        userChat: Chat,
        contextChat: List<Chat> = emptyList(),
        replyTo: Chat? = null
    ): ChatResponse = withContext(Dispatchers.IO) {
        try {
            val requestBody = buildRequestBody(
                userChat.message,
                contextChat,
                replyTo,
                context.getString(R.string.system_prompt)
            )
            val request = buildRequest(requestBody)
            executeRequest(request, userChat.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error in chat method", e)
            ChatResponse.Failed(e.message ?: "Unexpected error occurred")
        }
    }

    private fun buildRequestBody(
        inputSentence: String,
        contextChats: List<Chat>,
        replyTo: Chat?,
        systemPrompt: String,
    ): String {
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
                    content = chat.message
                )
            )
        }

        messages.add(Message(role = "user", content = userMessage))

        val chatRequest = OpenAiRequest(
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

    private fun executeRequest(request: Request, replyToUser: String): ChatResponse {
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ChatResponse.Failed("API request failed with code: ${response.code}")
                }

                val responseBody =
                    response.body?.string() ?: return ChatResponse.Failed("Empty response body")
                parseResponse(responseBody, replyToUser)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            ChatResponse.Failed("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing request", e)
            ChatResponse.Failed("Error: ${e.message}")
        }
    }

    private fun parseResponse(responseBody: String, replyToUser: String): ChatResponse {
        return try {
            val groqResponse = groqResponseAdapter.fromJson(responseBody)
                ?: return ChatResponse.Failed("Failed to parse response")

            val content = groqResponse.choices.firstOrNull()?.message?.content
                ?: return ChatResponse.Failed("No message content in response")

            val parsedParagraphs = content.split("\n").filter { it.isNotBlank() }
            val chats = createChainedChats(parsedParagraphs, replyToUser)

            ChatResponse.Success(chats)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            ChatResponse.Failed("Failed to parse response: ${e.message}")
        }
    }

    private fun createChainedChats(
        paragraphs: List<String>,
        initialReplayChatId: String?
    ): List<Chat> {
        val chats = mutableListOf<Chat>()
        var currentReplayChatId = initialReplayChatId

        paragraphs.forEach { paragraph ->
            val newChat = Chat(
                message = paragraph,
                isBot = true,
                replayChatId = currentReplayChatId,
                createdAt = System.currentTimeMillis()
            )
            chats.add(newChat)
            currentReplayChatId = newChat.id
        }
        return chats
    }
}

sealed class ChatResponse {
    data class Failed(val message: String) : ChatResponse()
    data class Success(val response: List<Chat>) : ChatResponse()
}
