package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.example.data.db.FocusDao
import com.example.data.model.AllowedApp
import com.example.data.model.FocusSession
import com.example.data.model.SubjectTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusRepository(private val focusDao: FocusDao) {

    val allSessions: Flow<List<FocusSession>> = focusDao.getAllSessions()
    suspend fun getSessionById(sessionId: Long): FocusSession? = focusDao.getSessionById(sessionId)
    val scheduledSessions: Flow<List<FocusSession>> = focusDao.getScheduledSessions()
    fun allowedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>> = focusDao.getAllowedApps(profile)
    fun whitelistedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>> = focusDao.getWhitelistedApps(profile)
    val allSubjects: Flow<List<SubjectTask>> = focusDao.getAllSubjects()

    suspend fun getWhitelistedAppsList(profile: String = "MANUAL"): List<AllowedApp> = focusDao.getWhitelistedAppsList(profile)

    suspend fun initializeDefaultDataIfEmpty(context: Context) {
        val existingAppsManual = allowedApps("MANUAL").first()
        val existingAppsStrict = allowedApps("STRICT").first()
        val existingAppsSpecial = allowedApps("SPECIAL").first()

        val defaultPopularApps = listOf(
            Triple("com.google.android.youtube", "YouTube", "Entertainment"),
            Triple("com.instagram.android", "Instagram", "Social"),
            Triple("com.whatsapp", "WhatsApp", "Communication"),
            Triple("com.android.chrome", "Google Chrome", "Browser"),
            Triple("org.telegram.messenger", "Telegram", "Communication"),
            Triple("com.facebook.katana", "Facebook", "Social"),
            Triple("com.twitter.android", "X (Twitter)", "Social"),
            Triple("com.zhiliaoapp.musically", "TikTok", "Entertainment"),
            Triple("com.reddit.frontpage", "Reddit", "Social"),
            Triple("com.spotify.music", "Spotify", "Music"),
            Triple("com.netflix.mediaclient", "Netflix", "Entertainment"),
            Triple("com.snapchat.android", "Snapchat", "Social"),
            Triple("com.google.android.calculator", "Calculator", "Study Utility"),
            Triple("com.google.android.keep", "Google Keep Notes", "Study Utility"),
            Triple("com.google.android.apps.docs", "Google Drive", "Study Utility"),
            Triple("com.google.android.apps.classroom", "Google Classroom", "Study Utility"),
            Triple("com.google.android.deskclock", "Clock & Timer", "Study Utility"),
            Triple("com.google.android.gm", "Gmail", "Productivity")
        )

        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = try {
            packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val allUniqueApps = mutableMapOf<String, Pair<String, String>>()

        // Add standard curated catalog first
        defaultPopularApps.forEach { (pkg, name, cat) ->
            allUniqueApps[pkg] = Pair(name, cat)
        }

        // Merge installed activities
        resolveInfoList.forEach { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(packageManager).toString()
            if (pkg.isNotEmpty() && !allUniqueApps.containsKey(pkg)) {
                val isStudy = pkg.contains("calc", true) || pkg.contains("note", true) || pkg.contains("clock", true) || pkg.contains("drive", true)
                allUniqueApps[pkg] = Pair(label, if (isStudy) "Study Utility" else "Application")
            }
        }

        val appsToInsert = mutableListOf<AllowedApp>()

        listOf("MANUAL", "STRICT", "SPECIAL").forEach { profile ->
            val existing = when (profile) {
                "STRICT" -> existingAppsStrict
                "SPECIAL" -> existingAppsSpecial
                else -> existingAppsManual
            }
            val existingPkgs = existing.map { it.packageName }.toSet()

            allUniqueApps.forEach { (pkg, pair) ->
                if (!existingPkgs.contains(pkg)) {
                    val (appName, category) = pair
                    val isStudyTool = category == "Study Utility" || appName.contains("calc", true) || appName.contains("note", true) || appName.contains("drive", true)
                    appsToInsert.add(AllowedApp(pkg, profile, appName, category, isAllowed = isStudyTool))
                }
            }
        }

        if (appsToInsert.isNotEmpty()) {
            focusDao.insertOrUpdateApps(appsToInsert)
        }
    }

    suspend fun toggleAppWhitelist(packageName: String, isAllowed: Boolean, profile: String = "MANUAL") {
        focusDao.setAppAllowed(packageName, isAllowed, profile)
    }

    suspend fun saveSession(session: FocusSession): Long {
        val id = focusDao.insertSession(session)
        focusDao.addSubjectTime(session.subjectName, session.completedDurationSeconds)
        return id
    }

    suspend fun updateSession(session: FocusSession) {
        focusDao.updateSession(session)
    }

    suspend fun deleteSession(session: FocusSession) {
        focusDao.deleteSession(session)
    }

    suspend fun deleteSessionById(sessionId: Long) {
        focusDao.deleteSessionById(sessionId)
    }

    suspend fun addSubject(subjectName: String, colorHex: String) {
        focusDao.insertSubject(SubjectTask(name = subjectName, categoryColorHex = colorHex))
    }

    suspend fun deleteSubject(subject: SubjectTask) {
        focusDao.deleteSubject(subject)
    }
}
