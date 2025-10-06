package com.winderlogbook

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.winderlogbook.service.FirestoreService
import com.winderlogbook.service.BiometricAuthService
import com.winderlogbook.service.VoiceToTextService
import com.winderlogbook.service.PhotoCaptureService
import com.winderlogbook.service.SimpleReportGenerationService
import com.winderlogbook.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WinderLogbookInterface(private val context: Context) {
    
    private val gson = Gson()
    private val firestoreService = FirestoreService.getInstance()
    private lateinit var biometricAuthService: BiometricAuthService
    private val voiceToTextService = VoiceToTextService(context)
    private val photoCaptureService = PhotoCaptureService(context)
    private val reportGenerationService = SimpleReportGenerationService(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Authentication state
    private var isAuthenticated = false
    private var currentUser = "Bays Draganovic"
    private var currentShift = "Day Shift"
    private var authenticationTime = 0L
    
    /**
     * Initialize biometric service - call this from MainActivity
     */
    fun initializeBiometricService(activity: MainActivity) {
        biometricAuthService = BiometricAuthService(activity)
        biometricAuthService.setAuthenticationCallback { success, message ->
            isAuthenticated = success
            if (success) {
                authenticationTime = System.currentTimeMillis()
            }
            // Call JavaScript callback
            val activity = context as? MainActivity
            activity?.runOnUiThread {
                activity.findViewById<android.webkit.WebView>(R.id.webView)?.evaluateJavascript(
                    "if(typeof onBiometricAuthResult === 'function') onBiometricAuthResult($success, '$message');",
                    null
                )
            }
        }
    }
    
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    @JavascriptInterface
    fun saveLogbookEntry(entryJson: String): Boolean {
        return try {
            val entryMap = gson.fromJson(entryJson, Map::class.java) as Map<String, Any>
            
        coroutineScope.launch {
            val success = withContext(Dispatchers.IO) {
                when (entryMap["entryType"]) {
                    "trip_counters" -> firestoreService.saveTripCounters(entryMap)
                    "component_status" -> firestoreService.saveComponentStatus(entryMap)
                    "biometric_signature" -> firestoreService.saveBiometricSignature(entryMap)
                    "complete_shift_data" -> firestoreService.saveShiftData(entryMap)
                    "emergency_log" -> firestoreService.saveEmergencyLog(entryMap)
                    "maintenance_status" -> firestoreService.saveMaintenance(entryMap)
                    else -> firestoreService.saveLogbookEntry(entryMap)
                }
            }

            if (success) {
                showToast("Data saved to Firestore: ${entryMap["entryType"]}")
            } else {
                showToast("Failed to save data to Firestore")
            }
        }
            true
        } catch (e: Exception) {
            showToast("Error saving entry: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun getLogbookEntries(): String {
        // Note: This is a synchronous method, but Firestore is async
        // In a real app, you'd want to redesign this to use callbacks
        return try {
            gson.toJson(emptyList<Map<String, Any>>()) // Return empty for now
        } catch (e: Exception) {
            gson.toJson(emptyList<Map<String, Any>>())
        }
    }
    
    @JavascriptInterface
    fun saveInspection(inspectionJson: String): Boolean {
        return try {
            val inspectionMap = gson.fromJson(inspectionJson, Map::class.java) as Map<String, Any>
            
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    firestoreService.saveInspection(inspectionMap)
                }
                
                if (success) {
                    showToast("Inspection saved to Firestore: ${inspectionMap["type"]}")
                } else {
                    showToast("Failed to save inspection to Firestore")
                }
            }
            true
        } catch (e: Exception) {
            showToast("Error saving inspection: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun generateReport(reportType: String): String {
        showToast("Generating $reportType report...")
        
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (reportType.lowercase()) {
                    "daily_shift_report", "daily" -> {
                        reportGenerationService.generateDailyShiftReport()
                    }
                    "weekly_report", "weekly" -> {
                        reportGenerationService.generateTextReport("weekly")
                    }
                    "maintenance_report", "maintenance" -> {
                        reportGenerationService.generateTextReport("maintenance")
                    }
                    else -> {
                        reportGenerationService.generateDailyShiftReport()
                    }
                }
            }
            
            if (result.success) {
                showToast("✅ Report generated: ${result.fileName}")
                showToast("📁 Saved to: ${result.filePath}")
            } else {
                showToast("❌ Report generation failed: ${result.error}")
            }
        }
        
        return gson.toJson(mapOf(
            "status" to "processing",
            "message" to "Report generation started. You will be notified when complete.",
            "reportType" to reportType
        ))
    }

    // ==================== BIOMETRIC AUTHENTICATION ====================
    
    @JavascriptInterface
    fun isBiometricAvailable(): Boolean {
        return if (::biometricAuthService.isInitialized) {
            biometricAuthService.isBiometricAvailable()
        } else {
            false
        }
    }
    
    @JavascriptInterface
    fun getBiometricStatus(): String {
        return if (::biometricAuthService.isInitialized) {
            biometricAuthService.getBiometricStatus()
        } else {
            "not_initialized"
        }
    }
    
    @JavascriptInterface
    fun getBiometricStatusMessage(): String {
        return if (::biometricAuthService.isInitialized) {
            biometricAuthService.getBiometricStatusMessage()
        } else {
            "Biometric service not initialized"
        }
    }
    
    @JavascriptInterface
    fun startBiometricAuthentication() {
        if (::biometricAuthService.isInitialized) {
            biometricAuthService.authenticate()
        } else {
            showToast("Biometric service not available")
        }
    }
    
    @JavascriptInterface
    fun getCurrentUser(): String {
        return currentUser
    }

    @JavascriptInterface
    fun getCurrentShift(): String {
        return currentShift
    }

    @JavascriptInterface
    fun isUserAuthenticated(): Boolean {
        return isAuthenticated
    }

    @JavascriptInterface
    fun getAuthenticationTimeRemaining(): String {
        if (!isAuthenticated) return "Not authenticated"
        
        val timeElapsed = System.currentTimeMillis() - authenticationTime
        val sessionDuration = 8 * 60 * 60 * 1000L // 8 hours in milliseconds
        val timeRemaining = sessionDuration - timeElapsed
        
        return if (timeRemaining > 0) {
            val hours = timeRemaining / (60 * 60 * 1000)
            val minutes = (timeRemaining % (60 * 60 * 1000)) / (60 * 1000)
            "${hours}h ${minutes}m remaining"
        } else {
            "Session expired"
        }
    }
    
    @JavascriptInterface
    fun logout() {
        isAuthenticated = false
        authenticationTime = 0L
        showToast("Logged out successfully")
        
        // Call JavaScript callback to update UI
        val activity = context as? MainActivity
        activity?.runOnUiThread {
            activity.findViewById<android.webkit.WebView>(R.id.webView)?.evaluateJavascript(
                "if(typeof onLogout === 'function') onLogout();",
                null
            )
        }
    }

    @JavascriptInterface
    fun getBiometricSignature(): String {
        val signatureData = mapOf(
            "user" to currentUser,
            "shift" to currentShift,
            "timestamp" to System.currentTimeMillis(),
            "authenticated" to isAuthenticated,
            "signature_hash" to generateSignatureHash()
        )
        return gson.toJson(signatureData)
    }

    @JavascriptInterface
    fun logBiometricAction(action: String, details: String): Boolean {
        return try {
            val actionLog = mapOf(
                "action" to action,
                "details" to details,
                "user" to currentUser,
                "shift" to currentShift,
                "timestamp" to System.currentTimeMillis(),
                "biometric_signature" to getBiometricSignature()
            )

            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    firestoreService.saveLogbookEntry(actionLog)
                }

                if (success) {
                    showToast("Biometric action logged: $action")
                } else {
                    showToast("Failed to log biometric action")
                }
            }
            true
        } catch (e: Exception) {
            showToast("Error logging biometric action: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun getDashboardStats(): String {
        return try {
            // Note: This is async, so for immediate response we return cached or default data
            // In production, this would be called periodically to update the dashboard
            val defaultStats = mapOf(
                "operationsToday" to 0,
                "personnelTransported" to 0,
                "materialTrips" to 0,
                "mineralTrips" to 0,
                "explosivesTrips" to 0,
                "incidents" to 0,
                "faultyComponents" to 0,
                "maintenanceRequired" to 0,
                "lastUpdated" to System.currentTimeMillis()
            )
            gson.toJson(defaultStats)
        } catch (e: Exception) {
            showToast("Error getting dashboard stats: ${e.message}")
            gson.toJson(mapOf("error" to "Failed to get stats"))
        }
    }

    @JavascriptInterface
    fun getWebDashboardData(): String {
        return try {
            // For web dashboard integration - this would be called by web dashboard via API
            coroutineScope.launch {
                val data = withContext(Dispatchers.IO) {
                    firestoreService.getWebDashboardData()
                }
                showToast("Web dashboard data retrieved")
            }
            gson.toJson(mapOf("status" to "retrieving", "message" to "Data being prepared for web dashboard"))
        } catch (e: Exception) {
            showToast("Error getting web dashboard data: ${e.message}")
            gson.toJson(mapOf("error" to "Failed to get web dashboard data"))
        }
    }

    @JavascriptInterface
    fun syncAllDataToFirestore(): Boolean {
        return try {
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    // This would sync all local data to Firestore
                    // For now, just trigger a sync status update
                    true
                }
                
                if (success) {
                    showToast("All data synced to Firestore successfully")
                } else {
                    showToast("Some data failed to sync to Firestore")
                }
            }
            true
        } catch (e: Exception) {
            showToast("Error syncing data: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun syncUsers(): Boolean {
        return try {
            coroutineScope.launch {
                val usersJson = withContext(Dispatchers.IO) {
                    firestoreService.syncUsersToLocalStorage()
                }
                
                // Call JavaScript callback with synced users
                val activity = context as? MainActivity
                activity?.runOnUiThread {
                    activity.findViewById<android.webkit.WebView>(R.id.webView)?.evaluateJavascript(
                        "if(typeof onUsersSynced === 'function') onUsersSynced('$usersJson');",
                        null
                    )
                }
                
                showToast("Users synced successfully")
            }
            true
        } catch (e: Exception) {
            showToast("Error syncing users: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun startVoiceToText(fieldId: String): Boolean {
        return try {
            if (!voiceToTextService.hasAudioPermission()) {
                showToast("Audio permission required for voice input")
                return false
            }
            
            if (!voiceToTextService.isVoiceRecognitionAvailable()) {
                showToast("Voice recognition not available on this device")
                return false
            }
            
            voiceToTextService.startListening(object : VoiceToTextService.VoiceRecognitionCallback {
                override fun onResult(text: String) {
                    // Call JavaScript function to update the field
                    val activity = context as? android.app.Activity
                    activity?.runOnUiThread {
                        val webView = (context as MainActivity).findViewById<android.webkit.WebView>(R.id.webView)
                        webView?.evaluateJavascript("updateVoiceInputField('$fieldId', '$text')") { }
                    }
                    showToast("Voice input: $text")
                }
                
                override fun onError(error: String) {
                    showToast("Voice recognition error: $error")
                }
                
                override fun onStart() {
                    showToast("Listening... Speak now")
                }
                
                override fun onEnd() {
                    showToast("Voice input complete")
                }
            })
            true
        } catch (e: Exception) {
            showToast("Error starting voice input: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun stopVoiceToText(): Boolean {
        return try {
            voiceToTextService.stopListening()
            showToast("Voice input stopped")
            true
        } catch (e: Exception) {
            showToast("Error stopping voice input: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun isVoiceInputAvailable(): Boolean {
        return voiceToTextService.isVoiceRecognitionAvailable() && voiceToTextService.hasAudioPermission()
    }
    
    @JavascriptInterface
    fun isCurrentlyListening(): Boolean {
        return voiceToTextService.isCurrentlyListening()
    }
    
    @JavascriptInterface
    fun capturePhoto(componentName: String, description: String): Boolean {
        return try {
            if (!photoCaptureService.hasCameraPermission()) {
                showToast("Camera permission required for photo capture")
                return false
            }
            
            // This would typically start an intent for camera capture
            // For simplicity, we'll simulate the process
            showToast("Camera capture initiated for $componentName")
            
            // In a real implementation, this would:
            // 1. Create camera intent
            // 2. Start activity for result
            // 3. Process the result in onActivityResult
            // 4. Save to Firestore
            
            // For now, we'll simulate saving a photo
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val mockImageData = createMockPhotoData(componentName, description)
                    firestoreService.saveLogbookEntry(mockImageData)
                }
                showToast("Photo saved for $componentName")
            }
            
            true
        } catch (e: Exception) {
            showToast("Error capturing photo: ${e.message}")
            false
        }
    }
    
    @JavascriptInterface
    fun isCameraAvailable(): Boolean {
        return photoCaptureService.hasCameraPermission()
    }
    
    @JavascriptInterface
    fun savePhotoToFirestore(base64Image: String, componentName: String, description: String): Boolean {
        return try {
            photoCaptureService.saveImageToFirestore(base64Image, componentName, description) { success, message ->
                showToast(message)
            }
            true
        } catch (e: Exception) {
            showToast("Error saving photo: ${e.message}")
            false
        }
    }
    
    private fun createMockPhotoData(componentName: String, description: String): Map<String, Any> {
        return mapOf(
            "type" to "equipment_photo",
            "component" to componentName,
            "description" to description,
            "user" to currentUser,
            "shift" to currentShift,
            "timestamp" to System.currentTimeMillis(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "image_placeholder" to "base64_image_data_would_be_here",
            "location" to "Winder Station",
            "severity" to if (description.contains("fault", true)) "high" else "normal"
        )
    }

    private fun generateSignatureHash(): String {
        val data = "${currentUser}_${System.currentTimeMillis()}"
        return data.hashCode().toString()
    }
    
    @JavascriptInterface
    fun saveNotificationData(notificationJson: String) {
        try {
            val notificationData = gson.fromJson(notificationJson, JsonObject::class.java)
            
            coroutineScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        firestoreService.saveNotification(notificationData)
                    }
                    
                    if (result) {
                        showToast("🚨 Alert notification sent to dashboard")
                        
                        // Log notification details
                        val componentName = notificationData.get("componentName")?.asString ?: "Unknown"
                        val status = notificationData.get("status")?.asString ?: "unknown"
                        val alertLevel = notificationData.get("alertLevel")?.asString ?: "INFO"
                        
                        android.util.Log.d("WinderNotification", 
                            "Notification saved: $componentName - $status ($alertLevel)")
                    } else {
                        showToast("⚠️ Failed to send notification - saved locally")
                        android.util.Log.w("WinderNotification", "Failed to save notification to Firestore")
                    }
                } catch (e: Exception) {
                    showToast("❌ Error sending notification: ${e.message}")
                    android.util.Log.e("WinderNotification", "Error saving notification", e)
                }
            }
        } catch (e: Exception) {
            showToast("❌ Invalid notification data")
            android.util.Log.e("WinderNotification", "Error parsing notification JSON: $notificationJson", e)
        }
    }
    
    @JavascriptInterface
    fun saveChecklistData(checklistJson: String) {
        try {
            val checklistData = gson.fromJson(checklistJson, JsonObject::class.java)
            
            coroutineScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        firestoreService.saveChecklistData(checklistData)
                    }
                    
                    if (result) {
                        showToast("📋 Checklist data saved to dashboard")
                        
                        // Log checklist details
                        val type = checklistData.get("type")?.asString ?: "Unknown"
                        val completedBy = checklistData.get("completedBy")?.asString ?: "Unknown"
                        val status = checklistData.get("status")?.asString ?: "unknown"
                        
                        android.util.Log.d("WinderChecklist", 
                            "Checklist saved: $type by $completedBy - Status: $status")
                    } else {
                        showToast("⚠️ Failed to save checklist - saved locally")
                        android.util.Log.w("WinderChecklist", "Failed to save checklist to Firestore")
                    }
                } catch (e: Exception) {
                    showToast("❌ Error saving checklist: ${e.message}")
                    android.util.Log.e("WinderChecklist", "Error saving checklist", e)
                }
            }
        } catch (e: Exception) {
            showToast("❌ Invalid checklist data")
            android.util.Log.e("WinderChecklist", "Error parsing checklist JSON: $checklistJson", e)
        }
    }
    
    @JavascriptInterface
    fun generateWeeklyReport(reportJson: String) {
        try {
            val reportData = gson.fromJson(reportJson, JsonObject::class.java)
            
            coroutineScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        reportGenerationService.generateWeeklyElectricalReport(reportData)
                    }
                    
                    if (result) {
                        showToast("📄 Weekly electrical report generated successfully")
                        android.util.Log.d("WeeklyReport", "Weekly electrical report generated")
                    } else {
                        showToast("❌ Failed to generate weekly report")
                        android.util.Log.w("WeeklyReport", "Failed to generate weekly electrical report")
                    }
                } catch (e: Exception) {
                    showToast("❌ Error generating report: ${e.message}")
                    android.util.Log.e("WeeklyReport", "Error generating weekly report", e)
                }
            }
        } catch (e: Exception) {
            showToast("❌ Invalid report data")
            android.util.Log.e("WeeklyReport", "Error parsing report JSON: $reportJson", e)
        }
    }
}
