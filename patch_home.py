import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports_to_add = """import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
"""
content = content.replace("import com.example.ui.theme.*", imports_to_add + "import com.example.ui.theme.*")

# Add state var
old_state = 'var userName by remember { mutableStateOf("Focus Student") }'
new_state = 'var userName by remember { mutableStateOf("Focus Student") }\n    var profilePhotoUri by remember { mutableStateOf<String?>(null) }'
content = content.replace(old_state, new_state)

old_load = 'userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"'
new_load = 'userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"\n        profilePhotoUri = sharedPrefs.getString("PROFILE_PHOTO_URI", null)'
content = content.replace(old_load, new_load)

# Modify Greeting section
old_greeting = """        // Greeting & Motivation
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = FocusTextPrimary
                )
                Text(
                    text = "100% Distraction-free Study Environment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
            }
        }"""
new_greeting = """        // Greeting & Motivation
        item {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, FocusPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(
                        text = "Hi, $userName 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusTextPrimary
                    )
                    Text(
                        text = "100% Distraction-free Study Environment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusTextSecondary
                    )
                }
            }
        }"""
content = content.replace(old_greeting, new_greeting)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
