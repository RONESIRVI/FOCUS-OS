package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.FocusWarning
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel
import java.io.File

@Composable
fun CameraVerificationScreen(
    viewModel: FocusViewModel,
    isStart: Boolean,
    onVerificationComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var currentUriToSave by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri = currentUriToSave
        }
    }

    fun takePhoto() {
        val file = File(context.cacheDir, "focus_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        currentUriToSave = uri
        cameraLauncher.launch(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Text(
                text = if (isStart) "START PHOTO VERIFICATION" else "FINAL SELFIE CAMERA",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = FocusWarning
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isStart) "Take a photo of your desk or study setup to begin." else "Take a selfie to verify you completed the session.",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (photoUri != null) {
                // Show taken photo
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .border(2.dp, FocusPrimary, RoundedCornerShape(32.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Captured Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Retake button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { takePhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Retake", tint = Color.White)
                    }
                }
            } else {
                // Camera Placeholder
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(FocusSurface, RoundedCornerShape(32.dp))
                        .border(2.dp, FocusSurfaceVariant, RoundedCornerShape(32.dp))
                        .clickable { takePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = FocusPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("TAP TO CAPTURE", color = FocusPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    // We can just proceed, optionally save photo URI to ViewModel
                    onVerificationComplete() 
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusPrimary
                ),
                enabled = true
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VERIFY & PROCEED", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            if (isStart) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onCancel) {
                    Text("Cancel Session", color = FocusTextSecondary)
                }
            }
        }
    }
}
