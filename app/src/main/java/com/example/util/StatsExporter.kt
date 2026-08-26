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

object StatsExporter {
    fun exportStatsAsImage(
        context: Context,
        periodName: String,
        totalSeconds: Int,
        dailyAvgSeconds: Int,
        sessions: List<FocusSession>
    ) {
        val totalSessions = sessions.size
        
        // Group by subject and sort
        val subjectMap = sessions.groupBy { it.subjectName }
            .mapValues { (_, list) -> list.sumOf { it.completedDurationSeconds } }
            .toList()
            .sortedByDescending { it.second }
            
        val numSubjects = minOf(5, subjectMap.size)
        
        val width = 1080
        val height = 1100 + (numSubjects * 120) // dynamic height based on content
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#121212") } // FocusBackground
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Text Paints
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val labelPaint = Paint().apply {
            color = Color.parseColor("#A0A0A0")
            textSize = 40f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val valuePaint = Paint().apply {
            color = Color.parseColor("#4CAF50") // Primary Green / FocusPrimary
            textSize = 90f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val datePaint = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 35f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Card Paint
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E1E1E")
            isAntiAlias = true
        }

        // Draw Title
        canvas.drawText("My Focus Report", width / 2f, 150f, titlePaint)
        
        // Draw Date/Period
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText("Period: $periodName • Generated on $dateString", width / 2f, 220f, datePaint)

        // Draw Total Time Card
        canvas.drawRoundRect(RectF(100f, 300f, width - 100f, 500f), 40f, 40f, cardPaint)
        canvas.drawText("Total Focus Time", width / 2f, 380f, labelPaint)
        canvas.drawText(formatTime(totalSeconds), width / 2f, 470f, valuePaint)

        // Draw Daily Avg Card
        canvas.drawRoundRect(RectF(100f, 530f, 500f, 780f), 40f, 40f, cardPaint)
        canvas.drawText("Daily Average", 300f, 620f, labelPaint)
        canvas.drawText(formatTime(dailyAvgSeconds), 300f, 720f, valuePaint.apply { textSize = 75f })

        // Draw Sessions Card
        canvas.drawRoundRect(RectF(580f, 530f, width - 100f, 780f), 40f, 40f, cardPaint)
        canvas.drawText("Total Sessions", 830f, 620f, labelPaint)
        canvas.drawText("$totalSessions", 830f, 720f, valuePaint.apply { textSize = 75f })

        // Subject Breakdown
        if (subjectMap.isNotEmpty()) {
            canvas.drawRoundRect(RectF(100f, 810f, width - 100f, 810f + 160f + (numSubjects * 100f)), 40f, 40f, cardPaint)
            
            val subjectTitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 50f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }
            canvas.drawText("Subject Breakdown", 150f, 890f, subjectTitlePaint)
            
            val subjectNamePaint = Paint().apply {
                color = Color.WHITE
                textSize = 45f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }
            val subjectTimePaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 45f
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
                isFakeBoldText = true
            }
            
            val colors = listOf("#38BDF8", "#F59E0B", "#EC4899", "#10B981", "#8B5CF6")
            
            for (i in 0 until numSubjects) {
                val yPos = 990f + (i * 100f)
                val dotPaint = Paint().apply {
                    color = Color.parseColor(colors[i % colors.size])
                    isAntiAlias = true
                }
                canvas.drawCircle(170f, yPos - 15f, 20f, dotPaint)
                canvas.drawText(subjectMap[i].first, 220f, yPos, subjectNamePaint)
                val percent = if (totalSeconds > 0) ((subjectMap[i].second.toFloat() / totalSeconds) * 100).toInt() else 0
                canvas.drawText("${formatTime(subjectMap[i].second)} ($percent%)", width - 150f, yPos, subjectTimePaint)
            }
        }

        // Save Bitmap
        saveBitmapToGallery(context, bitmap)
    }

    private fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "FocusReport_${System.currentTimeMillis()}.jpg"
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
            // Show toast on the main thread
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Stats fully exported to Gallery!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun exportViewToImage(context: Context, view: android.view.View) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.parseColor("#121212")) // Default background
        }
        view.draw(canvas)
        saveBitmapToGallery(context, bitmap)
    }
}
