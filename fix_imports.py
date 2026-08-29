import sys

def add_import(file_path):
    with open(file_path, "r") as f:
        content = f.read()
    
    if "import androidx.compose.ui.text.style.TextOverflow" not in content:
        # Find the last import
        import_idx = content.rfind("import ")
        if import_idx != -1:
            end_of_line = content.find("\n", import_idx)
            content = content[:end_of_line] + "\nimport androidx.compose.ui.text.style.TextOverflow" + content[end_of_line:]
            with open(file_path, "w") as f:
                f.write(content)
            print(f"Added import to {file_path}")

add_import("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt")
add_import("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt")
