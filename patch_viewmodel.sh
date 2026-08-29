awk '
/fun scheduleFocusSession/ {
    print "    fun startSpecialSession(durationMinutes: Int, whitelistType: String) {"
    print "        val context = getApplication<Application>()"
    print "        "
    print "        // Select allowed apps based on whitelist type"
    print "        val allowedList = when (whitelistType) {"
    print "            \"STRICT\" -> whitelistedAppsStrict.value.filter { it.isAllowed }.map { it.packageName }"
    print "            \"SPECIAL\" -> whitelistedAppsSpecial.value.filter { it.isAllowed }.map { it.packageName }"
    print "            else -> whitelistedAppsManual.value.filter { it.isAllowed }.map { it.packageName }"
    print "        }"
    print "        "
    print "        FocusLockManager.updateFocusState("
    print "            isActive = true,"
    print "            lockMode = LockMode.MAXIMUM_LOCK,"
    print "            allowedPackageNames = allowedList"
    print "        )"
    print "        "
    print "        val intent = Intent(context, FocusTimerService::class.java).apply {"
    print "            action = \"ACTION_START_TIMER\""
    print "            putExtra(\"DURATION\", durationMinutes)"
    print "            putExtra(\"SESSION_NAME\", \"Special Whitelist Focus\")"
    print "            putExtra(\"SUBJECT_NAME\", \"Special Focus\")"
    print "            putExtra(\"LOCK_MODE\", LockMode.MAXIMUM_LOCK.name)"
    print "            putExtra(\"SOUND_TYPE\", SoundOption.SILENT.name)"
    print "            putExtra(\"REQUIRES_SELFIE\", false)"
    print "            putExtra(\"IS_SPECIAL_WHITELIST_SESSION\", true) // custom flag"
    print "        }"
    print "        "
    print "        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {"
    print "            context.startForegroundService(intent)"
    print "        } else {"
    print "            context.startService(intent)"
    print "        }"
    print "    }"
    print ""
    print $0
    next
}
{ print $0 }
' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt > tmp.kt
mv tmp.kt app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt
