with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

# Remove the bad imports
content = content.replace("import androidx.compose.ui.zIndex.zIndex\n", "")
content = content.replace("import androidx.compose.ui.input.pointer.pointerInput\n", "")
content = content.replace("import androidx.compose.ui.input.pointer.PointerEventPass\n", "")

old_box = """.zIndex(100f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                // Consume pointer events
                            }
                        }
                    }"""

# A simpler way to block touches that doesn't need complex imports
new_box = """.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )"""

content = content.replace(old_box, new_box)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
print("Patch 4 applied")
