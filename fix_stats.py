import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

# Add map import if not there
if "import kotlinx.coroutines.flow.map" not in content:
    content = content.replace("import kotlinx.coroutines.flow.combine", "import kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.map")

old_stats_def = """    private val _summaryStats = MutableStateFlow(StudySummaryStats())
    val summaryStats: StateFlow<StudySummaryStats> = _summaryStats.asStateFlow()"""

new_stats_def = """    val summaryStats: StateFlow<StudySummaryStats> = repository.allSessions.map { sessions ->
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        var todaySec = 0
        var totalDistractions = 0
        
        sessions.forEach {
            if (it.timestamp >= todayStart) {
                todaySec += it.completedDurationSeconds
                totalDistractions += it.distractionAttempts
            }
        }
        
        StudySummaryStats(
            todayFocusSeconds = todaySec,
            totalSessions = sessions.size,
            focusScore = maxOf(60, 100 - (totalDistractions * 5))
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudySummaryStats())"""

content = content.replace(old_stats_def, new_stats_def)

old_update_summary = """                // Update summary statistics
                val updatedToday = _summaryStats.value.todayFocusSeconds + completedSecs
                val newScore = maxOf(60, 100 - (current.distractionAttempts * 5))
                _summaryStats.value = _summaryStats.value.copy(
                    todayFocusSeconds = updatedToday,
                    totalSessions = _summaryStats.value.totalSessions + 1,
                    focusScore = newScore
                )"""

content = content.replace(old_update_summary, "// summaryStats will automatically update via flow")

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
