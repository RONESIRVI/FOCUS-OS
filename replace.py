import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r"Spacer\(modifier = Modifier\.height\(12\.dp\)\)\s+Row\(\s+modifier = Modifier\s+\.fillMaxWidth\(\)\s+\.horizontalScroll\(rememberScrollState\(\)\),\s+horizontalArrangement = Arrangement\.spacedBy\(12\.dp\)\s+\) \{\s+SoundType\.entries\.forEach \{ sound ->.*?maxLines = 3\s+\)\s+\}\s+\}\s+\}\s+\}", re.DOTALL)

replacement = """Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SoundType.entries.forEach { sound ->
                                val isSelected = selectedSound == sound
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) FocusPrimary else FocusSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSound = sound }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(
                                                    if (isSelected) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                    androidx.compose.foundation.shape.CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (sound.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = sound.label,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) FocusPrimary else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (sound.name != "NONE") {
                                                    Text(
                                                        text = sound.badge,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                        modifier = Modifier
                                                            .background(
                                                                if (isSelected) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.hindiTitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = FocusWarning
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                                color = FocusTextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedSound = sound },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = FocusPrimary,
                                                unselectedColor = FocusTextSecondary
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }"""

new_content, count = pattern.subn(replacement, content)
if count > 0:
    with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
        f.write(new_content)
    print("Success ScheduleCreateScreen")
else:
    print("Failed ScheduleCreateScreen")
    
# Now for FocusSetupScreen.kt
with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content2 = f.read()

pattern2 = re.compile(r"Spacer\(modifier = Modifier\.height\(12\.dp\)\)\s+LazyRow\(horizontalArrangement = Arrangement\.spacedBy\(12\.dp\)\) \{\s+items\(SoundType\.entries\) \{ st ->.*?maxLines = 3\s+\)\s+\}\s+\}\s+\}\s+\}", re.DOTALL)

replacement2 = """Spacer(modifier = Modifier.height(14.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SoundType.entries.forEach { st ->
                                        val isSel = setup.selectedSound == st
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSel) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                            border = BorderStroke(
                                                width = if (isSel) 1.5.dp else 1.dp,
                                                color = if (isSel) FocusPrimary else FocusSurfaceVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.updateSetup(soundType = st) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .background(
                                                            if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                            androidx.compose.foundation.shape.CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (st.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = if (isSel) FocusPrimary else FocusTextSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = st.label,
                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = if (isSel) FocusPrimary else Color.White
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        if (st.name != "NONE") {
                                                            Text(
                                                                text = st.badge,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                color = if (isSel) FocusPrimary else FocusTextSecondary,
                                                                modifier = Modifier
                                                                    .background(
                                                                        if (isSel) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant,
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = st.hindiTitle,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                        color = FocusWarning
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = st.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                                        color = FocusTextSecondary,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                RadioButton(
                                                    selected = isSel,
                                                    onClick = { viewModel.updateSetup(soundType = st) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = FocusPrimary,
                                                        unselectedColor = FocusTextSecondary
                                                    ),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }"""

new_content2, count2 = pattern2.subn(replacement2, content2)
if count2 > 0:
    with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
        f.write(new_content2)
    print("Success FocusSetupScreen")
else:
    print("Failed FocusSetupScreen")
    
