awk '
/fun startTimer\(/ {
    print "    fun startTimer("
    print "        durationMinutes: Int,"
    print "        sessionName: String,"
    print "        subjectName: String,"
    print "        lockMode: LockMode,"
    print "        soundType: SoundType,"
    print "        requiresSelfie: Boolean = false,"
    print "        isScheduled: Boolean = false,"
    print "        isSpecialSession: Boolean = false"
    print "    ) {"
    skip = 1
    next
}
skip == 1 && /isScheduled: Boolean = false/ { next }
skip == 1 && /) {/ { skip = 0; next }
/val isScheduled = isScheduled/ {
    print $0
    print "            isSpecialSession = isSpecialSession,"
    next
}
{ print $0 }
' app/src/main/java/com/example/services/FocusTimerService.kt > tmp.kt
mv tmp.kt app/src/main/java/com/example/services/FocusTimerService.kt
