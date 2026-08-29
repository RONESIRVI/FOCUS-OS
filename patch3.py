with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import androidx.compose.ui.zIndex.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass"""

if "import androidx.compose.ui.zIndex.zIndex" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", imports + "\nimport androidx.compose.ui.Modifier")

old_box = """.androidx.compose.ui.zIndex.zIndex(100f)
                    .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                // Consume pointer events
                            }
                        }
                    }"""

new_box = """.zIndex(100f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                // Consume pointer events
                            }
                        }
                    }"""

content = content.replace(old_box, new_box)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
print("Patch 3 applied")
