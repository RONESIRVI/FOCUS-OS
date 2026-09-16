sed -i '/testTag("setup_subject_input"),/a \
                                        colors = OutlinedTextFieldDefaults.colors(\
                                            focusedBorderColor = Color.Transparent,\
                                            unfocusedBorderColor = Color.Transparent,\
                                            focusedContainerColor = Color.Transparent,\
                                            unfocusedContainerColor = Color.Transparent,\
                                            focusedTextColor = FocusTextPrimary,\
                                            unfocusedTextColor = FocusTextPrimary\
                                        ),' app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt
sed -i '/testTag("schedule_subject_input"),/a \
                                        colors = OutlinedTextFieldDefaults.colors(\
                                            focusedBorderColor = Color.Transparent,\
                                            unfocusedBorderColor = Color.Transparent,\
                                            focusedContainerColor = Color.Transparent,\
                                            unfocusedContainerColor = Color.Transparent,\
                                            focusedTextColor = FocusTextPrimary,\
                                            unfocusedTextColor = FocusTextPrimary\
                                        ),' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
sed -i '/testTag("schedule_session_input"),/a \
                                        colors = OutlinedTextFieldDefaults.colors(\
                                            focusedBorderColor = Color.Transparent,\
                                            unfocusedBorderColor = Color.Transparent,\
                                            focusedContainerColor = Color.Transparent,\
                                            unfocusedContainerColor = Color.Transparent,\
                                            focusedTextColor = FocusTextPrimary,\
                                            unfocusedTextColor = FocusTextPrimary\
                                        ),' app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt
