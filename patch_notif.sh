sed -i 's/val list = mutableListOf<AppNotification>()/val list = mutableListOf<AppNotification>()\n        val dismissedIds = viewModel.dismissedNotificationIds.value/g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

sed -i 's/val iconTint: Color,/val iconTint: Color,\n    val idString: String = java.util.UUID.randomUUID().toString(),/g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

sed -i 's/modifier = Modifier.fillMaxWidth().testTag("notification_card_${notif.id}")/modifier = Modifier.fillMaxWidth().testTag("notification_card_${notif.id}")/g' app/src/main/java/com/example/ui/dialogs/NotificationCenterDialog.kt

