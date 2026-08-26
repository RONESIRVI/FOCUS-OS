import re

with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "r") as f:
    content = f.read()

# 1. Update signature of showBlockedOverlay
old_sig = """    fun showBlockedOverlay(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>> = emptyList() // Pair(packageName, appName)
    ) {"""

new_sig = """    fun showBlockedOverlay(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>> = emptyList(), // Pair(packageName, appName)
        isSoftLock: Boolean = false
    ) {"""
content = content.replace(old_sig, new_sig)

# 2. Update updateOverlayContent call
content = content.replace(
    'updateOverlayContent(context, blockedPackage, remainingSeconds, subjectName, allowedPackages)',
    'updateOverlayContent(context, blockedPackage, remainingSeconds, subjectName, allowedPackages)\n                    if (isSoftLock) scheduleSoftLockDismiss()'
)

# 3. Update createOverlayView call
content = content.replace(
    'val view = createOverlayView(context, blockedPackage, remainingSeconds, subjectName, allowedPackages)',
    'val view = createOverlayView(context, blockedPackage, remainingSeconds, subjectName, allowedPackages, isSoftLock)\n                if (isSoftLock) scheduleSoftLockDismiss()'
)

# 4. Update createOverlayView signature
old_create = """    private fun createOverlayView(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>>
    ): View {"""
new_create = """    private fun createOverlayView(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>>,
        isSoftLock: Boolean
    ): View {"""
content = content.replace(old_create, new_create)

# 5. Hide Return button if soft lock
old_btn = """        // Primary Action: Return to Focus Timer
        val returnBtn = Button(context).apply {
            text = "RETURN TO FOCUS TIMER"
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#0284C7")) // Primary blue
            setPadding(32, 24, 32, 24)
            setOnClickListener {
                dismissOverlay()
                bringAppToFront(context, blockedPackage)
            }
        }
        rootLayout.addView(returnBtn)"""
new_btn = """        // Primary Action: Return to Focus Timer
        val returnBtn = Button(context).apply {
            if (isSoftLock) {
                text = "WAITING 30 SECONDS..."
                isEnabled = false
                setBackgroundColor(android.graphics.Color.parseColor("#475569")) // Slate 600
            } else {
                text = "RETURN TO FOCUS TIMER"
                setBackgroundColor(android.graphics.Color.parseColor("#0284C7")) // Primary blue
                setOnClickListener {
                    dismissOverlay()
                    bringAppToFront(context, blockedPackage)
                }
            }
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(32, 24, 32, 24)
        }
        rootLayout.addView(returnBtn)"""
content = content.replace(old_btn, new_btn)

# 6. Add scheduleSoftLockDismiss method
new_methods = """
    private var softLockRunnable: Runnable? = null
    
    private fun scheduleSoftLockDismiss() {
        softLockRunnable?.let { mainHandler.removeCallbacks(it) }
        val r = Runnable { 
            dismissOverlay()
        }
        softLockRunnable = r
        mainHandler.postDelayed(r, 30000L) // 30 seconds
    }
"""
content = content.replace('private fun updateOverlayContent', new_methods + '\n    private fun updateOverlayContent')

with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "w") as f:
    f.write(content)

