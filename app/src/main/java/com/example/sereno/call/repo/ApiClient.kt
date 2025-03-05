package com.example.sereno.call.repo

import android.content.Context
import android.util.Log
import com.example.sereno.call.model.OpenAiTTsRequest
import com.example.sereno.chat.model.GroqRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class ApiClient {
    private val client: OkHttpClient = OkHttpClient()

    companion object {
        private const val TAG = "ApiClient"
        private const val GROQ_API_KEY = "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"
        private const val OPEN_API_KEY =
            "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"
        private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val OPEN_AI_URL = "https://api.openai.com/v1/audio/speech"
        private const val OPEN_AI_TTS_MODEL = "tts-1"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val moshi: Moshi = Moshi.Builder().build()

    private val openAiTTsRequestAdapter: JsonAdapter<OpenAiTTsRequest> =
        moshi.adapter(OpenAiTTsRequest::class.java)

    /**
     * Executes a request to the Groq AI API
     *
     * @param requestJson Prepared JSON request body
     * @return ApiResult with response data or error
     */
    fun executeGroqRequest(requestJson: String): ApiResult<String> {
        return try {
            val request = Request.Builder()
                .url(GROQ_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                .post(requestJson.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ApiResult.Error("Groq API request failed: ${response.code}")
                }

                response.body?.string()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty Groq response")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Groq network error", e)
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Executes a text-to-speech request to OpenAI
     *
     * @param text Text to convert to speech
     * @return ApiResult with generated audio file or error
     */
    fun executeTtsRequest(text: String): ApiResult<File> {
        return try {

            val ttsRequestBody = openAiTTsRequestAdapter.toJson(
                OpenAiTTsRequest(
                    model = OPEN_AI_TTS_MODEL,
                    input = text,
                    voice = "sage"
                )
            )

            val request = Request.Builder()
                .url(OPEN_AI_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $OPEN_API_KEY")
                .post(ttsRequestBody.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.d("Error", "TTS API request failed: ${response.code}")
                    Log.d("Error", "TTS API request failed: ${response.message}")
                    Log.d("Error", "TTS API request failed: ${response.body}")
                    Log.d("Error", "TTS API request failed: ${response.body?.string()}")
                    return ApiResult.Error("TTS API request failed: ${response.code}")
                }

                val responseBody = response.body
                    ?: return ApiResult.Error("Empty response body")

                val audioFile = File.createTempFile("speech", ".mp3")
                try {
                    audioFile.outputStream().use { fileOutputStream ->
                        responseBody.byteStream().use { inputStream ->
                            inputStream.copyTo(fileOutputStream)
                        }
                    }
                    ApiResult.Success(audioFile)
                } catch (e: IOException) {
                    Log.e(TAG, "Error saving audio file", e)
                    ApiResult.Error("Failed to save audio file: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS request error", e)
            ApiResult.Error("TTS error: ${e.message}")
        }
    }
}

/**
 * Sealed class to represent API call results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}