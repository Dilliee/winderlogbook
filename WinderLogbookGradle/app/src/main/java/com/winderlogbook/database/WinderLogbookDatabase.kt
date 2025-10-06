package com.winderlogbook.database

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Simplified database interface using SharedPreferences for basic functionality
// Room database has been removed to simplify the build process
class WinderLogbookDatabase private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("winder_logbook_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: WinderLogbookDatabase? = null
        
        fun getDatabase(context: Context): WinderLogbookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = WinderLogbookDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    fun saveLogbookEntry(entry: Map<String, Any>) {
        val entries = getLogbookEntries().toMutableList()
        val entryWithId = entry.toMutableMap()
        entryWithId["id"] = System.currentTimeMillis().toString()
        entryWithId["timestamp"] = System.currentTimeMillis()
        entries.add(entryWithId)
        
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString("logbook_entries", json).apply()
    }
    
    fun getLogbookEntries(): List<Map<String, Any>> {
        val json = sharedPreferences.getString("logbook_entries", "[]")
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun saveInspection(inspection: Map<String, Any>) {
        val inspections = getInspections().toMutableList()
        val inspectionWithId = inspection.toMutableMap()
        inspectionWithId["id"] = System.currentTimeMillis().toString()
        inspectionWithId["timestamp"] = System.currentTimeMillis()
        inspections.add(inspectionWithId)
        
        val json = gson.toJson(inspections)
        sharedPreferences.edit().putString("inspections", json).apply()
    }
    
    fun getInspections(): List<Map<String, Any>> {
        val json = sharedPreferences.getString("inspections", "[]")
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun saveMaintenance(maintenance: Map<String, Any>) {
        val maintenanceRecords = getMaintenanceRecords().toMutableList()
        val maintenanceWithId = maintenance.toMutableMap()
        maintenanceWithId["id"] = System.currentTimeMillis().toString()
        maintenanceWithId["timestamp"] = System.currentTimeMillis()
        maintenanceRecords.add(maintenanceWithId)
        
        val json = gson.toJson(maintenanceRecords)
        sharedPreferences.edit().putString("maintenance_records", json).apply()
    }
    
    fun getMaintenanceRecords(): List<Map<String, Any>> {
        val json = sharedPreferences.getString("maintenance_records", "[]")
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}