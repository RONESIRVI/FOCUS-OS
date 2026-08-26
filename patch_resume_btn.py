import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

old_btns = """                        Button(
                            onClick = {
                                if (emergencyPenaltyCountdown <= 0) {
                                    showEmergencyConfirm = false
                                    viewModel.emergencyExitSession()
                                    onSessionComplete()
                                }
                            },
                            enabled = emergencyPenaltyCountdown <= 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_emergency_exit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusDanger)
                        ) {
                            Text("CONFIRM EARLY EXIT", fontWeight = FontWeight.Bold)
                        }
                    }"""

new_btns = """                        Button(
                            onClick = {
                                if (emergencyPenaltyCountdown <= 0) {
                                    showEmergencyConfirm = false
                                    viewModel.emergencyExitSession()
                                    onSessionComplete()
                                }
                            },
                            enabled = emergencyPenaltyCountdown <= 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_emergency_exit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusDanger)
                        ) {
                            Text("CONFIRM EARLY EXIT", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                showEmergencyConfirm = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusPrimary),
                            border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f))
                        ) {
                            Text("RESUME STUDYING", fontWeight = FontWeight.Bold)
                        }
                    }"""

content = content.replace(old_btns, new_btns)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
