import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

old_call = "com.example.util.ComposeViewExporter.captureAndSaveComposeView(context = context, width = view.width) { StatisticsScreen(viewModel, onBack = {}, isExporting = true) }"
new_call = "com.example.util.ComposeViewExporter.captureAndSaveComposeView(context = context, width = view.width) { androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.wrapContentHeight()) { StatisticsScreen(viewModel, onBack = {}, isExporting = true) } }"

content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
