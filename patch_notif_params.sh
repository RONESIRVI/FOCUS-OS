sed -i 's/val dismissedIds = viewModel.dismissedNotificationIds.value//g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

sed -i 's/summaryStats: StudySummaryStats,/summaryStats: StudySummaryStats,\n    dismissedIds: Set<String>,\n    onDismissNotification: (String) -> Unit,/g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

sed -i 's/list\n    }/list.filter { it.id !in dismissedIds }\n    }/g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

sed -i 's/summaryStats = summaryStats,/summaryStats = summaryStats,\n            dismissedIds = viewModel.dismissedNotificationIds.collectAsState().value,\n            onDismissNotification = { viewModel.dismissNotification(it) },/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
