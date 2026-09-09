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
            
            // --- WATERMARK LOGIC ---
            inputStream = contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return null
            
            // Handle EXIF orientation (Camera photos might be rotated)
            var finalBitmap = originalBitmap
            try {
                contentResolver.openInputStream(sourceUri)?.use { exis ->
                    val exif = android.media.ExifInterface(exis)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    val matrix = android.graphics.Matrix()
                    when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    if (!matrix.isIdentity) {
                        finalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                        if (finalBitmap != originalBitmap) originalBitmap.recycle()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exif parsing error", e)
            }

            // Create mutable copy and draw watermark
            val watermarkedBitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            if (finalBitmap != watermarkedBitmap) finalBitmap.recycle()
            
            val canvas = android.graphics.Canvas(watermarkedBitmap)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (watermarkedBitmap.width / 25f).coerceAtLeast(30f)
                isAntiAlias = true
                setShadowLayer(8f, 2f, 2f, android.graphics.Color.BLACK)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            }
            
            val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            val text = "$dateStr • FocusOS Proof"
            val textWidth = paint.measureText(text)
            
            val padding = watermarkedBitmap.width / 30f
            val x = watermarkedBitmap.width - textWidth - padding
            val y = watermarkedBitmap.height - padding
            
            canvas.drawText(text, x, y, paint)
            // --- END WATERMARK LOGIC ---

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
                    watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    outputStream.flush()
                }
                
                watermarkedBitmap.recycle()

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
