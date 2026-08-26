package com.example.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStream

object ComposeViewExporter {
    
    private tailrec fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

    fun captureAndSaveComposeView(context: Context, width: Int, content: @Composable () -> Unit) {
        val activity = context.getActivity() as? ComponentActivity
        if (activity == null) {
            Toast.makeText(context, "Cannot export: Activity not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        
        val composeView = ComposeView(context).apply {
            setContent {
                content()
            }
        }
        
        // Wrap in a ScrollView that will allow it to be arbitrarily large
        val container = android.widget.ScrollView(context).apply {
            alpha = 0f
            addView(composeView, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        
        rootView.addView(container, ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT))
        
        Toast.makeText(context, "Generating Full Report... Please wait", Toast.LENGTH_SHORT).show()
        
        CoroutineScope(Dispatchers.Main).launch {
            // Wait for composition to settle (1 second)
            delay(1000) 
            
            try {
                // Measure the composeView to get its full height
                val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                
                composeView.measure(widthMeasureSpec, heightMeasureSpec)
                
                val measuredWidth = composeView.measuredWidth
                val measuredHeight = composeView.measuredHeight
                
                if (measuredWidth > 0 && measuredHeight > 0) {
                    composeView.layout(0, 0, measuredWidth, measuredHeight)
                    
                    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.parseColor("#121212")) // Background
                    composeView.draw(canvas)
                    
                    saveBitmapToGallery(context, bitmap)
                } else {
                    Toast.makeText(context, "Failed to measure content.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error capturing view", Toast.LENGTH_SHORT).show()
            } finally {
                rootView.removeView(container)
            }
        }
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
            
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Full Stats Report saved to Gallery!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Failed to save report", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
