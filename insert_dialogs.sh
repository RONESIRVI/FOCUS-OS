sed -i '1204i\
    if (showSpecialWhitelistPopup) {\
        AppBlockingSystemDialog(\
            onDismiss = { showSpecialWhitelistPopup = false },\
            onSelectWhitelist = { whitelist ->\
                selectedSpecialWhitelist = whitelist\
                showSpecialWhitelistPopup = false\
                showQuickDurationDialog = true\
            }\
        )\
    }\
    if (showQuickDurationDialog) {\
        QuickDurationDialog(\
            onDismissRequest = { showQuickDurationDialog = false },\
            onSubmit = { duration ->\
                showQuickDurationDialog = false\
                viewModel.startSpecialSession(duration, selectedSpecialWhitelist)\
                onNavigateToTimer()\
            }\
        )\
    }\
' app/src/main/java/com/example/ui/screens/HomeScreen.kt
