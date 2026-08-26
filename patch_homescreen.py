import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

pattern = r'\s*// Quick Focus Session Card\s*item \{\s*Card\(.*?(QUICK FOCUS MODE).*?\}\s*\}\s*\}'
new_content = re.sub(pattern, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(new_content)
