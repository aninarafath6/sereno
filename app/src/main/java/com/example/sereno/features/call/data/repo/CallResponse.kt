package com.example.sereno.features.call.data.repo

import com.example.sereno.features.call.domain.error.CallError
import java.io.File
import java.lang.Error

sealed class CallResponse {
    data class Success(val audioFile: File) : CallResponse()
    data class Failed(val error: CallError) : CallResponse()
}