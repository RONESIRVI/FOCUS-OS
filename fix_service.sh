cat << 'INNER_EOF' > patch.diff
--- app/src/main/java/com/example/services/FocusTimerService.kt
+++ app/src/main/java/com/example/services/FocusTimerService.kt
@@ -204,13 +204,14 @@
 
-    fun startTimer(
-        durationMinutes: Int,
-        sessionName: String,
-        subjectName: String,
-        lockMode: LockMode,
-        soundType: SoundType,
-        requiresSelfie: Boolean = false,
-        isScheduled: Boolean = false,
-        isSpecialSession: Boolean = false
-    ) {
-        durationMinutes: Int,
-        sessionName: String,
-        subjectName: String,
-        lockMode: LockMode,
-        soundType: SoundType,
-        requiresSelfie: Boolean = false,
+    fun startTimer(
+        durationMinutes: Int,
+        sessionName: String,
+        subjectName: String,
+        lockMode: LockMode,
+        soundType: SoundType,
+        requiresSelfie: Boolean = false,
+        isScheduled: Boolean = false,
+        isSpecialSession: Boolean = false
+    ) {
         FocusLockManager.clearPendingSchedule()
INNER_EOF
patch -p0 < patch.diff
