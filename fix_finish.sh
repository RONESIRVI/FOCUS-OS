sed -i '127a\
    LaunchedEffect(isFinishing) {\
        if (isFinishing) {\
            delay(1000)\
            onSessionComplete()\
        }\
    }' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
sed -i 's/onSessionComplete()/isFinishing = true/' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
