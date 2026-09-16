sed -i '950,955c\
            Card(\
                modifier = Modifier.fillMaxWidth().neumorphic(20.dp),\
                colors = CardDefaults.cardColors(containerColor = FocusSurface),\
                shape = RoundedCornerShape(20.dp),\
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f))\
            ) {' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i '1043,1048c\
            Card(\
                modifier = Modifier.fillMaxWidth().neumorphic(20.dp).testTag("streak_card"),\
                colors = CardDefaults.cardColors(containerColor = FocusSurface),\
                shape = RoundedCornerShape(20.dp),\
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f))\
            ) {' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i '1173,1179c\
                        Card(\
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),\
                            shape = RoundedCornerShape(16.dp),\
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline.copy(alpha=0.5f)),\
                            modifier = Modifier.fillMaxWidth().neumorphic(16.dp)\
                        ) {' app/src/main/java/com/example/ui/screens/HomeScreen.kt
