import re

with open("app/src/main/java/com/example/data/model/Models.kt", "r") as f:
    content = f.read()

old_enum = """enum class LockMode(val title: String, val description: String) {
    NORMAL("Normal", "Standard Pomodoro countdown. Switch apps freely."),
    SOFT_LOCK("Soft Lock (Level 1)", "Warning overlay alerts you when attempting to switch apps."),
    STRICT_LOCK("Strict Lock (Level 2)", "Locks phone to Focus App & Whitelisted Apps only. Anti-Exit protection."),
    MAXIMUM_LOCK("Maximum Lock (Level 3)", "Kiosk lockdown mode. Emergency exit requires 200s delay penalty.")
}"""

new_enum = """enum class LockMode(val title: String, val description: String) {
    NORMAL("Normal", "Standard Pomodoro countdown. Switch apps freely."),
    SOFT_LOCK("Soft Lock (Level 1)", "Warning toast alerts you when attempting to switch apps."),
    MAXIMUM_LOCK("Maximum Lock (Level 2)", "Kiosk lockdown mode. Emergency exit requires 300s delay penalty.")
}"""

content = content.replace(old_enum, new_enum)
# Just in case the description differs
content = re.sub(r'enum class LockMode.*?}', new_enum, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/model/Models.kt", "w") as f:
    f.write(content)
