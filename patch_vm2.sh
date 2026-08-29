sed -i '49,53d' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt
sed -i '48a\    val startPhotoUri: String? = null,\n    val endSelfieUri: String? = null,\n    val whitelistProfile: String = "STRICT"\n)' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt
