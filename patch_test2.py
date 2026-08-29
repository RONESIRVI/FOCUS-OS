import re

with open("app/src/test/java/com/example/FocusAppSystemTest.kt", "r") as f:
    content = f.read()

old_test = """        // Own package is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(ownPkg, ownPkg))
        
        // Whitelisted study app is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(allowedApp, ownPkg))

        // System essential UI is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed("com.android.systemui", ownPkg))"""

new_test = """        // Own package is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, ownPkg, ownPkg))
        
        // Whitelisted study app is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, allowedApp, ownPkg))

        // System essential UI is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, "com.android.systemui", ownPkg))"""

content = content.replace(old_test, new_test)

with open("app/src/test/java/com/example/FocusAppSystemTest.kt", "w") as f:
    f.write(content)
