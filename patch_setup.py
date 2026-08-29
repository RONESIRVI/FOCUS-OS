import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

state_vars = """    val whitelistedApps by viewModel.whitelistedAppsManual.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState(initial = emptyList())
    var showValidationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var validationConflicts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FocusSession>>(emptyList()) }
    var nextValidationSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.FocusSession?>(null) }"""
content = content.replace("    val whitelistedApps by viewModel.whitelistedAppsManual.collectAsState()", state_vars, 1)

old_btn = """                                        onClick = {
                                            val finalSubject = if (customSubject.isNotBlank()) customSubject else "Focus Session"
                                            val finalGoal = if (customGoal.isNotBlank()) customGoal else "General Study"
                                            
                                            if (customSubject.isNotBlank() && subjects.none { it.name.equals(customSubject.trim(), ignoreCase = true) }) {
                                                viewModel.addCustomSubject(customSubject.trim(), "#0284C7")
                                            }

                                            viewModel.updateSetup(
                                                sessionName = finalGoal,
                                                subjectName = finalSubject,
                                                durationMinutes = customDuration.toInt()
                                            )
                                            onStartSession()
                                        }"""

new_btn = """                                        onClick = {
                                            val finalSubject = if (customSubject.isNotBlank()) customSubject else "Focus Session"
                                            val finalGoal = if (customGoal.isNotBlank()) customGoal else "General Study"
                                            
                                            val duration = customDuration.toInt()
                                            val userStart = System.currentTimeMillis()
                                            val userEnd = userStart + (duration * 60 * 1000L)
                                            val conflicts = scheduledSessions.filter { it.status == "SCHEDULED" }.filter { s ->
                                                val sStart = s.scheduledStartTime ?: return@filter false
                                                val sEnd = s.scheduledEndTime ?: return@filter false
                                                userStart < sEnd && userEnd > sStart
                                            }
                                            
                                            if (conflicts.isNotEmpty()) {
                                                validationConflicts = conflicts
                                                nextValidationSession = scheduledSessions.filter { it.status == "SCHEDULED" && (it.scheduledStartTime ?: 0) >= userEnd }.minByOrNull { it.scheduledStartTime ?: 0 }
                                                showValidationDialog = true
                                            } else {
                                                if (customSubject.isNotBlank() && subjects.none { it.name.equals(customSubject.trim(), ignoreCase = true) }) {
                                                    viewModel.addCustomSubject(customSubject.trim(), "#0284C7")
                                                }

                                                viewModel.updateSetup(
                                                    sessionName = finalGoal,
                                                    subjectName = finalSubject,
                                                    durationMinutes = duration
                                                )
                                                onStartSession()
                                            }
                                        }"""
content = content.replace(old_btn, new_btn)

# We need to insert the dialog code at the end of the composable or somewhere safe, like right before the final `}`
# Let's find a good place. It's inside a Box or Surface?
# Wait, `FocusSetupScreen` top level is a `Scaffold`. 
# We can just put it at the very end of the file before `}` closing `FocusSetupScreen`?
