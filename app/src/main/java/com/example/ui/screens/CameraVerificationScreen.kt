package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.PhotoStorageHelper
import java.io.File

@Composable
fun CameraVerificationScreen(
    viewModel: FocusViewModel,
    isStart: Boolean,
    onVerificationComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    androidx.activity.compose.BackHandler {
        onCancel()
    }

    DisposableEffect(Unit) {
        com.example.util.FocusLockManager.setCameraVerificationActive(true)
        onDispose {
            com.example.util.FocusLockManager.setCameraVerificationActive(false)
        }
    }

    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCaptureUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var isSavingToGallery by remember { mutableStateOf(false) }
    
    val photoUri = photoUriString?.let { Uri.parse(it) }
    val tempCaptureUri = tempCaptureUriString?.let { Uri.parse(it) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = tempCaptureUri
        if (success && uri != null && PhotoStorageHelper.isPhotoFileValid(context, uri)) {
            isSavingToGallery = true
            // Save to device gallery in background thread
            val savedGalleryUri = PhotoStorageHelper.savePhotoToDeviceGallery(context, uri) ?: uri
            photoUriString = savedGalleryUri.toString()
            isSavingToGallery = false

            if (isStart) {
                viewModel.setStartPhotoUri(savedGalleryUri.toString())
            } else {
                viewModel.setEndSelfieUri(savedGalleryUri.toString())
            }
            Toast.makeText(context, "📸 Photo saved to Gallery (Pictures/FocusOS)", Toast.LENGTH_LONG).show()
        } else {
            // Photo capture was cancelled or failed
            if (photoUri == null) {
                Toast.makeText(context, "Camera capture cancelled. Photo is required to proceed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val capturePair = PhotoStorageHelper.createCaptureUri(context)
            if (capturePair != null) {
                tempCaptureUriString = capturePair.first.toString()
                cameraLauncher.launch(capturePair.first)
            } else {
                Toast.makeText(context, "Failed to initialize camera storage", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture photo proof", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val capturePair = PhotoStorageHelper.createCaptureUri(context)
            if (capturePair != null) {
                tempCaptureUriString = capturePair.first.toString()
                cameraLauncher.launch(capturePair.first)
            } else {
                Toast.makeText(context, "Unable to create storage for photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(FocusSurfaceVariant, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isStart) FocusPrimary else FocusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStart) "ANTI-CHEAT PHOTO VERIFICATION" else "SESSION COMPLETION VERIFICATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isStart) "CAPTURE STUDY DESK PHOTO" else "CAPTURE COMPLETION SELFIE",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isStart)
                        "Take a real photo of your study space or book with your phone camera. The photo is saved directly to your phone's Gallery (Pictures/FocusOS)."
                    else
                        "Take a selfie with your camera to confirm genuine session completion and save it to your phone Gallery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Viewfinder / Captured Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(FocusSurface)
                    .border(
                        width = 2.dp,
                        brush = if (photoUri != null)
                            Brush.linearGradient(listOf(FocusPrimary, FocusWarning))
                        else
                            Brush.linearGradient(listOf(FocusSurfaceVariant, FocusOutline)),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    // Display real captured photo
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Real Camera Photo Proof",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SAVED TO GALLERY ✓",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Retake button
                        IconButton(
                            onClick = { launchCamera() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White)
                        }
                    }
                } else {
                    // Placeholder when no photo taken yet
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { launchCamera() }
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(FocusPrimary.copy(alpha = 0.15f), CircleShape)
                                .border(2.dp, FocusPrimary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Open Camera",
                                tint = FocusPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "TAP TO OPEN PHONE CAMERA",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Opens real camera • Saves to Gallery",
                            style = MaterialTheme.typography.labelMedium,
                            color = FocusWarning
                        )
                    }
                }
            }

            // Bottom Actions & Verification Button (Strictly requires captured photo)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (photoUri == null) {
                    // Open Camera Primary Button
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("open_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN CAMERA & TAKE PHOTO",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                val hasCapturedPhoto = photoUri != null
                Button(
                    onClick = {
                        if (hasCapturedPhoto) {
                            if (isStart) {
                                viewModel.setStartPhotoUri(photoUri.toString())
                            } else {
                                viewModel.setEndSelfieUri(photoUri.toString())
                            }
                            onVerificationComplete()
                        } else {
                            Toast.makeText(context, "⚠️ Please take a photo with your camera first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("verify_photo_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasCapturedPhoto) FocusPrimary else FocusSurfaceVariant,
                        disabledContainerColor = FocusSurfaceVariant,
                        disabledContentColor = FocusTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = hasCapturedPhoto
                ) {
                    if (hasCapturedPhoto) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isStart) "VERIFY & START FOCUS SESSION" else "VERIFY & FINISH SESSION",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = FocusTextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📸 TAKE PHOTO TO UNLOCK",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FocusTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isStart) "Cancel & Return" else "Skip Selfie & Complete (Stats Saved)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusTextSecondary
                    )
                }
            }
        }
    }
}
