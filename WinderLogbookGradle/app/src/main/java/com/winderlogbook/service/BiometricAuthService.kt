package com.winderlogbook.service

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricAuthService(private val activity: FragmentActivity) {
    
    private val TAG = "BiometricAuthService"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    init {
        setupBiometricPrompt()
    }
    
    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(activity)
        
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Authentication error: $errString")
                    onAuthenticationResult(false, "Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded!")
                    onAuthenticationResult(true, "Authentication successful")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Authentication failed")
                    onAuthenticationResult(false, "Authentication failed - please try again")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Winder Logbook Authentication")
            .setSubtitle("Use your fingerprint or face to access the logbook")
            .setDescription("Place your finger on the sensor or look at the camera to authenticate")
            .setNegativeButtonText("Use PIN/Password")
            .build()
    }
    
    /**
     * Check if biometric authentication is available on this device
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "No biometric features available on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "Biometric features are currently unavailable.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(TAG, "The user hasn't associated any biometric credentials with their account.")
                false
            }
            else -> {
                Log.e(TAG, "Unknown biometric status")
                false
            }
        }
    }
    
    /**
     * Get detailed biometric availability status
     */
    fun getBiometricStatus(): String {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "no_hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "hardware_unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "none_enrolled"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "security_update_required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "unsupported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "unknown"
            else -> "error"
        }
    }
    
    /**
     * Start biometric authentication
     */
    fun authenticate() {
        if (isBiometricAvailable()) {
            Log.d(TAG, "Starting biometric authentication")
            biometricPrompt.authenticate(promptInfo)
        } else {
            Log.w(TAG, "Biometric authentication not available")
            onAuthenticationResult(false, "Biometric authentication not available on this device")
        }
    }
    
    /**
     * Callback for authentication results - to be overridden by JavaScript interface
     */
    var onAuthenticationResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    
    /**
     * Set custom authentication callback
     */
    fun setAuthenticationCallback(callback: (success: Boolean, message: String) -> Unit) {
        onAuthenticationResult = callback
    }
    
    /**
     * Check if device supports any form of biometric authentication
     */
    fun supportsAnyBiometric(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        val strongAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val weakAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        
        return strongAuth == BiometricManager.BIOMETRIC_SUCCESS || 
               weakAuth == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Get user-friendly message about biometric availability
     */
    fun getBiometricStatusMessage(): String {
        return when (getBiometricStatus()) {
            "available" -> "Biometric authentication is available"
            "no_hardware" -> "This device doesn't support biometric authentication"
            "hardware_unavailable" -> "Biometric sensor is currently unavailable"
            "none_enrolled" -> "No fingerprint or face data enrolled. Please set up biometric authentication in device settings"
            "security_update_required" -> "Biometric authentication requires a security update"
            "unsupported" -> "Biometric authentication is not supported"
            "unknown" -> "Biometric status unknown"
            else -> "Biometric authentication error"
        }
    }
}
