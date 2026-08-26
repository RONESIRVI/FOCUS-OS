import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports_to_add = """import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
"""
content = content.replace("import com.example.ui.theme.*", imports_to_add + "import com.example.ui.theme.*")

# Add state vars and launcher
old_state = 'var userName by remember { mutableStateOf("Focus Student") }'
new_state = """var userName by remember { mutableStateOf("Focus Student") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val file = File(context.filesDir, "profile_photo.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    
                    val newUri = Uri.fromFile(file).toString()
                    sharedPrefs.edit().putString("PROFILE_PHOTO_URI", newUri).apply()
                    profilePhotoUri = newUri
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )"""
content = content.replace(old_state, new_state)

old_load = 'userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"'
new_load = 'userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"\n        profilePhotoUri = sharedPrefs.getString("PROFILE_PHOTO_URI", null)'
content = content.replace(old_load, new_load)

# Modify profile photo box to show Image and be clickable
old_photo_box = """                    Box(
                        modifier = Modifier
                            .size(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(FocusSurfaceVariant, CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                        }"""
new_photo_box = """                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .border(2.dp, FocusPrimary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(FocusSurfaceVariant, CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                            }
                        }"""
content = content.replace(old_photo_box, new_photo_box)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
