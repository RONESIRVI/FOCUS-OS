import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

card_pattern = r'                        // Allowed Apps.*?                    Icon\(Icons\.Default\.ChevronRight, contentDescription = "Select Apps", tint = FocusTextSecondary\)\n                }\n            \}'

new_content = re.sub(card_pattern, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(new_content)
