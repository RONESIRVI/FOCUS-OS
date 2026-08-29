sed -i '1224,1231d' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i '1224i\
        }\
        item { Spacer(modifier = Modifier.height(60.dp)) }\
    }\
}
' app/src/main/java/com/example/ui/screens/HomeScreen.kt
