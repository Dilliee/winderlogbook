package com.winderlogbook.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreService {
    
    private val db: FirebaseFirestore = Firebase.firestore
    private val TAG = "FirestoreService"
    
    companion object {
        @Volatile
        private var INSTANCE: FirestoreService? = null
        
        fun getInstance(): FirestoreService {
            return INSTANCE ?: synchronized(this) {
                val instance = FirestoreService()
                INSTANCE = instance
                instance
            }
        }
    }
    
    // Logbook Entries
    suspend fun saveLogbookEntry(entry: Map<String, Any>): Boolean {
        return try {
            val entryWithTimestamp = entry.toMutableMap()
            entryWithTimestamp["timestamp"] = System.currentTimeMillis()
            entryWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("logbook_entries")
                .add(entryWithTimestamp)
                .await()
            
            Log.d(TAG, "Logbook entry saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving logbook entry", e)
            false
        }
    }
    
    suspend fun getLogbookEntries(): List<Map<String, Any>> {
        return try {
            val result = db.collection("logbook_entries")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching logbook entries", e)
            emptyList()
        }
    }
    
    // Inspections
    suspend fun saveInspection(inspection: Map<String, Any>): Boolean {
        return try {
            val inspectionWithTimestamp = inspection.toMutableMap()
            inspectionWithTimestamp["timestamp"] = System.currentTimeMillis()
            inspectionWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("inspections")
                .add(inspectionWithTimestamp)
                .await()
            
            Log.d(TAG, "Inspection saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving inspection", e)
            false
        }
    }
    
    suspend fun getInspections(): List<Map<String, Any>> {
        return try {
            val result = db.collection("inspections")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching inspections", e)
            emptyList()
        }
    }
    
    // Maintenance Records
    suspend fun saveMaintenance(maintenance: Map<String, Any>): Boolean {
        return try {
            val maintenanceWithTimestamp = maintenance.toMutableMap()
            maintenanceWithTimestamp["timestamp"] = System.currentTimeMillis()
            maintenanceWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("maintenance_records")
                .add(maintenanceWithTimestamp)
                .await()
            
            Log.d(TAG, "Maintenance record saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving maintenance record", e)
            false
        }
    }
    
    suspend fun getMaintenanceRecords(): List<Map<String, Any>> {
        return try {
            val result = db.collection("maintenance_records")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching maintenance records", e)
            emptyList()
        }
    }
    
    // Users
    suspend fun saveUser(user: Map<String, Any>): Boolean {
        return try {
            val userId = user["employeeNumber"] ?: user["id"] ?: System.currentTimeMillis().toString()
            
            db.collection("users")
                .document(userId.toString())
                .set(user)
                .await()
            
            Log.d(TAG, "User saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            false
        }
    }
    
    suspend fun getUser(employeeNumber: String): Map<String, Any>? {
        return try {
            val result = db.collection("users")
                .document(employeeNumber)
                .get()
                .await()
            
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user", e)
            null
        }
    }
    
    // Trip Counters
    suspend fun saveTripCounters(tripData: Map<String, Any>): Boolean {
        return try {
            val dataWithTimestamp = tripData.toMutableMap()
            dataWithTimestamp["timestamp"] = System.currentTimeMillis()
            dataWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("trip_counters")
                .add(dataWithTimestamp)
                .await()
            
            Log.d(TAG, "Trip counters saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving trip counters", e)
            false
        }
    }

    // Component Status
    suspend fun saveComponentStatus(statusData: Map<String, Any>): Boolean {
        return try {
            val dataWithTimestamp = statusData.toMutableMap()
            dataWithTimestamp["timestamp"] = System.currentTimeMillis()
            dataWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("component_status")
                .add(dataWithTimestamp)
                .await()
            
            Log.d(TAG, "Component status saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving component status", e)
            false
        }
    }

    // Biometric Signatures
    suspend fun saveBiometricSignature(signatureData: Map<String, Any>): Boolean {
        return try {
            val dataWithTimestamp = signatureData.toMutableMap()
            dataWithTimestamp["timestamp"] = System.currentTimeMillis()
            dataWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("biometric_signatures")
                .add(dataWithTimestamp)
                .await()
            
            Log.d(TAG, "Biometric signature saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving biometric signature", e)
            false
        }
    }

    // Shifts Management
    suspend fun saveShiftData(shiftData: Map<String, Any>): Boolean {
        return try {
            val dataWithTimestamp = shiftData.toMutableMap()
            dataWithTimestamp["timestamp"] = System.currentTimeMillis()
            dataWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            db.collection("shifts")
                .add(dataWithTimestamp)
                .await()
            
            Log.d(TAG, "Shift data saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving shift data", e)
            false
        }
    }

    // Emergency Logs
    suspend fun saveEmergencyLog(emergencyData: Map<String, Any>): Boolean {
        return try {
            val dataWithTimestamp = emergencyData.toMutableMap()
            dataWithTimestamp["timestamp"] = System.currentTimeMillis()
            dataWithTimestamp["date"] = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            dataWithTimestamp["priority"] = "high"
            
            db.collection("emergency_logs")
                .add(dataWithTimestamp)
                .await()
            
            Log.d(TAG, "Emergency log saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving emergency log", e)
            false
        }
    }

    // Dashboard Statistics (Enhanced)
    suspend fun getDashboardStats(): Map<String, Any> {
        return try {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            // Get today's data from all collections
            val todayLogbookEntries = db.collection("logbook_entries")
                .whereEqualTo("date", today)
                .get()
                .await()
            
            val todayTripCounters = db.collection("trip_counters")
                .whereEqualTo("date", today)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            val todayComponentStatus = db.collection("component_status")
                .whereEqualTo("date", today)
                .get()
                .await()
            
            val todayEmergencyLogs = db.collection("emergency_logs")
                .whereEqualTo("date", today)
                .get()
                .await()
            
            // Calculate stats
            var operationsToday = 0
            var personnelTransported = 0
            var materialTrips = 0
            var mineralTrips = 0
            var explosivesTrips = 0
            var incidents = todayEmergencyLogs.size()
            var faultyComponents = 0
            var maintenanceRequired = 0
            
            // Process trip counters
            if (!todayTripCounters.isEmpty) {
                val latestTripData = todayTripCounters.documents[0].data
                val counters = latestTripData?.get("counters") as? Map<*, *>
                if (counters != null) {
                    personnelTransported = (counters["persons"] as? Number)?.toInt() ?: 0
                    materialTrips = (counters["material"] as? Number)?.toInt() ?: 0
                    mineralTrips = (counters["mineral"] as? Number)?.toInt() ?: 0
                    explosivesTrips = (counters["explosives"] as? Number)?.toInt() ?: 0
                    operationsToday = personnelTransported + materialTrips + mineralTrips + explosivesTrips
                }
            }
            
            // Process component status
            for (entry in todayComponentStatus.documents) {
                val data = entry.data ?: continue
                val status = data["status"] as? String
                when (status) {
                    "faulty" -> faultyComponents++
                    "attention" -> maintenanceRequired++
                }
            }
            
            mapOf(
                "operationsToday" to operationsToday,
                "personnelTransported" to personnelTransported,
                "materialTrips" to materialTrips,
                "mineralTrips" to mineralTrips,
                "explosivesTrips" to explosivesTrips,
                "incidents" to incidents,
                "faultyComponents" to faultyComponents,
                "maintenanceRequired" to maintenanceRequired,
                "lastUpdated" to System.currentTimeMillis(),
                "date" to today
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching dashboard stats", e)
            mapOf(
                "operationsToday" to 0,
                "personnelTransported" to 0,
                "materialTrips" to 0,
                "mineralTrips" to 0,
                "explosivesTrips" to 0,
                "incidents" to 0,
                "faultyComponents" to 0,
                "maintenanceRequired" to 0,
                "lastUpdated" to System.currentTimeMillis(),
                "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
            )
        }
    }

    // Get data for web dashboard (last 30 days)
    suspend fun getWebDashboardData(): Map<String, Any> {
        return try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            
            // Get recent data from all collections
            val recentLogbookEntries = db.collection("logbook_entries")
                .whereGreaterThan("timestamp", thirtyDaysAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val recentTripCounters = db.collection("trip_counters")
                .whereGreaterThan("timestamp", thirtyDaysAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val recentComponentStatus = db.collection("component_status")
                .whereGreaterThan("timestamp", thirtyDaysAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val recentEmergencyLogs = db.collection("emergency_logs")
                .whereGreaterThan("timestamp", thirtyDaysAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val recentShifts = db.collection("shifts")
                .whereGreaterThan("timestamp", thirtyDaysAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            // Convert to format suitable for web dashboard
            val logbookData = recentLogbookEntries.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            
            val tripData = recentTripCounters.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            
            val componentData = recentComponentStatus.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            
            val emergencyData = recentEmergencyLogs.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            
            val shiftData = recentShifts.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            
            mapOf(
                "logbook_entries" to logbookData,
                "trip_counters" to tripData,
                "component_status" to componentData,
                "emergency_logs" to emergencyData,
                "shifts" to shiftData,
                "summary" to getDashboardStats(),
                "lastUpdated" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching web dashboard data", e)
            mapOf(
                "error" to "Failed to fetch dashboard data",
                "lastUpdated" to System.currentTimeMillis()
            )
        }
    }
    
    // Checklist Data
    suspend fun saveChecklistData(checklistData: com.google.gson.JsonObject): Boolean {
        return try {
            val checklistMap = mutableMapOf<String, Any>()
            
            // Extract checklist data
            checklistData.entrySet().forEach { (key, value) ->
                when {
                    value.isJsonPrimitive -> {
                        val primitive = value.asJsonPrimitive
                        checklistMap[key] = when {
                            primitive.isString -> primitive.asString
                            primitive.isNumber -> primitive.asDouble
                            primitive.isBoolean -> primitive.asBoolean
                            else -> primitive.asString
                        }
                    }
                    value.isJsonObject -> {
                        checklistMap[key] = convertJsonObjectToMap(value.asJsonObject)
                    }
                    value.isJsonArray -> {
                        checklistMap[key] = value.asJsonArray.map { 
                            if (it.isJsonObject) convertJsonObjectToMap(it.asJsonObject) else it.asString 
                        }
                    }
                    else -> {
                        checklistMap[key] = value.toString()
                    }
                }
            }
            
            checklistMap["createdAt"] = com.google.firebase.Timestamp.now()
            
            db.collection("checklists")
                .add(checklistMap)
                .await()
            
            Log.d(TAG, "Checklist data saved successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving checklist data", e)
            false
        }
    }
    
    // Notifications
    suspend fun saveNotification(notification: com.google.gson.JsonObject): Boolean {
        return try {
            val notificationData = mutableMapOf<String, Any>()
            
            // Extract notification data
            notification.entrySet().forEach { (key, value) ->
                when {
                    value.isJsonPrimitive -> {
                        val primitive = value.asJsonPrimitive
                        notificationData[key] = when {
                            primitive.isString -> primitive.asString
                            primitive.isNumber -> primitive.asDouble
                            primitive.isBoolean -> primitive.asBoolean
                            else -> primitive.asString
                        }
                    }
                    value.isJsonArray -> {
                        notificationData[key] = value.asJsonArray.map { it.asString }
                    }
                    else -> {
                        notificationData[key] = value.toString()
                    }
                }
            }
            
            notificationData["createdAt"] = com.google.firebase.Timestamp.now()
            notificationData["processed"] = false
            
            db.collection("notifications")
                .add(notificationData)
                .await()
            
            Log.d(TAG, "Notification saved successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification", e)
            false
        }
    }
    
    private fun convertJsonObjectToMap(jsonObject: com.google.gson.JsonObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        jsonObject.entrySet().forEach { (key, value) ->
            when {
                value.isJsonPrimitive -> {
                    val primitive = value.asJsonPrimitive
                    map[key] = when {
                        primitive.isString -> primitive.asString
                        primitive.isNumber -> primitive.asDouble
                        primitive.isBoolean -> primitive.asBoolean
                        else -> primitive.asString
                    }
                }
                value.isJsonObject -> {
                    map[key] = convertJsonObjectToMap(value.asJsonObject)
                }
                value.isJsonArray -> {
                    map[key] = value.asJsonArray.map { 
                        if (it.isJsonObject) convertJsonObjectToMap(it.asJsonObject) else it.asString 
                    }
                }
                else -> {
                    map[key] = value.toString()
                }
            }
        }
        return map
    }
    
    // User Management Functions
    suspend fun getUsers(): List<Map<String, Any>> {
        return try {
            val result = db.collection("users")
                .whereEqualTo("status", "active")
                .get()
                .await()
            
            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users", e)
            emptyList()
        }
    }
    
    suspend fun syncUsersToLocalStorage(): String {
        return try {
            val users = getUsers()
            val gson = com.google.gson.Gson()
            val usersJson = gson.toJson(users)
            Log.d(TAG, "Synced ${users.size} users to local storage")
            usersJson
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users", e)
            "[]"
        }
    }
}
