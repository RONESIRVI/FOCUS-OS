sed -i 's/modifier = Modifier.fillMaxWidth(),/modifier = Modifier.fillMaxWidth().neumorphic(20.dp),/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/modifier = Modifier.fillMaxWidth().testTag("streak_card"),/modifier = Modifier.fillMaxWidth().neumorphic(20.dp).testTag("streak_card"),/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline)/border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f))/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
