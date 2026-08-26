import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_imports = """import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
"""
if "Manifest" not in content:
    content = content.replace("import android.content.Intent", new_imports + "import android.content.Intent")

req_code = """        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
"""
if "POST_NOTIFICATIONS" not in content:
    content = content.replace("super.onCreate(savedInstanceState)", "super.onCreate(savedInstanceState)\n" + req_code)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
