import re

with open("app/src/main/java/com/example/data/repository/FocusRepository.kt", "r") as f:
    content = f.read()

pattern = r'        val existingSubjects = allSubjects\.first\(\).*?        \}'

new_content = re.sub(pattern, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/repository/FocusRepository.kt", "w") as f:
    f.write(new_content)
