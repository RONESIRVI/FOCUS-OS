sed -i '1166i\
        item {\
            Spacer(modifier = Modifier.height(16.dp))\
            Row(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .clickable { showSpecialWhitelistPopup = true }\
                    .padding(vertical = 12.dp),\
                verticalAlignment = Alignment.CenterVertically,\
                horizontalArrangement = Arrangement.SpaceBetween\
            ) {\
                Row(verticalAlignment = Alignment.CenterVertically) {\
                    Icon(\
                        imageVector = Icons.Default.Shield,\
                        contentDescription = "Special Whitelist",\
                        tint = FocusPrimary,\
                        modifier = Modifier.size(24.dp)\
                    )\
                    Spacer(modifier = Modifier.width(12.dp))\
                    Text(\
                        text = "Special Whitelist Switch",\
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),\
                        color = Color.White\
                    )\
                }\
                Switch(\
                    checked = false,\
                    onCheckedChange = { showSpecialWhitelistPopup = true },\
                    colors = SwitchDefaults.colors(\
                        checkedThumbColor = Color.White,\
                        checkedTrackColor = FocusPrimary,\
                        uncheckedThumbColor = Color.White,\
                        uncheckedTrackColor = FocusSurfaceVariant\
                    )\
                )\
            }\
        }\
' app/src/main/java/com/example/ui/screens/HomeScreen.kt
