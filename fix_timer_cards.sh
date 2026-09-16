sed -i '535,542c\
            Card(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .neumorphic(20.dp)\
                    .testTag("allowed_apps_container"),\
                colors = CardDefaults.cardColors(containerColor = FocusSurface),\
                shape = RoundedCornerShape(20.dp),\
                border = BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f))\
            ) {' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
sed -i '700,706c\
            Card(\
                colors = CardDefaults.cardColors(containerColor = FocusSurface),\
                shape = RoundedCornerShape(22.dp),\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .neumorphic(22.dp),\
                border = BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f))\
            ) {' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
