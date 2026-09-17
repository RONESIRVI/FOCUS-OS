sed -i 's/Text("Apply", color = FocusTextPrimary, fontWeight = FontWeight.Bold)/Text("Apply", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)/g' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt
sed -i 's/Text("Select Range", color = FocusTextPrimary, fontWeight = FontWeight.Bold)/Text("Select Range", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)/g' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt
sed -i 's/tint = FocusTextPrimary,/tint = androidx.compose.ui.graphics.Color.White,/g' app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt
sed -i 's/color = FocusTextPrimary/color = androidx.compose.ui.graphics.Color.White/g' app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt
