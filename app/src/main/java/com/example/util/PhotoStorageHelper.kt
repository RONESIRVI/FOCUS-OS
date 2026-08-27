package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object PhotoStorageHelper {
    private const val TAG = "PhotoStorageHelper"

    /**
     * Creates a new temporary file in external pictures / internal files directory
     * and returns its FileProvider content URI for camera capture.
     */
    fun createCaptureUri(context: Context): Pair<Uri, File>? {
        return try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) 
                ?: File(context.filesDir, "Pictures").apply { mkdirs() }
            
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val filename = "FocusOS_Proof_${System.currentTimeMillis()}.jpg"
            val photoFile = File(storageDir, filename)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            Pair(uri, photoFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating capture URI", e)
            null
        }
    }

    /**
     * Saves the captured image file into the phone's public MediaStore (Pictures/FocusOS)
     * so it appears in the device's Gallery / Photos app.
     */
    fun savePhotoToDeviceGallery(context: Context, sourceUri: Uri): Uri? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val contentResolver = context.contentResolver
            inputStream = contentResolver.openInputStream(sourceUri) ?: return null

            val filename = "FocusOS_Proof_${System.currentTimeMillis()}.jpg"
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FocusOS")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val savedUri = contentResolver.insert(imageCollection, contentValues)
            if (savedUri != null) {
                outputStream = contentResolver.openOutputStream(savedUri)
                if (outputStream != null) {
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(savedUri, contentValues, null, null)
                }

                // Also trigger MediaScannerConnection for legacy / gallery refresh
                try {
                    val path = getFilePathFromUri(context, savedUri)
                    if (path != null) {
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(path),
                            arrayOf("image/jpeg"),
                            null
                        )
                    }
                } catch (e: Exception) {
                    // Ignore scanner error
                }

                Log.d(TAG, "Photo successfully saved to Gallery at: $savedUri")
                return savedUri
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save photo to MediaStore gallery", e)
        } finally {
            try { inputStream?.close() } catch (e: Exception) {}
            try { outputStream?.close() } catch (e: Exception) {}
        }
        return sourceUri // Fallback to source URI if MediaStore insert fails
    }

    /**
     * Validates that the URI points to an actual existing photo file with size > 0
     */
    fun isPhotoFileValid(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            val size = pfd?.statSize ?: 0L
            pfd?.close()
            if (size > 1024) return true

            // Fallback: check if we can decode bounds (in case statSize fails for FileProvider)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options)
            }
            options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }
}
