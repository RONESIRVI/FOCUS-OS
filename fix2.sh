sed -i '916,929c\
                                Box(\
                                    modifier = Modifier\
                                        .fillMaxWidth()\
                                        .neumorphic(14.dp)\
                                        .background(FocusBackground, RoundedCornerShape(14.dp))\
                                ) {\
                                OutlinedTextField(\
                                    value = sessionName,\
                                    onValueChange = { sessionName = it },\
                                    modifier = Modifier\
                                        .fillMaxWidth()\
                                        .testTag("schedule_session_input"),\
                                    placeholder = {\
                                        Text(\
                                            "e.g. Chapter 4 Numericals, Solve 30 MCQs, Revise notes...",\
                                            color = FocusTextSecondary.copy(alpha = 0.45f),\
                                            fontSize = 14.sp\
                                        )\
                                    }' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
