with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

imports_to_add = "import androidx.compose.foundation.layout.imePadding\nimport androidx.compose.foundation.layout.consumeWindowInsets\n"
content = content.replace("import androidx.compose.foundation.layout.padding", imports_to_add + "import androidx.compose.foundation.layout.padding")

old_box = "Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(FocusBackground)) {"
new_box = "Box(modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).imePadding().background(FocusBackground)) {"
content = content.replace(old_box, new_box)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
