import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

card_pattern = r'                        // Section 3: Lock & Security Mode.*?                        // Section 4: Ambient Focus Sound Generator'

new_content = re.sub(card_pattern, '                        // Section 4: Ambient Focus Sound Generator', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(new_content)
