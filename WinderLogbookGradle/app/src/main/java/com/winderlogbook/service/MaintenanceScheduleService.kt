package com.winderlogbook.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MaintenanceScheduleService {
    
    companion object {
        private const val TAG = "MaintenanceScheduleService"
        
        @Volatile
        private var INSTANCE: MaintenanceScheduleService? = null
        
        fun getInstance(): MaintenanceScheduleService {
            return INSTANCE ?: synchronized(this) {
                val instance = MaintenanceScheduleService()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private val firestoreService = FirestoreService.getInstance()
    private val gson = Gson()
    
    // Maintenance intervals in days
    data class MaintenanceInterval(
        val component: String,
        val intervalType: IntervalType,
        val intervalDays: Int,
        val description: String,
        val priority: Priority
    )
    
    enum class IntervalType {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    }
    
    enum class Priority {
        LOW, NORMAL, HIGH, CRITICAL
    }
    
    data class MaintenanceAlert(
        val component: String,
        val intervalType: IntervalType,
        val lastCompleted: Long,
        val nextDue: Long,
        val daysOverdue: Int,
        val priority: Priority,
        val description: String,
        val assignedTo: String = "",
        val escalationLevel: Int = 0
    )
    
    // Define maintenance schedules for each component
    private val maintenanceSchedules = listOf(
        MaintenanceInterval("Winder Motor", IntervalType.DAILY, 1, "Visual inspection and lubrication check", Priority.HIGH),
        MaintenanceInterval("Winder Motor", IntervalType.WEEKLY, 7, "Complete motor inspection and cleaning", Priority.HIGH),
        MaintenanceInterval("Winder Motor", IntervalType.MONTHLY, 30, "Motor alignment and bearing check", Priority.CRITICAL),
        
        MaintenanceInterval("Winder Drums", IntervalType.DAILY, 1, "Visual wear inspection", Priority.HIGH),
        MaintenanceInterval("Winder Drums", IntervalType.WEEKLY, 7, "Drum alignment and balance check", Priority.HIGH),
        MaintenanceInterval("Winder Drums", IntervalType.MONTHLY, 30, "Complete drum overhaul", Priority.CRITICAL),
        
        MaintenanceInterval("Ropes", IntervalType.DAILY, 1, "Visual rope condition check", Priority.CRITICAL),
        MaintenanceInterval("Ropes", IntervalType.WEEKLY, 7, "Rope tension and wear measurement", Priority.CRITICAL),
        MaintenanceInterval("Ropes", IntervalType.MONTHLY, 30, "Complete rope inspection and lubrication", Priority.CRITICAL),
        
        MaintenanceInterval("Clutches", IntervalType.DAILY, 1, "Clutch operation test", Priority.HIGH),
        MaintenanceInterval("Clutches", IntervalType.WEEKLY, 7, "Clutch adjustment and lubrication", Priority.HIGH),
        MaintenanceInterval("Clutches", IntervalType.QUARTERLY, 90, "Complete clutch overhaul", Priority.HIGH),
        
        MaintenanceInterval("Control Levers", IntervalType.DAILY, 1, "Operation and response test", Priority.NORMAL),
        MaintenanceInterval("Control Levers", IntervalType.WEEKLY, 7, "Lubrication and calibration", Priority.NORMAL),
        MaintenanceInterval("Control Levers", IntervalType.QUARTERLY, 90, "Complete lever system inspection", Priority.HIGH),
        
        MaintenanceInterval("Safety Devices", IntervalType.DAILY, 1, "Safety system function test", Priority.CRITICAL),
        MaintenanceInterval("Safety Devices", IntervalType.WEEKLY, 7, "Emergency stop system test", Priority.CRITICAL),
        MaintenanceInterval("Safety Devices", IntervalType.MONTHLY, 30, "Complete safety system certification", Priority.CRITICAL),
        
        MaintenanceInterval("Signaling Systems", IntervalType.DAILY, 1, "Signal clarity and response test", Priority.HIGH),
        MaintenanceInterval("Signaling Systems", IntervalType.WEEKLY, 7, "Communication system calibration", Priority.HIGH),
        MaintenanceInterval("Signaling Systems", IntervalType.QUARTERLY, 90, "Complete signaling system overhaul", Priority.HIGH),
        
        MaintenanceInterval("Lubrication", IntervalType.DAILY, 1, "Lubrication level check", Priority.NORMAL),
        MaintenanceInterval("Lubrication", IntervalType.WEEKLY, 7, "Lubrication system maintenance", Priority.HIGH),
        MaintenanceInterval("Lubrication", IntervalType.MONTHLY, 30, "Complete lubrication system service", Priority.HIGH),
        
        MaintenanceInterval("Illumination", IntervalType.WEEKLY, 7, "Light functionality check", Priority.NORMAL),
        MaintenanceInterval("Illumination", IntervalType.MONTHLY, 30, "Complete lighting system maintenance", Priority.NORMAL),
        
        MaintenanceInterval("Cooling Systems", IntervalType.DAILY, 1, "Cooling system operation check", Priority.HIGH),
        MaintenanceInterval("Cooling Systems", IntervalType.WEEKLY, 7, "Coolant level and filter check", Priority.HIGH),
        MaintenanceInterval("Cooling Systems", IntervalType.MONTHLY, 30, "Complete cooling system service", Priority.HIGH),
        
        MaintenanceInterval("Brake Systems", IntervalType.DAILY, 1, "Brake response and pressure test", Priority.CRITICAL),
        MaintenanceInterval("Brake Systems", IntervalType.WEEKLY, 7, "Brake pad and fluid inspection", Priority.CRITICAL),
        MaintenanceInterval("Brake Systems", IntervalType.MONTHLY, 30, "Complete brake system overhaul", Priority.CRITICAL)
    )
    
    suspend fun checkOverdueMaintenanceForAllComponents(): List<MaintenanceAlert> {
        return withContext(Dispatchers.IO) {
            val overdueAlerts = mutableListOf<MaintenanceAlert>()
            val currentTime = System.currentTimeMillis()
            
            try {
                for (schedule in maintenanceSchedules) {
                    val lastCompleted = getLastMaintenanceDate(schedule.component, schedule.intervalType)
                    val nextDue = lastCompleted + (schedule.intervalDays * 24 * 60 * 60 * 1000L)
                    
                    if (currentTime > nextDue) {
                        val daysOverdue = ((currentTime - nextDue) / (24 * 60 * 60 * 1000L)).toInt()
                        
                        val alert = MaintenanceAlert(
                            component = schedule.component,
                            intervalType = schedule.intervalType,
                            lastCompleted = lastCompleted,
                            nextDue = nextDue,
                            daysOverdue = daysOverdue,
                            priority = adjustPriorityForOverdue(schedule.priority, daysOverdue),
                            description = schedule.description,
                            assignedTo = getAssignedTechnician(schedule.component),
                            escalationLevel = calculateEscalationLevel(daysOverdue)
                        )
                        
                        overdueAlerts.add(alert)
                    }
                }
                
                // Sort by priority and days overdue
                overdueAlerts.sortedWith(compareByDescending<MaintenanceAlert> { it.priority.ordinal }
                    .thenByDescending { it.daysOverdue })
                    
            } catch (e: Exception) {
                Log.e(TAG, "Error checking overdue maintenance", e)
                emptyList()
            }
        }
    }
    
    suspend fun getUpcomingMaintenance(daysAhead: Int = 7): List<MaintenanceAlert> {
        return withContext(Dispatchers.IO) {
            val upcomingAlerts = mutableListOf<MaintenanceAlert>()
            val currentTime = System.currentTimeMillis()
            val futureTime = currentTime + (daysAhead * 24 * 60 * 60 * 1000L)
            
            try {
                for (schedule in maintenanceSchedules) {
                    val lastCompleted = getLastMaintenanceDate(schedule.component, schedule.intervalType)
                    val nextDue = lastCompleted + (schedule.intervalDays * 24 * 60 * 60 * 1000L)
                    
                    if (nextDue > currentTime && nextDue <= futureTime) {
                        val daysUntilDue = ((nextDue - currentTime) / (24 * 60 * 60 * 1000L)).toInt()
                        
                        val alert = MaintenanceAlert(
                            component = schedule.component,
                            intervalType = schedule.intervalType,
                            lastCompleted = lastCompleted,
                            nextDue = nextDue,
                            daysOverdue = -daysUntilDue, // Negative for upcoming
                            priority = schedule.priority,
                            description = schedule.description,
                            assignedTo = getAssignedTechnician(schedule.component)
                        )
                        
                        upcomingAlerts.add(alert)
                    }
                }
                
                upcomingAlerts.sortedBy { it.nextDue }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting upcoming maintenance", e)
                emptyList()
            }
        }
    }
    
    suspend fun recordMaintenanceCompletion(
        component: String,
        intervalType: IntervalType,
        completedBy: String,
        notes: String = ""
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val maintenanceRecord = mapOf(
                    "component" to component,
                    "intervalType" to intervalType.name,
                    "completedBy" to completedBy,
                    "completedDate" to System.currentTimeMillis(),
                    "notes" to notes,
                    "type" to "maintenance_completion",
                    "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                
                firestoreService.saveMaintenance(maintenanceRecord)
            } catch (e: Exception) {
                Log.e(TAG, "Error recording maintenance completion", e)
                false
            }
        }
    }
    
    suspend fun createMaintenanceAlert(alert: MaintenanceAlert): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val alertData = mapOf(
                    "component" to alert.component,
                    "intervalType" to alert.intervalType.name,
                    "priority" to alert.priority.name,
                    "daysOverdue" to alert.daysOverdue,
                    "description" to alert.description,
                    "assignedTo" to alert.assignedTo,
                    "escalationLevel" to alert.escalationLevel,
                    "createdDate" to System.currentTimeMillis(),
                    "type" to "maintenance_alert",
                    "status" to "active"
                )
                
                firestoreService.saveLogbookEntry(alertData)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating maintenance alert", e)
                false
            }
        }
    }
    
    private suspend fun getLastMaintenanceDate(component: String, intervalType: IntervalType): Long {
        // This would query Firestore for the last maintenance completion
        // For now, return a mock date (30 days ago for simulation)
        return System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
    }
    
    private fun adjustPriorityForOverdue(originalPriority: Priority, daysOverdue: Int): Priority {
        return when {
            daysOverdue > 7 -> Priority.CRITICAL
            daysOverdue > 3 -> Priority.HIGH
            daysOverdue > 1 -> Priority.NORMAL
            else -> originalPriority
        }
    }
    
    private fun calculateEscalationLevel(daysOverdue: Int): Int {
        return when {
            daysOverdue > 14 -> 3 // Engineer + Safety Officer
            daysOverdue > 7 -> 2  // Engineer
            daysOverdue > 3 -> 1  // Foreman
            else -> 0 // No escalation
        }
    }
    
    private fun getAssignedTechnician(component: String): String {
        return when (component) {
            "Winder Motor", "Cooling Systems" -> "Engineer"
            "Signaling Systems", "Illumination" -> "Electrician"
            "Ropes", "Safety Devices" -> "Rigger"
            "Brake Systems", "Clutches" -> "Fitter"
            "Control Levers" -> "Boilermaker"
            "Lubrication" -> "Artisan"
            else -> "Maintenance Team"
        }
    }
    
    fun getMaintenanceScheduleForComponent(component: String): List<MaintenanceInterval> {
        return maintenanceSchedules.filter { it.component == component }
    }
    
    fun getAllComponents(): List<String> {
        return maintenanceSchedules.map { it.component }.distinct()
    }
    
    suspend fun generateMaintenanceReport(dateRange: Pair<Long, Long>): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val overdueItems = checkOverdueMaintenanceForAllComponents()
                val upcomingItems = getUpcomingMaintenance(14)
                
                mapOf(
                    "reportDate" to System.currentTimeMillis(),
                    "dateRange" to mapOf(
                        "start" to dateRange.first,
                        "end" to dateRange.second
                    ),
                    "overdueMaintenanceCount" to overdueItems.size,
                    "upcomingMaintenanceCount" to upcomingItems.size,
                    "criticalAlertsCount" to overdueItems.count { it.priority == Priority.CRITICAL },
                    "highPriorityAlertsCount" to overdueItems.count { it.priority == Priority.HIGH },
                    "overdueItems" to overdueItems.map { alert ->
                        mapOf(
                            "component" to alert.component,
                            "intervalType" to alert.intervalType.name,
                            "daysOverdue" to alert.daysOverdue,
                            "priority" to alert.priority.name,
                            "assignedTo" to alert.assignedTo
                        )
                    },
                    "upcomingItems" to upcomingItems.map { alert ->
                        mapOf(
                            "component" to alert.component,
                            "intervalType" to alert.intervalType.name,
                            "dueIn" to abs(alert.daysOverdue),
                            "priority" to alert.priority.name,
                            "assignedTo" to alert.assignedTo
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error generating maintenance report", e)
                mapOf("error" to "Failed to generate maintenance report")
            }
        }
    }
}
