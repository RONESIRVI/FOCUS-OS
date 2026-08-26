import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

# It has:
#             LockMode.STRICT_LOCK -> {
#                 onDistractionListener?.invoke(blockedPackageName, false)
#                 if (hasOverlayPerm) {
# ...
#                     } catch (e: Exception) {}
#                 }
#             }

# We can just remove the whole Strict Lock block.
pattern = r'LockMode\.STRICT_LOCK -> \{.*?\}(?=\n\s+LockMode\.MAXIMUM_LOCK ->)'

new_content = re.sub(pattern, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(new_content)
