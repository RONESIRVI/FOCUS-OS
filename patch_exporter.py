with open("app/src/main/java/com/example/util/ComposeViewExporter.kt", "r") as f:
    content = f.read()

# I want to change the container to a ScrollView so it gives infinite height to ComposeView
old_container = """        // Wrap in a FrameLayout that will allow it to be arbitrarily large
        val container = FrameLayout(context).apply {
            alpha = 0f
            addView(composeView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ))
        }"""
new_container = """        // Wrap in a ScrollView that will allow it to be arbitrarily large
        val container = android.widget.ScrollView(context).apply {
            alpha = 0f
            addView(composeView, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ))
        }"""
content = content.replace(old_container, new_container)

with open("app/src/main/java/com/example/util/ComposeViewExporter.kt", "w") as f:
    f.write(content)
