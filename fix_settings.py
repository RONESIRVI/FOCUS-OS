import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

profile_state = """    var showEditProfileDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Focus Student") }
    
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
    
    LaunchedEffect(Unit) {
        userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"
    }"""

if "showEditProfileDialog" not in content:
    content = content.replace("fun SettingsScreen(\n    viewModel: FocusViewModel,\n    onNavigateToAppSelector: (String) -> Unit\n) {", 
                              "fun SettingsScreen(\n    viewModel: FocusViewModel,\n    onNavigateToAppSelector: (String) -> Unit\n) {\n" + profile_state + "\n")
    content = content.replace("import androidx.compose.ui.platform.LocalContext", "")
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalContext\nimport android.content.Context")
    content = content.replace("Text(\"Focus Student\"", "Text(userName")
    content = content.replace("IconButton(onClick = { /* TODO: Edit Profile */ })", "IconButton(onClick = { showEditProfileDialog = true })")

    dialog_code = """
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    userName = tempName
                    sharedPrefs.edit().putString("USER_NAME", tempName).apply()
                    showEditProfileDialog = false
                }) {
                    Text("Save", color = FocusPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = FocusTextSecondary)
                }
            },
            containerColor = FocusSurface
        )
    }
}"""
    content = content.replace("        item { Spacer(modifier = Modifier.height(100.dp)) }\n    }\n}", "        item { Spacer(modifier = Modifier.height(100.dp)) }\n    }\n" + dialog_code)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
