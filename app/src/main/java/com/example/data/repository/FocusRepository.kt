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

    suspend fun syncInstalledApps(context: Context) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = try {
            packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val installedApps = mutableMapOf<String, Pair<String, String>>()
        resolveInfoList.forEach { resolveInfo ->
            val pkg = resolveInfo.activityInfo?.packageName
            if (!pkg.isNullOrEmpty() && pkg != context.packageName) {
                val label = try {
                    resolveInfo.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                } catch (e: Exception) {
                    pkg
                }
                val isStudy = pkg.contains("calc", true) || 
                              pkg.contains("note", true) || 
                              pkg.contains("clock", true) || 
                              pkg.contains("drive", true) || 
                              pkg.contains("doc", true) || 
                              pkg.contains("sheet", true) ||
                              label.contains("calc", true) ||
                              label.contains("note", true) ||
                              label.contains("clock", true)
                val category = if (isStudy) "Study Utility" else "Application"
                installedApps[pkg] = Pair(label, category)
            }
        }

        val installedPackages = installedApps.keys.toList()

        // Remove any apps from DB that are no longer installed on device
        if (installedPackages.isNotEmpty()) {
            focusDao.deleteUninstalledApps(installedPackages)
        }

        val existingAppsManual = allowedApps("MANUAL").first().associateBy { it.packageName }
        val existingAppsStrict = allowedApps("STRICT").first().associateBy { it.packageName }
        val existingAppsSpecial = allowedApps("SPECIAL").first().associateBy { it.packageName }

        val appsToInsert = mutableListOf<AllowedApp>()

        listOf("MANUAL", "STRICT", "SPECIAL").forEach { profile ->
            val existingMap = when (profile) {
                "STRICT" -> existingAppsStrict
                "SPECIAL" -> existingAppsSpecial
                else -> existingAppsManual
            }

            installedApps.forEach { (pkg, pair) ->
                val (appName, category) = pair
                val existingApp = existingMap[pkg]
                if (existingApp != null) {
                    // Update name and category if changed, but keep user's isAllowed choice
                    if (existingApp.appName != appName || existingApp.category != category) {
                        appsToInsert.add(existingApp.copy(appName = appName, category = category))
                    }
                } else {
                    // New installed app - set default allowed status
                    val isStudyTool = category == "Study Utility"
                    appsToInsert.add(AllowedApp(pkg, profile, appName, category, isAllowed = isStudyTool))
                }
            }
        }

        if (appsToInsert.isNotEmpty()) {
            focusDao.insertOrUpdateApps(appsToInsert)
        }
    }

    suspend fun initializeDefaultDataIfEmpty(context: Context) {
        syncInstalledApps(context)
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
