import re

with open("/tmp/settings_full.txt", "r") as f:
    lines = f.readlines()

# The original content is in lines but prefixed with line numbers like "   270\t        }"
# Wait, I can just use git checkout to get the file, but it said not a git repo.
# Did I back it up? Yes, /tmp/settings_full.txt has the line numbers. I can strip them.

clean_lines = []
for line in lines:
    idx = line.find("\t")
    if idx != -1:
        clean_lines.append(line[idx+1:])
    else:
        clean_lines.append(line)

content = "".join(clean_lines)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

