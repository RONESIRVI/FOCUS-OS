sed -i 's/border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.4f)),/border = BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f)),/g' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i 's/border = BorderStroke(1.dp, FocusOutline),/border = BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f)),/g' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '416s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '582s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '760s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '1035s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '1248s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '1342s/modifier = Modifier.fillMaxWidth()/modifier = Modifier.fillMaxWidth().neumorphic(20.dp)/' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
