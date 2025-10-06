package com.winderlogbook.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.PermissionChecker
import java.util.Locale

class VoiceToTextService(private val context: Context) {
    
    companion object {
        private const val TAG = "VoiceToTextService"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    interface VoiceRecognitionCallback {
        fun onResult(text: String)
        fun onError(error: String)
        fun onStart()
        fun onEnd()
    }
    
    fun isVoiceRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    fun hasAudioPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(
            context, 
            android.Manifest.permission.RECORD_AUDIO
        ) == PermissionChecker.PERMISSION_GRANTED
    }
    
    fun startListening(callback: VoiceRecognitionCallback) {
        if (!hasAudioPermission()) {
            callback.onError("Audio permission not granted")
            return
        }
        
        if (!isVoiceRecognitionAvailable()) {
            callback.onError("Voice recognition not available")
            return
        }
        
        if (isListening) {
            stopListening()
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    callback.onStart()
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started")
                    isListening = true
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Volume level changed - could be used for visual feedback
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "Speech ended")
                    isListening = false
                    callback.onEnd()
                }
                
                override fun onError(error: Int) {
                    Log.e(TAG, "Speech recognition error: $error")
                    isListening = false
                    
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech input was detected"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    
                    callback.onError(errorMessage)
                }
                
                override fun onResults(results: Bundle?) {
                    Log.d(TAG, "Speech recognition results received")
                    isListening = false
                    
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Recognized text: $recognizedText")
                        callback.onResult(recognizedText)
                    } else {
                        callback.onError("No speech recognized")
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results - could be used for real-time feedback
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        Log.d(TAG, "Partial result: ${matches[0]}")
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Additional events
                }
            }
            
            speechRecognizer?.setRecognitionListener(recognitionListener)
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your maintenance notes...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Enable partial results for real-time feedback
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.startListening(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            callback.onError("Failed to start voice recognition: ${e.message}")
        }
    }
    
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
            Log.d(TAG, "Speech recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }
    
    fun isCurrentlyListening(): Boolean {
        return isListening
    }
    
    fun cleanup() {
        stopListening()
    }
}
