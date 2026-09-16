sed -i '896,911c\
                                Text(\
                                    text = "TARGET GOAL / CHAPTER (OPTIONAL)",\
                                    style = MaterialTheme.typography.labelSmall.copy(\
                                        fontWeight = FontWeight.Bold,\
                                        letterSpacing = 0.5.sp\
                                    ),\
                                    color = FocusTextSecondary\
                                )\
                                Spacer(modifier = Modifier.height(6.dp))\
                                Box(\
                                    modifier = Modifier\
                                        .fillMaxWidth()\
                                        .neumorphic(14.dp)\
                                        .background(FocusBackground, RoundedCornerShape(14.dp))\
                                ) {' app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt
