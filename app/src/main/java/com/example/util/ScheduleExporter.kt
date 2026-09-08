package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.FocusSession
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScheduleExporter {

    fun exportScheduleAsImage(
        context: Context,
        sessions: List<FocusSession>
    ) {
        val width = 1080
        val headerHeight = 250
        val cardHeight = 220
        val cardSpacing = 40
        val padding = 60
        
        val totalSessions = sessions.size
        val height = headerHeight + (totalSessions * (cardHeight + cardSpacing)) + padding
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#121212") } // FocusBackground
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Text Paints
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 75f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val datePaint = Paint().apply {
            color = Color.parseColor("#A0A0A0")
            textSize = 35f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Card Paint
        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#1E2633")
            isAntiAlias = true
        }
        
        val cardTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 45f
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            isFakeBoldText = true
        }
        
        val cardTextPaint = Paint().apply {
            color = Color.parseColor("#B0B0B0")
            textSize = 35f
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
        
        val badgePaint = Paint().apply {
            color = Color.parseColor("#4CAF50") // Green for upcoming
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        
        val badgeTextPaint = Paint().apply {
            color = Color.parseColor("#4CAF50")
            textSize = 30f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // Draw Header
        canvas.drawText("My Schedule", width / 2f, 120f, titlePaint)
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText(dateString, width / 2f, 180f, datePaint)

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Draw Cards
        for ((index, session) in sessions.withIndex()) {
            val top = headerHeight + (index * (cardHeight + cardSpacing)).toFloat()
            val rect = RectF(padding.toFloat(), top, (width - padding).toFloat(), top + cardHeight)
            canvas.drawRoundRect(rect, 30f, 30f, cardBgPaint)
            
            // Session Name
            val sessionName = if (session.sessionName.isNotBlank()) session.sessionName else "Study Session"
            canvas.drawText(sessionName, padding + 40f, top + 70f, cardTitlePaint)
            
            // Details
            val timeStr = session.scheduledStartTime?.let { timeFormatter.format(Date(it)) } ?: "N/A"
            val durationStr = "${session.targetDurationMinutes} min"
            canvas.drawText("Subject: ${session.subjectName}", padding + 40f, top + 130f, cardTextPaint)
            canvas.drawText("$timeStr • $durationStr", padding + 40f, top + 180f, cardTextPaint)
            
            // Badge
            val badgeRect = RectF(width - padding - 180f, top + 40f, width - padding - 40f, top + 90f)
            canvas.drawRoundRect(badgeRect, 15f, 15f, badgePaint)
            canvas.drawText("UPCOMING", badgeRect.centerX(), badgeRect.centerY() + 10f, badgeTextPaint)
        }

        saveBitmapToGallery(context, bitmap)
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "Schedule_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: android.net.Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FocusApp")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Schedule saved to Gallery!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Failed to save schedule image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
