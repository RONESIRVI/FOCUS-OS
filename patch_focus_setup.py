import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

card_pattern = r'                        // Section 2: Allowed Apps.*?                        // Section 3: Lock & Security Mode'

new_content = re.sub(card_pattern, '                        // Section 3: Lock & Security Mode', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(new_content)
