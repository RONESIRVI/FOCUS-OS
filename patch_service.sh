awk '
/val isScheduled = intent\.getBooleanExtra\("IS_SCHEDULED", false\)/ {
    print $0
    print "                val isSpecialSession = intent.getBooleanExtra(\"IS_SPECIAL_WHITELIST_SESSION\", false)"
    next
}
/startTimer\(duration, sessionName, subjectName, lockMode, soundType, requiresSelfie, isScheduled\)/ {
    print "                startTimer(duration, sessionName, subjectName, lockMode, soundType, requiresSelfie, isScheduled, isSpecialSession)"
    next
}
/private fun startTimer\(/ {
    print "    private fun startTimer("
    print "        duration: Int,"
    print "        sessionName: String,"
    print "        subjectName: String,"
    print "        lockMode: LockMode,"
    print "        soundType: SoundType,"
    print "        requiresSelfie: Boolean,"
    print "        isScheduled: Boolean,"
    print "        isSpecialSession: Boolean = false"
    print "    ) {"
    skip = 1
    next
}
skip == 1 && /isScheduled: Boolean/ { next }
skip == 1 && /) {/ { skip = 0; next }
/val isScheduled = isScheduled/ {
    print $0
    print "            isSpecialSession = isSpecialSession,"
    next
}
{ print $0 }
' app/src/main/java/com/example/services/FocusTimerService.kt > tmp.kt
mv tmp.kt app/src/main/java/com/example/services/FocusTimerService.kt
