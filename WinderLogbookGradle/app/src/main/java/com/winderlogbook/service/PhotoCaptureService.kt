package com.winderlogbook.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoCaptureService(private val context: Context) {
    
    companion object {
        private const val TAG = "PhotoCaptureService"
        private const val IMAGE_QUALITY = 80
        private const val MAX_IMAGE_WIDTH = 1024
        private const val MAX_IMAGE_HEIGHT = 1024
    }
    
    interface PhotoCaptureCallback {
        fun onPhotoTaken(base64Image: String, filePath: String)
        fun onError(error: String)
    }
    
    fun hasCameraPermission(): Boolean {
        return androidx.core.content.PermissionChecker.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED
    }
    
    fun createImageFile(): File? {
        return try {
            // Create an image file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "WINDER_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            
            File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
        } catch (ex: IOException) {
            Log.e(TAG, "Error creating image file", ex)
            null
        }
    }
    
    fun createCameraIntent(): Intent? {
        if (!hasCameraPermission()) {
            Log.e(TAG, "Camera permission not granted")
            return null
        }
        
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            return takePictureIntent
        }
        
        Log.e(TAG, "No camera app available")
        return null
    }
    
    fun createFileProviderUri(file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file provider URI", e)
            null
        }
    }
    
    fun processImage(filePath: String, callback: PhotoCaptureCallback) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                callback.onError("Image file not found")
                return
            }
            
            // Load and compress the image
            val bitmap = loadAndResizeBitmap(filePath)
            if (bitmap == null) {
                callback.onError("Failed to load image")
                return
            }
            
            // Convert to base64
            val base64String = bitmapToBase64(bitmap)
            if (base64String.isEmpty()) {
                callback.onError("Failed to encode image")
                return
            }
            
            // Add metadata
            val imageWithMetadata = addImageMetadata(base64String, filePath)
            
            callback.onPhotoTaken(imageWithMetadata, filePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            callback.onError("Error processing image: ${e.message}")
        }
    }
    
    private fun loadAndResizeBitmap(filePath: String): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap", e)
            null
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bitmap to base64", e)
            ""
        }
    }
    
    private fun addImageMetadata(base64Image: String, filePath: String): String {
        val metadata = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "filePath" to filePath,
            "capturedBy" to "WinderLogbook",
            "imageData" to base64Image
        )
        
        return com.google.gson.Gson().toJson(metadata)
    }
    
    fun saveImageToFirestore(
        base64Image: String, 
        componentName: String, 
        description: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val imageData = mapOf(
                "image" to base64Image,
                "component" to componentName,
                "description" to description,
                "timestamp" to System.currentTimeMillis(),
                "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                "type" to "equipment_photo"
            )
            
            // Use FirestoreService to save the image
            val firestoreService = FirestoreService.getInstance()
            
            // Save asynchronously (this would need to be handled properly in production)
            CoroutineScope(Dispatchers.IO).launch {
                val success = firestoreService.saveLogbookEntry(imageData)
                withContext(Dispatchers.Main) {
                    if (success) {
                        callback(true, "Photo saved successfully")
                    } else {
                        callback(false, "Failed to save photo")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image to Firestore", e)
            callback(false, "Error saving photo: ${e.message}")
        }
    }
    
    fun cleanup() {
        // Clean up any temporary files if needed
        Log.d(TAG, "PhotoCaptureService cleanup")
    }
}
