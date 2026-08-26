import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val completedSessions = sessions.filter { it.completedDurationSeconds > 0 || it.status == "COMPLETED" }',
    'val completedSessions = sessions.filter { it.completedDurationSeconds > 0 || it.status == "COMPLETED" || it.status == "ARCHIVED" }'
)

old_delete_repo = "            repository.deleteSession(session)"
new_delete_repo = """            if (session.status == "COMPLETED" || session.completedDurationSeconds > 0) {
                repository.updateSession(session.copy(status = "ARCHIVED"))
            } else {
                repository.deleteSession(session)
            }"""

content = content.replace(old_delete_repo, new_delete_repo)

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
