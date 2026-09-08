# Project Custom Rules

## App Versioning Rule
- **CRITICAL RULE**: Whenever any code change, bug fix, or new feature is implemented in this app, you MUST automatically increment the `versionCode` and update the `versionName` in `app/build.gradle.kts`. 
- **Reason**: The user relies on an In-App Update flow. If the version is not incremented, the Android Package Installer will reject the new APK as a duplicate/downgrade. Always increment the version!
