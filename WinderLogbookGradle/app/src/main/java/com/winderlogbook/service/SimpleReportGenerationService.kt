package com.winderlogbook.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SimpleReportGenerationService(private val context: Context) {

    private val firestoreService = FirestoreService.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ReportGenService"
        private const val REPORTS_DIR = "WinnerLogbook_Reports"
    }

    data class ReportResult(val success: Boolean, val fileName: String?, val filePath: String?, val error: String?)

    suspend fun generateDailyShiftReport(): ReportResult {
        val fileName = "DailyShiftReport_${System.currentTimeMillis()}.pdf"
        val filePath = getReportFile(fileName).absolutePath
        val file = File(filePath)

        return try {
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // Title
            document.add(Paragraph("Daily Shift Report").apply {
                alignment = Element.ALIGN_CENTER
                font = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            })
            document.add(Paragraph("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}").apply {
                alignment = Element.ALIGN_CENTER
                font = Font(Font.FontFamily.HELVETICA, 10f)
            })
            document.add(Chunk("\n"))

            // Fetch data
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val tripCounters = getTripCountersForDate(today)
            val componentStatuses = getComponentStatusesForDate(today)
            val logbookEntries = getLogbookEntriesForDate(today)

            // Summary Table
            document.add(Paragraph("Summary").apply { font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD) })
            val summaryTable = PdfPTable(2).apply { widthPercentage = 100f }
            
            try {
                val totalPersons = tripCounters.sumOf { (it["persons"] as? Number)?.toLong() ?: 0 }
                val totalMaterial = tripCounters.sumOf { (it["material"] as? Number)?.toLong() ?: 0 }
                val totalMineral = tripCounters.sumOf { (it["mineral"] as? Number)?.toLong() ?: 0 }
                val totalExplosives = tripCounters.sumOf { (it["explosives"] as? Number)?.toLong() ?: 0 }
                val totalOperations = totalPersons + totalMaterial + totalMineral + totalExplosives
                
                summaryTable.addCell(PdfPCell(Phrase("Total Operations Today:")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("$totalOperations")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("Personnel Transported:")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("$totalPersons")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("Material Trips:")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("$totalMaterial")).apply { border = Rectangle.NO_BORDER })
            } catch (e: Exception) {
                Log.w(TAG, "Error processing trip counter data: ${e.message}")
                summaryTable.addCell(PdfPCell(Phrase("Data processing error")).apply { border = Rectangle.NO_BORDER })
                summaryTable.addCell(PdfPCell(Phrase("Unable to calculate")).apply { border = Rectangle.NO_BORDER })
            }
            
            document.add(summaryTable)
            document.add(Chunk("\n"))

            // Shift-by-Shift Breakdown
            document.add(Paragraph("Shift Breakdown").apply { font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD) })
            val shiftTable = PdfPTable(arrayOf(2f, 1f, 1f, 1f, 1f, 1f).toFloatArray()).apply { widthPercentage = 100f }
            shiftTable.addCell("Shift")
            shiftTable.addCell("Driver")
            shiftTable.addCell("Persons")
            shiftTable.addCell("Material")
            shiftTable.addCell("Mineral")
            shiftTable.addCell("Explosives")

            val shifts = listOf("Morning", "Afternoon", "Night")
            shifts.forEach { shift ->
                try {
                    val entries = tripCounters.filter { (it["shift"] as? String) == shift }
                    shiftTable.addCell(shift)
                    shiftTable.addCell(entries.firstOrNull()?.get("user") as? String ?: "N/A")
                    shiftTable.addCell("${entries.sumOf { (it["persons"] as? Number)?.toLong() ?: 0 }}")
                    shiftTable.addCell("${entries.sumOf { (it["material"] as? Number)?.toLong() ?: 0 }}")
                    shiftTable.addCell("${entries.sumOf { (it["mineral"] as? Number)?.toLong() ?: 0 }}")
                    shiftTable.addCell("${entries.sumOf { (it["explosives"] as? Number)?.toLong() ?: 0 }}")
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing shift data for $shift: ${e.message}")
                    shiftTable.addCell(shift)
                    shiftTable.addCell("Error")
                    shiftTable.addCell("0")
                    shiftTable.addCell("0")
                    shiftTable.addCell("0")
                    shiftTable.addCell("0")
                }
            }
            document.add(shiftTable)
            document.add(Chunk("\n"))

            // Equipment Status Summary
            document.add(Paragraph("Equipment Status Summary").apply { font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD) })
            try {
                val statusCounts = componentStatuses.groupBy { it["status"] as? String ?: "Unknown" }
                val statusTable = PdfPTable(2).apply { widthPercentage = 100f }
                
                if (statusCounts.isNotEmpty()) {
                    statusCounts.forEach { (status, list) ->
                        val displayStatus = status.replaceFirstChar { 
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                        }
                        statusTable.addCell(displayStatus)
                        statusTable.addCell("${list.size}")
                    }
                } else {
                    statusTable.addCell("No Status Data")
                    statusTable.addCell("0")
                }
                
                document.add(statusTable)
            } catch (e: Exception) {
                Log.w(TAG, "Error processing component status data: ${e.message}")
                val errorTable = PdfPTable(2).apply { widthPercentage = 100f }
                errorTable.addCell("Status Processing Error")
                errorTable.addCell("Unable to load")
                document.add(errorTable)
            }
            document.add(Chunk("\n"))

            // Logbook Entries
            document.add(Paragraph("Logbook Entries").apply { font = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD) })
            if (logbookEntries.isNotEmpty()) {
                logbookEntries.forEach { entry ->
                    try {
                        val entryType = entry["entryType"] as? String ?: "Unknown"
                        val details = entry["details"] as? String ?: entry["action"] as? String ?: "No details"
                        val user = entry["user"] as? String ?: "Unknown User"
                        val timestamp = entry["timestamp"] as? Long ?: System.currentTimeMillis()
                        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                        
                        document.add(Paragraph("• $entryType: $details by $user at $timeString"))
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing logbook entry: ${e.message}")
                        document.add(Paragraph("• Entry processing error: ${e.message}"))
                    }
                }
            } else {
                document.add(Paragraph("No logbook entries recorded for this date."))
            }

            document.close()

            // Try to upload to Firebase Storage
            var downloadUrl: String? = null
            try {
                downloadUrl = uploadReportToFirebaseStorage(file, "daily_shift_reports").await().toString()
                Log.d(TAG, "Successfully uploaded to Firebase Storage: $downloadUrl")
                
                // Save metadata to Firestore only if upload succeeded
                saveReportMetadataToFirestore(fileName, "daily_shift_report", today, downloadUrl)
                Log.d(TAG, "Report metadata saved to Firestore")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload to Firebase Storage: ${e.message}")
                
                // Save metadata to Firestore even if upload failed, with local path
                try {
                    saveReportMetadataToFirestore(fileName, "daily_shift_report", today, "local://${file.absolutePath}")
                    Log.d(TAG, "Report metadata saved with local path")
                } catch (firestoreError: Exception) {
                    Log.e(TAG, "Also failed to save metadata to Firestore: ${firestoreError.message}")
                }
            }

            Log.d(TAG, "Daily Shift Report generated successfully: $fileName")
            Log.d(TAG, "Local file path: $filePath")
            if (downloadUrl != null) {
                Log.d(TAG, "Download URL: $downloadUrl")
            }
            
            ReportResult(true, fileName, filePath, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating daily shift report", e)
            ReportResult(false, null, null, e.message)
        }
    }

    suspend fun generateTextReport(type: String): ReportResult {
        val fileName = "${type}Report_${System.currentTimeMillis()}.txt"
        val filePath = getReportFile(fileName).absolutePath
        val file = File(filePath)

        return try {
            val content = StringBuilder()
            content.append("$type Report\n")
            content.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            try {
                when (type) {
                    "weekly" -> {
                        content.append("--- Weekly Summary ---\n")
                        val tripCounters = getTripCountersForDate(today)
                        val totalPersons = tripCounters.sumOf { (it["persons"] as? Number)?.toLong() ?: 0 }
                        val totalMaterial = tripCounters.sumOf { (it["material"] as? Number)?.toLong() ?: 0 }
                        content.append("Total Persons: $totalPersons\n")
                        content.append("Total Material: $totalMaterial\n")
                        content.append("Total Entries: ${tripCounters.size}\n")
                    }
                    "maintenance" -> {
                        content.append("--- Maintenance Report ---\n")
                        val maintenanceTasks = getMaintenanceData()
                        if (maintenanceTasks.isNotEmpty()) {
                            maintenanceTasks.forEach { task ->
                                val componentName = task["componentName"] as? String ?: "Unknown Component"
                                val status = task["status"] as? String ?: "Unknown Status"
                                val date = task["date"] as? String ?: "No Date"
                                content.append("• Component: $componentName, Status: $status, Date: $date\n")
                            }
                        } else {
                            content.append("No maintenance tasks found.\n")
                        }
                    }
                    else -> {
                        content.append("--- General Report ---\n")
                        content.append("Report type: $type\n")
                        content.append("No specific data processing for this report type.\n")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing report data for type $type", e)
                content.append("--- Error Processing Report ---\n")
                content.append("An error occurred while processing the report data.\n")
                content.append("Error: ${e.message}\n")
            }

            file.writeText(content.toString())

            // Try to upload to Firebase Storage
            var downloadUrl: String? = null
            try {
                downloadUrl = uploadReportToFirebaseStorage(file, "${type}_reports").await().toString()
                Log.d(TAG, "Successfully uploaded $type report to Firebase Storage: $downloadUrl")
                
                // Save metadata to Firestore only if upload succeeded
                saveReportMetadataToFirestore(fileName, type, today, downloadUrl)
                Log.d(TAG, "$type report metadata saved to Firestore")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload $type report to Firebase Storage: ${e.message}")
                
                // Save metadata to Firestore even if upload failed, with local path
                try {
                    saveReportMetadataToFirestore(fileName, type, today, "local://${file.absolutePath}")
                    Log.d(TAG, "$type report metadata saved with local path")
                } catch (firestoreError: Exception) {
                    Log.e(TAG, "Also failed to save $type report metadata to Firestore: ${firestoreError.message}")
                }
            }

            Log.d(TAG, "$type Report generated successfully: $fileName")
            ReportResult(true, fileName, filePath, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating $type report", e)
            ReportResult(false, null, null, e.message)
        }
    }

    private fun getReportFile(fileName: String): File {
        val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), REPORTS_DIR)
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return File(reportsDir, fileName)
    }

    private fun uploadReportToFirebaseStorage(file: File, folder: String) = CoroutineScope(Dispatchers.IO).async {
        try {
            Log.d(TAG, "Starting upload for file: ${file.name} to folder: $folder")
            Log.d(TAG, "File exists: ${file.exists()}, File size: ${file.length()} bytes")
            
            if (!file.exists()) {
                throw Exception("File does not exist at path: ${file.absolutePath}")
            }
            
            val storageRef = storage.reference.child("reports/$folder/${file.name}")
            Log.d(TAG, "Storage reference path: reports/$folder/${file.name}")
            
            val uploadTask = storageRef.putFile(Uri.fromFile(file))
            Log.d(TAG, "Starting upload task...")
            
            uploadTask.await()
            Log.d(TAG, "Upload completed, getting download URL...")
            
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d(TAG, "Download URL obtained: $downloadUrl")
            
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage upload failed", e)
            throw e
        }
    }

    private suspend fun saveReportMetadataToFirestore(fileName: String, reportType: String, reportDate: String, downloadUrl: String) {
        try {
            val reportMetadata = mapOf(
                "fileName" to fileName,
                "reportType" to reportType,
                "reportDate" to reportDate,
                "generatedAt" to Date(),
                "downloadUrl" to downloadUrl,
                "generatedBy" to "Mobile App",
                "timestamp" to System.currentTimeMillis(),
                "status" to if (downloadUrl.startsWith("local://")) "local_only" else "uploaded"
            )
            
            Log.d(TAG, "Saving report metadata to Firestore: $reportMetadata")
            val docRef = db.collection("generated_reports").add(reportMetadata).await()
            Log.d(TAG, "Report metadata saved with document ID: ${docRef.id}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save report metadata to Firestore: ${e.message}", e)
            throw e
        }
    }

    // Data fetching methods
    private suspend fun getTripCountersForDate(date: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("trip_counters")
                .whereEqualTo("date", date)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip counters", e)
            emptyList()
        }
    }

    private suspend fun getComponentStatusesForDate(date: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("component_status")
                .whereEqualTo("date", date)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching component statuses", e)
            emptyList()
        }
    }

    private suspend fun getLogbookEntriesForDate(date: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("logbook_entries")
                .whereEqualTo("date", date)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching logbook entries", e)
            emptyList()
        }
    }

    private suspend fun getMaintenanceData(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("maintenance_schedules")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching maintenance data", e)
            emptyList()
        }
    }
    
    suspend fun generateWeeklyElectricalReport(reportData: com.google.gson.JsonObject): Boolean {
        return try {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val fileName = "WeeklyElectricalReport_$today.txt"
            val filePath = "${context.getExternalFilesDir(null)}/reports/$fileName"
            val file = java.io.File(filePath)
            
            // Ensure directory exists
            file.parentFile?.mkdirs()
            
            // Generate report content
            val content = generateWeeklyReportContent(reportData)
            
            // Write to file
            file.writeText(content)
            Log.d(TAG, "Weekly electrical report written to: $filePath")
            
            // Upload to Firebase Storage
            var downloadUrl: String? = null
            try {
                val uploadResult = uploadReportToFirebaseStorage(file, fileName)
                downloadUrl = uploadResult.await().toString()
                Log.d(TAG, "Weekly report uploaded to Firebase Storage: $downloadUrl")
                
                // Save metadata to Firestore
                saveReportMetadataToFirestore(fileName, "weekly_electrical_report", today, downloadUrl)
                Log.d(TAG, "Weekly report metadata saved to Firestore")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload weekly report to Firebase Storage: ${e.message}")
                
                // Save metadata to Firestore even if upload failed, with local path
                try {
                    saveReportMetadataToFirestore(fileName, "weekly_electrical_report", today, "local://${file.absolutePath}")
                    Log.d(TAG, "Weekly report metadata saved with local path")
                } catch (firestoreError: Exception) {
                    Log.e(TAG, "Also failed to save weekly report metadata to Firestore: ${firestoreError.message}")
                }
            }
            
            Log.d(TAG, "Weekly Electrical Report generated successfully: $fileName")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating weekly electrical report", e)
            false
        }
    }
    
    private fun generateWeeklyReportContent(reportData: com.google.gson.JsonObject): String {
        val sb = StringBuilder()
        
        sb.append("=".repeat(60)).append("\n")
        sb.append("           WEEKLY ELECTRICAL INSPECTION REPORT\n")
        sb.append("=".repeat(60)).append("\n\n")
        
        // Basic info
        sb.append("Report Type: Weekly Electrical Checklist\n")
        sb.append("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        
        val completedBy = reportData.get("completedBy")?.asString ?: "Unknown"
        val completedAt = reportData.get("completedAt")?.asString ?: "Unknown"
        sb.append("Completed By: $completedBy\n")
        sb.append("Completed At: $completedAt\n\n")
        
        // Risk Assessment Section
        sb.append("-".repeat(40)).append("\n")
        sb.append("RISK ASSESSMENT\n")
        sb.append("-".repeat(40)).append("\n")
        
        val riskAssessment = reportData.getAsJsonObject("riskAssessment")
        if (riskAssessment != null) {
            sb.append("Hazard Identification: ${riskAssessment.get("hazardIdentification")?.asString ?: "N/A"}\n")
            sb.append("Risk Level: ${riskAssessment.get("riskLevel")?.asString ?: "N/A"}\n")
            sb.append("Safety Measures: ${riskAssessment.get("safetyMeasures")?.asString ?: "N/A"}\n")
            
            val ppeRequired = riskAssessment.getAsJsonArray("ppeRequired")
            if (ppeRequired != null && ppeRequired.size() > 0) {
                sb.append("PPE Required: ")
                ppeRequired.forEachIndexed { index, element ->
                    if (index > 0) sb.append(", ")
                    sb.append(element.asString)
                }
                sb.append("\n")
            }
        }
        sb.append("\n")
        
        // Summary Section
        sb.append("-".repeat(40)).append("\n")
        sb.append("SUMMARY\n")
        sb.append("-".repeat(40)).append("\n")
        
        val summary = reportData.getAsJsonObject("summary")
        if (summary != null) {
            val totalItems = summary.get("totalItems")?.asInt ?: 0
            val greenCount = summary.get("greenCount")?.asInt ?: 0
            val yellowCount = summary.get("yellowCount")?.asInt ?: 0
            val redCount = summary.get("redCount")?.asInt ?: 0
            val overallStatus = summary.get("overallStatus")?.asString ?: "unknown"
            
            sb.append("Total Items Inspected: $totalItems\n")
            sb.append("🟢 Green (All in order): $greenCount\n")
            sb.append("🟡 Yellow (Needs attention): $yellowCount\n")
            sb.append("🔴 Red (Urgent issue): $redCount\n")
            sb.append("Overall Status: ${overallStatus.uppercase()}\n\n")
            
            if (yellowCount > 0 || redCount > 0) {
                sb.append("⚠️  ATTENTION REQUIRED: $yellowCount items need attention, $redCount items are urgent\n")
                sb.append("📧 Notifications have been sent to relevant personnel\n\n")
            }
        }
        
        sb.append("=".repeat(60)).append("\n")
        sb.append("End of Weekly Electrical Inspection Report\n")
        sb.append("=".repeat(60)).append("\n")
        
        return sb.toString()
    }
}
