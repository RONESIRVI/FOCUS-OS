import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('LockMode.STRICT_LOCK', 'LockMode.MAXIMUM_LOCK')

# Clean up redundant conditions like `state.lockMode == LockMode.MAXIMUM_LOCK || state.lockMode == LockMode.MAXIMUM_LOCK`
content = content.replace('state.lockMode == LockMode.MAXIMUM_LOCK || state.lockMode == LockMode.MAXIMUM_LOCK', 'state.lockMode == LockMode.MAXIMUM_LOCK')
content = content.replace('setup.lockMode == LockMode.MAXIMUM_LOCK || setup.lockMode == LockMode.MAXIMUM_LOCK', 'setup.lockMode == LockMode.MAXIMUM_LOCK')
content = content.replace('currentMode == LockMode.MAXIMUM_LOCK || currentMode == LockMode.MAXIMUM_LOCK', 'currentMode == LockMode.MAXIMUM_LOCK')

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
