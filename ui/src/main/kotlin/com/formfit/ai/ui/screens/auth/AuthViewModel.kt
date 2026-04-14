package com.formfit.ai.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val magicLinkSent: Boolean = false
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
            try {
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authRepository.signInWithGoogle(
                        idToken = googleIdTokenCredential.idToken,
                        rawNonce = rawNonce
                    ).onSuccess {
                        _uiState.update { it.copy(isGoogleLoading = false, isAuthenticated = true) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isGoogleLoading = false, errorMessage = "Google sign-in failed: ${e.message}") }
                    }
                } else {
                    _uiState.update { it.copy(isGoogleLoading = false, errorMessage = "Unexpected credential type") }
                }
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(isGoogleLoading = false, errorMessage = "Google sign-in unavailable: ${e.message}") }
            } catch (e: GoogleIdTokenParsingException) {
                _uiState.update { it.copy(isGoogleLoading = false, errorMessage = "Invalid Google token") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGoogleLoading = false, errorMessage = "Sign-in failed: ${e.message}") }
            }
        }
    }

    fun sendMagicLink(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email first") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.sendMagicLink(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, magicLinkSent = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to send magic link: ${e.message}") } }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        const val GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID"
    }
}
