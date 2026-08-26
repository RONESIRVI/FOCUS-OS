import re

with open("app/src/main/java/com/example/ui/screens/CameraVerificationScreen.kt", "r") as f:
    content = f.read()

header = """                Text(
                    text = if (isStart) "START VERIFICATION" else "END VERIFICATION",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isStart) "Take a live photo of your study material" else "Take a final selfie to prove completion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )"""

new_header = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOCK ACTIVE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = FocusWarning)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isStart) "START VERIFICATION" else "END VERIFICATION",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isStart) "Take a live photo of your book or study material." else "Take a final selfie to prove completion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📷 Gallery upload not allowed",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusTextSecondary
                )"""

content = content.replace(header, new_header)

if "import androidx.compose.material.icons.filled.Lock" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Check", "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Lock")

with open("app/src/main/java/com/example/ui/screens/CameraVerificationScreen.kt", "w") as f:
    f.write(content)
