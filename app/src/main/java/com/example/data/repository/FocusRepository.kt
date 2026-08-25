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
    val allowedApps: Flow<List<AllowedApp>> = focusDao.getAllowedApps()
    val whitelistedApps: Flow<List<AllowedApp>> = focusDao.getWhitelistedApps()
    val allSubjects: Flow<List<SubjectTask>> = focusDao.getAllSubjects()

    suspend fun initializeDefaultDataIfEmpty(context: Context) {
        val existingApps = allowedApps.first()
        if (existingApps.isEmpty()) {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
            val defaultApps = resolveInfoList.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(packageManager).toString()
                // Prevent duplicate or empty packages
                if (packageName.isNotEmpty()) {
                    AllowedApp(
                        packageName = packageName,
                        appName = appName,
                        category = "Installed App",
                        isAllowed = false
                    )
                } else null
            }.distinctBy { it.packageName }
            
            if (defaultApps.isNotEmpty()) {
                focusDao.insertOrUpdateApps(defaultApps)
            }
        }

        val existingSubjects = allSubjects.first()
        if (existingSubjects.isEmpty()) {
            val defaultSubjects = listOf(
                SubjectTask(name = "General Study", categoryColorHex = "#0284C7", targetHours = 8f, completedSeconds = 0)
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
