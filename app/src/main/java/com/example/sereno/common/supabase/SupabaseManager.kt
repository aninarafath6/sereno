package com.example.sereno.common.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.MoshiSerializer

object SupabaseManager {
    private lateinit var supabaseClient: SupabaseClient
    fun init() {
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://gyuzsefcgihqogcxfbex.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd5dXpzZWZjZ2locW9nY3hmYmV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk5OTA2NDMsImV4cCI6MjA1NTU2NjY0M30.d0bdlndsL0yyp2sjiMOAbQbRb9anG8NGzRWXoTj1gg0"
        ) {
            install(Postgrest)
            install(Auth)
            defaultSerializer = MoshiSerializer()
        }
    }

    suspend fun signInWithGoogle(googleIdToken: String, rawNonce: String) {
        supabaseClient.auth.awaitInitialization()
        supabaseClient.auth.signInWith(IDToken) {
            idToken = googleIdToken
            provider = Google
            nonce = rawNonce

        }
    }

    fun getClient(): SupabaseClient {
        return supabaseClient
    }

    suspend fun isUserAuthenticated(): Boolean {
        supabaseClient.auth.awaitInitialization()
        return supabaseClient.auth.currentSessionOrNull() != null
    }
}