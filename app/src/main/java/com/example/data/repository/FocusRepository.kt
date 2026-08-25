package com.example.data.repository

import com.example.data.db.FocusDao
import com.example.data.model.AllowedApp
import com.example.data.model.FocusSession
import com.example.data.model.SubjectTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusRepository(private val focusDao: FocusDao) {

    val allSessions: Flow<List<FocusSession>> = focusDao.getAllSessions()
    val allowedApps: Flow<List<AllowedApp>> = focusDao.getAllowedApps()
    val whitelistedApps: Flow<List<AllowedApp>> = focusDao.getWhitelistedApps()
    val allSubjects: Flow<List<SubjectTask>> = focusDao.getAllSubjects()

    suspend fun initializeDefaultDataIfEmpty() {
        val existingApps = allowedApps.first()
        if (existingApps.isEmpty()) {
            val defaultApps = listOf(
                AllowedApp("com.example.notes", "📚 Notes & Journal", "Notes", isAllowed = true),
                AllowedApp("com.adobe.reader", "📖 PDF Reader & Books", "Reader", isAllowed = true),
                AllowedApp("com.google.android.youtube", "🎥 YouTube (Study Channel)", "Video", isAllowed = true),
                AllowedApp("com.android.chrome", "🌐 Browser / Web Research", "Browser", isAllowed = true),
                AllowedApp("com.google.android.calculator", "🧮 Calculator", "Tools", isAllowed = true),
                AllowedApp("com.google.android.apps.docs", "📁 Google Drive & Class Notes", "Notes", isAllowed = true),
                AllowedApp("com.ichi2.anki", "🎴 Anki Flashcards", "Study", isAllowed = true),
                AllowedApp("org.wikipedia", "🌐 Wikipedia", "Browser", isAllowed = true),
                AllowedApp("notion.id", "📝 Notion Workspace", "Notes", isAllowed = false),
                AllowedApp("org.telegram.messenger", "💬 Telegram Study Group", "Social", isAllowed = false),
                AllowedApp("com.instagram.android", "📸 Instagram", "Social Blocked", isAllowed = false),
                AllowedApp("com.whatsapp", "💬 WhatsApp", "Social Blocked", isAllowed = false),
                AllowedApp("com.facebook.katana", "👥 Facebook", "Social Blocked", isAllowed = false),
                AllowedApp("com.pubg.imobile", "🎮 Battlegrounds / Games", "Games Blocked", isAllowed = false)
            )
            focusDao.insertOrUpdateApps(defaultApps)
        }

        val existingSubjects = allSubjects.first()
        if (existingSubjects.isEmpty()) {
            val defaultSubjects = listOf(
                SubjectTask(name = "UPSC GS STUDY", categoryColorHex = "#0284C7", targetHours = 10f, completedSeconds = 14400),
                SubjectTask(name = "Rajasthan का इतिहास", categoryColorHex = "#E11D48", targetHours = 8f, completedSeconds = 18000),
                SubjectTask(name = "RAS Self", categoryColorHex = "#2563EB", targetHours = 6f, completedSeconds = 12060),
                SubjectTask(name = "Advance RAS", categoryColorHex = "#38BDF8", targetHours = 6f, completedSeconds = 11760),
                SubjectTask(name = "PYQS Test", categoryColorHex = "#EA580C", targetHours = 5f, completedSeconds = 11760),
                SubjectTask(name = "REVISION", categoryColorHex = "#16A34A", targetHours = 6f, completedSeconds = 8340),
                SubjectTask(name = "Value Addition", categoryColorHex = "#CA8A04", targetHours = 4f, completedSeconds = 3000),
                SubjectTask(name = "MOCK Test", categoryColorHex = "#9333EA", targetHours = 4f, completedSeconds = 2580)
            )
            defaultSubjects.forEach { focusDao.insertSubject(it) }
        }
    }

    suspend fun toggleAppWhitelist(packageName: String, isAllowed: Boolean) {
        focusDao.setAppAllowed(packageName, isAllowed)
    }

    suspend fun saveSession(session: FocusSession): Long {
        val id = focusDao.insertSession(session)
        focusDao.addSubjectTime(session.subjectName, session.completedDurationSeconds)
        return id
    }

    suspend fun addSubject(subjectName: String, colorHex: String) {
        focusDao.insertSubject(SubjectTask(name = subjectName, categoryColorHex = colorHex))
    }
}
