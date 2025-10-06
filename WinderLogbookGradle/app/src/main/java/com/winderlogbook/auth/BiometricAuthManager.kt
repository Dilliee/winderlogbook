package com.winderlogbook.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class BiometricAuthManager(private val context: Context) {

    companion object {
        const val SHARED_PREFS_NAME = "winder_logbook_auth"
        const val KEY_USER_AUTHENTICATED = "user_authenticated"
        const val KEY_LAST_AUTH_TIME = "last_auth_time"
        const val AUTH_VALIDITY_DURATION = 8 * 60 * 60 * 1000L // 8 hours in milliseconds
    }

    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun isBiometricAvailable(): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }

    fun getBiometricStatusMessage(): String {
        return when (isBiometricAvailable()) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric credentials enrolled"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric authentication unsupported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Biometric status unknown"
            else -> "Unknown biometric status"
        }
    }

    fun authenticateUser(
        activity: FragmentActivity,
        title: String = "Winder Logbook Authentication",
        subtitle: String = "Authenticate to access the Digital Winding Engine Driver Logbook",
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        if (isBiometricAvailable() != BiometricManager.BIOMETRIC_SUCCESS) {
            onError(getBiometricStatusMessage())
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    
                    // Save authentication state
                    saveAuthenticationState()
                    
                    // Get current user based on shift time
                    val currentUser = getCurrentShiftUser()
                    onSuccess("Authentication successful for $currentUser")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isUserAuthenticated(): Boolean {
        val isAuthenticated = sharedPreferences.getBoolean(KEY_USER_AUTHENTICATED, false)
        val lastAuthTime = sharedPreferences.getLong(KEY_LAST_AUTH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        
        // Check if authentication is still valid (within 8 hours)
        return isAuthenticated && (currentTime - lastAuthTime) < AUTH_VALIDITY_DURATION
    }

    fun saveAuthenticationState() {
        sharedPreferences.edit()
            .putBoolean(KEY_USER_AUTHENTICATED, true)
            .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
            .apply()
    }

    fun clearAuthenticationState() {
        sharedPreferences.edit()
            .putBoolean(KEY_USER_AUTHENTICATED, false)
            .putLong(KEY_LAST_AUTH_TIME, 0)
            .apply()
    }

    fun getCurrentShiftUser(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..13 -> "Bays Draganovic"
            hour in 14..21 -> "Elsa Nitandara" 
            else -> "Carl Renarafo"
        }
    }

    fun getCurrentShift(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..13 -> "Morning"
            hour in 14..21 -> "Afternoon"
            else -> "Night"
        }
    }

    fun requiresReAuthentication(): Boolean {
        return !isUserAuthenticated()
    }

    fun getAuthenticationTimeRemaining(): Long {
        val lastAuthTime = sharedPreferences.getLong(KEY_LAST_AUTH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastAuthTime
        return maxOf(0, AUTH_VALIDITY_DURATION - elapsed)
    }

    fun getAuthenticationTimeRemainingFormatted(): String {
        val remaining = getAuthenticationTimeRemaining()
        val hours = remaining / (60 * 60 * 1000)
        val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
        return "${hours}h ${minutes}m"
    }
}
