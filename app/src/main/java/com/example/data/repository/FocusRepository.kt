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
    val scheduledSessions: Flow<List<FocusSession>> = focusDao.getScheduledSessions()
    fun allowedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>> = focusDao.getAllowedApps(profile)
    fun whitelistedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>> = focusDao.getWhitelistedApps(profile)
    val allSubjects: Flow<List<SubjectTask>> = focusDao.getAllSubjects()

    suspend fun initializeDefaultDataIfEmpty(context: Context) {
        val existingAppsManual = allowedApps("MANUAL").first()
        val existingAppsStrict = allowedApps("STRICT").first()
        
        if (existingAppsManual.isEmpty() || existingAppsStrict.isEmpty()) {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
            
            val appsToInsert = mutableListOf<AllowedApp>()
            
            val uniquePackages = resolveInfoList.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(packageManager).toString()
                if (packageName.isNotEmpty()) Pair(packageName, appName) else null
            }.distinctBy { it.first }
            
            uniquePackages.forEach { (packageName, appName) ->
                val isStudyTool = packageName.contains("calculator", ignoreCase = true) ||
                        packageName.contains("dictionary", ignoreCase = true) ||
                        packageName.contains("drive", ignoreCase = true) ||
                        packageName.contains("classroom", ignoreCase = true) ||
                        appName.contains("calculator", ignoreCase = true) ||
                        appName.contains("dictionary", ignoreCase = true) ||
                        appName.contains("notes", ignoreCase = true) ||
                        appName.contains("clock", ignoreCase = true)

                val category = if (isStudyTool) "Study Utility" else "Application"

                if (existingAppsManual.isEmpty()) {
                    appsToInsert.add(AllowedApp(packageName, "MANUAL", appName, category, isStudyTool))
                }
                if (existingAppsStrict.isEmpty()) {
                    appsToInsert.add(AllowedApp(packageName, "STRICT", appName, category, isStudyTool))
                }
            }
            
            if (appsToInsert.isNotEmpty()) {
                focusDao.insertOrUpdateApps(appsToInsert)
            }
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
