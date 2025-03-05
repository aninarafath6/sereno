package com.example.sereno.call.repo

import android.content.Context
import android.util.Log
import com.example.sereno.R
import com.example.sereno.chat.model.GroqRequest
import com.example.sereno.chat.model.GroqResponse
import com.example.sereno.chat.model.Message
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.File

class CallRepo(private val context: Context) {
    private val apiClient: ApiClient = ApiClient()
    private val moshi: Moshi = Moshi.Builder().build()

    companion object {
        private const val TAG = "CallRepository"
        private const val MODEL_NAME = "llama-3.3-70b-versatile"
    }

    private val requestAdapter: JsonAdapter<GroqRequest> =
        moshi.adapter(GroqRequest::class.java)
    private val responseAdapter: JsonAdapter<GroqResponse> =
        moshi.adapter(GroqResponse::class.java)

    /**
     * Performs a call by generating a response and converting it to speech
     * @param userMessage The user's input message
     * @return CallResponse indicating success or failure
     */
    fun performCall(userMessage: String): CallResponse {
        return try {
            val groqRequest = createGroqRequest(userMessage)
            val aiResponse = executeAiRequest(groqRequest)
            val response = convertTextToSpeech(aiResponse)

            Log.d(TAG, "Call processed successfully")
            Log.d(TAG, "response: ${response}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Call processing error", e)
            CallResponse.Failed(e.message ?: "Unexpected error occurred")
        }
    }

    /**
     * Creates a Groq AI request with system and user messages
     *
     * @param userMessage The user's input message
     * @return GroqRequest for AI processing
     */
    private fun createGroqRequest(userMessage: String): GroqRequest {
        val messages = mutableListOf(
            Message(
                role = "system",
                content = context.getString(R.string.system_prompt_for_call)
            ),
            Message(role = "user", content = userMessage)
        )

        return GroqRequest(
            model = MODEL_NAME,
            messages = messages
        )
    }

    /**
     * Executes the AI request and retrieves the response
     *
     * @param request The prepared Groq request
     * @return The AI-generated response text
     */
    private fun executeAiRequest(request: GroqRequest): String {
        val requestJson = requestAdapter.toJson(request)
        return when (val result = apiClient.executeGroqRequest(requestJson)) {
            is ApiResult.Success -> {
                val groqResponse = responseAdapter.fromJson(result.data)
                groqResponse?.choices?.firstOrNull()?.message?.content
                    ?: throw IllegalStateException("No response content")
            }

            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    /**
     * Converts text response to speech audio file
     *
     * @param text The text to convert to speech
     * @return CallResponse with the generated audio file
     */
    private fun convertTextToSpeech(text: String): CallResponse {
        return when (val result = apiClient.executeTtsRequest(text)) {
            is ApiResult.Success -> CallResponse.Success(result.data)
            is ApiResult.Error -> CallResponse.Failed(result.message)
        }
    }
}

/**
 * Sealed class to represent call response states
 */
sealed class CallResponse {
    data class Success(val audioFile: File) : CallResponse()
    data class Failed(val message: String) : CallResponse()
}