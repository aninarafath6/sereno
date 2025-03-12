package com.example.sereno.audio.speech_recognizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognizer {
    private lateinit var speechRecognizerListener: SpeechRecognizerListener
    private lateinit var speechRecognizer: SpeechRecognizer
    private fun hasSpeechRecognizerCapability(context: Context): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun setListener(listener: SpeechRecognizerListener) {
        this.speechRecognizerListener = listener
    }

    fun init(context: Context): Boolean {
        if (!hasSpeechRecognizerCapability(context)) return false
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
                if (::speechRecognizerListener.isInitialized) {
                    speechRecognizerListener.onSpeechError("Something went wrong! ${error}")
                }
            }

            override fun onResults(results: Bundle) {
                val data: ArrayList<String>? =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (::speechRecognizerListener.isInitialized) {
                    speechRecognizerListener.onSpeechRecognized(data.toString())
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

        })
        return true
    }

    fun listen() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Set the language model to free-form
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Set the language to US English
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")


            // Possibly complete silence length
            // Helps prevent cutting off during short pauses
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                1000 // 1 second of potentially complete silence
            )
        }

        speechRecognizer.startListening(recognizerIntent)
    }
}

interface SpeechRecognizerListener {
    fun onSpeechRecognized(text: String)
    fun onSpeechError(error: String)
}