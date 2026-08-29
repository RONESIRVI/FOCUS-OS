import re

with open("app/src/test/java/com/example/FocusAppSystemTest.kt", "r") as f:
    content = f.read()

# Fix the test where it tests isPackageAllowed without a context
old_test = """        // 1. When focus is NOT active, all apps are allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = false,
            lockMode = LockMode.NORMAL,
            allowedPackageNames = emptyList()
        )
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(blockedApp, ownPkg))"""

new_test = """        // 1. When focus is NOT active, all apps are allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = false,
            lockMode = LockMode.NORMAL,
            allowedPackageNames = emptyList()
        )
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, blockedApp, ownPkg))"""

content = content.replace(old_test, new_test)

old_test2 = """        // 2. When MAXIMUM_LOCK is active with Docs allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = true,
            lockMode = LockMode.MAXIMUM_LOCK,
            allowedPackageNames = listOf(allowedApp)
        )
        // Own package is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(ownPkg, ownPkg))
        
        // Whitelisted study app is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(allowedApp, ownPkg))

        // System essential UI is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed("com.android.systemui", ownPkg))

        // Distracting social media app is BLOCKED
        assertFalse(com.example.util.FocusLockManager.isPackageAllowed(blockedApp, ownPkg))"""

new_test2 = """        // 2. When MAXIMUM_LOCK is active with Docs allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = true,
            lockMode = LockMode.MAXIMUM_LOCK,
            allowedPackageNames = listOf(allowedApp)
        )
        // Own package is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, ownPkg, ownPkg))
        
        // Whitelisted study app is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, allowedApp, ownPkg))

        // System essential UI is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, "com.android.systemui", ownPkg))

        // Distracting social media app is BLOCKED
        assertFalse(com.example.util.FocusLockManager.isPackageAllowed(null, blockedApp, ownPkg))"""

content = content.replace(old_test2, new_test2)

with open("app/src/test/java/com/example/FocusAppSystemTest.kt", "w") as f:
    f.write(content)
