package com.example.sereno.features.call.data.repo

import android.content.Context
import android.util.Log
import com.example.sereno.R
import com.example.sereno.features.call.domain.error.CallError
import com.example.sereno.features.call.domain.model.ApiResult
import com.example.sereno.features.chat.domain.model.OpenAiRequest
import com.example.sereno.features.chat.domain.model.OpenAiResponse
import com.example.sereno.features.chat.domain.model.Message
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class CallRepo(private val context: Context) {
    private val apiClient: OpenAiClient = OpenAiClient()
    private val moshi: Moshi = Moshi.Builder().build()

    companion object {
        private const val TAG = "CallRepository"
        private const val MODEL_NAME = "gpt-4o"
    }

    private val requestAdapter: JsonAdapter<OpenAiRequest> =
        moshi.adapter(OpenAiRequest::class.java)
    private val responseAdapter: JsonAdapter<OpenAiResponse> =
        moshi.adapter(OpenAiResponse::class.java)

    fun performCall(userMessage: String): CallResponse {
        return try {
            val groqRequest = createGroqRequest(userMessage)
            val aiResponse = executeAiRequest(groqRequest)
            val response = convertTextToSpeech(aiResponse)

            Log.d(TAG, "Call processed successfully")
            Log.d(TAG, "response: $response")
            response
        } catch (e: Exception) {
            CallResponse.Failed(CallError.Unknown)
        }
    }

    private fun createGroqRequest(userMessage: String): OpenAiRequest {
        val messages = mutableListOf(
            Message(
                role = "system",
                content = context.getString(R.string.system_prompt_for_call)
            ),
            Message(role = "user", content = userMessage)
        )

        return OpenAiRequest(
            model = MODEL_NAME,
            messages = messages
        )
    }

    private fun executeAiRequest(request: OpenAiRequest): String {
        val requestJson = requestAdapter.toJson(request)
        return when (val result = apiClient.executeOpenAiRequest(requestJson)) {
            is ApiResult.Success -> {
                val groqResponse = responseAdapter.fromJson(result.data)
                groqResponse?.choices?.firstOrNull()?.message?.content
                    ?: throw IllegalStateException("No response content")
            }

            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    private fun convertTextToSpeech(text: String): CallResponse {
        return when (val result = apiClient.executeTtsRequest(text)) {
            is ApiResult.Success -> CallResponse.Success(result.data)
            is ApiResult.Error -> CallResponse.Failed(CallError.Unknown)
        }
    }
}

