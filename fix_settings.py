with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    lines = f.readlines()

context_idx = -1
launcher_start_idx = -1
launcher_end_idx = -1

for i, line in enumerate(lines):
    if "val context = LocalContext.current" in line:
        context_idx = i
    if "val photoPickerLauncher = rememberLauncherForActivityResult(" in line:
        launcher_start_idx = i
    if launcher_start_idx != -1 and "    )" in line and i > launcher_start_idx and launcher_end_idx == -1:
        launcher_end_idx = i

# Move context and sharedPrefs up above launcher
if context_idx > launcher_end_idx:
    # extract context and sharedPrefs lines
    context_line = lines[context_idx]
    shared_prefs_line = lines[context_idx + 1]
    
    # remove them from original position
    lines.pop(context_idx + 1)
    lines.pop(context_idx)
    
    # insert before launcher
    lines.insert(launcher_start_idx, shared_prefs_line)
    lines.insert(launcher_start_idx, context_line)
    
with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.writelines(lines)
