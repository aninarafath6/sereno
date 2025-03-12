package com.example.sereno.features.call.domain.error

enum class CallErrorActionType {
    RETRY,
    SETTINGS,
    ALLOW
}

sealed class CallError(
    val title: String,
    val description: String,
    val actionText: String,
    val actionType: CallErrorActionType = CallErrorActionType.RETRY
) : Exception(description) {

    data class MicrophonePermissionRequired(val shouldShowRational: Boolean) : CallError(
        title = "Microphone Permission Required",
        description = "Please grant microphone access to continue",
        actionText = if (shouldShowRational) "Allow" else "Settings",
        actionType = if (shouldShowRational) CallErrorActionType.ALLOW else CallErrorActionType.SETTINGS
    )

    data object NoNetwork : CallError(
        title = "No Network",
        description = "Please check your internet connection",
        actionText = "Retry"
    )

    data object Unknown : CallError(
        title = "Something went wrong",
        description = "Please try again",
        actionText = "Retry"
    )
}

