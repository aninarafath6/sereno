package com.example.sereno.features.call.data.repo

import android.util.Log
import com.example.sereno.features.call.domain.model.ApiResult
import com.example.sereno.features.call.domain.model.OpenAiTTsRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class OpenAiClient {
    private val client: OkHttpClient = OkHttpClient()

    companion object {
        private const val TAG = "ApiClient"
        private const val OPEN_API_KEY =
            "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"
        private const val OPEN_AI_URL = "https://api.openai.com/v1/audio/speech"
        private const val OPEN_AI_CHAT_URL = "https://api.openai.com/v1/chat/completions"
        private const val OPEN_AI_TTS_MODEL = "tts-1"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val moshi: Moshi = Moshi.Builder().build()

    private val openAiTTsRequestAdapter: JsonAdapter<OpenAiTTsRequest> =
        moshi.adapter(OpenAiTTsRequest::class.java)

    fun executeOpenAiRequest(requestJson: String): ApiResult<String> {
        return try {
            val request = Request.Builder()
                .url(OPEN_AI_CHAT_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $OPEN_API_KEY")
                .post(requestJson.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ApiResult.Error("Open Ai request failed: ${response.code}")
                }

                response.body?.string()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty open ai response")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

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

