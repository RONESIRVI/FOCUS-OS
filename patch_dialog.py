import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

# Replace Dialog( ... ) { Box(...) } with just Box( ... zIndex = 100f ... )
old_dialog = """            Dialog(
                onDismissRequest = { /* Modal lock - require explicit button */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {"""

new_dialog = """            // We removed Dialog and use a direct Box overlay to prevent black screen rendering issues
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .padding(14.dp)
                    .androidx.compose.ui.zIndex.zIndex(100f)
                    .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                // Consume all touch events to block interaction with UI underneath
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {"""

content = content.replace(old_dialog, new_dialog)

# The end of the Dialog block needs one less closing brace
# But let's check how many braces we need to remove.
# We replaced Dialog { Box { with Box { (removed one block level).
