package com.example.sereno.features.onboarding.domain

import android.content.Context
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.common.supabase.SupabaseManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class OnboardingViewModel : ViewModel() {
    private lateinit var credentialManager: CredentialManager
    private val isLoading = MutableLiveData(false)


    fun isLoading(): LiveData<Boolean> = isLoading

    fun init(context: Context) {
        credentialManager = CredentialManager.create(context)
    }

    fun loginWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onFail: (message: String) -> Unit
    ) {

        if (!::credentialManager.isInitialized) {
            Toast.makeText(context, "Credential Manager is not initialized", Toast.LENGTH_SHORT)
                .show()
            return
        }
        isLoading.value = true

        viewModelScope.launch {
            try {
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hasNonce = digest.joinToString("") { "%02x".format(it) }

                val googleIdOptions = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("760233743157-it92oqiev68l9uuab1911co1dopcu1ge.apps.googleusercontent.com")
                    .setNonce(hasNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptions)
                    .build()

                val result = credentialManager.getCredential(request = request, context = context)
                val cred = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                val googleIdToken = googleIdTokenCredential.idToken
                SupabaseManager.signInWithGoogle(googleIdToken, rawNonce)
                onSuccess()
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                onFail("Login cancelled")
                isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                onFail("Login failed")
                isLoading.value = false
            }
        }
    }
}