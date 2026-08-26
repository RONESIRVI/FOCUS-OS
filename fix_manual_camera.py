import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

content = content.replace("onStartSession = { navController.navigate(FocusRoutes.CAMERA_START) }", "onStartSession = { navController.navigate(FocusRoutes.TIMER) }")

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
