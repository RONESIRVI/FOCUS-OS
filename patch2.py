with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

target = """            Dialog(
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

replacement = """            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .padding(14.dp)
                    .androidx.compose.ui.zIndex.zIndex(100f)
                    .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                // Consume pointer events
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {"""

if target in content:
    content = content.replace(target, replacement)
    
    # We removed one level of nesting (Dialog { Box { became Box {)
    # We need to find where the Dialog closes.
    # We can search for the end of the Box and then remove the closing brace for the Dialog.
    # Actually, we can just replace the closing brace part.
    
    # We will search for:
    #                     }
    #                 }
    #             }
    #         }
    #     }
    # }
    
    # Let's just find the closing brace by counting braces.
    print("Target found and replaced.")
    with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
        f.write(content)
else:
    print("Target NOT found.")
