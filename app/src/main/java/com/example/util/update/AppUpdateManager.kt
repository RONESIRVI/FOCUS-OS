package com.example.util.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val apkUrl: String,
    val fileSizeMB: String = "15 MB",
    val isMandatory: Boolean = false,
    val releaseDate: String = ""
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateStatus()
    data class NoUpdate(val currentVersion: String) : UpdateStatus()
    data class Downloading(val progressPercent: Int, val downloadedMB: Float, val totalMB: Float) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File, val info: UpdateInfo) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    
    // Default fallback update manifest endpoint (can be customized via settings)
    private const val PREFS_NAME = "FocusPrefs"
    private const val PREF_UPDATE_URL = "APP_UPDATE_MANIFEST_URL"
    private const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/firojmansuri/FocusOS-Releases/main/version.json"

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun resetStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    fun getUpdateManifestUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_UPDATE_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
    }

    fun setUpdateManifestUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_UPDATE_URL, url).apply()
    }

    /**
     * Checks if a newer version of the app is available online.
     * Compares versionCode or versionName against current app build.
     */
    fun checkForUpdates(context: Context, isManual: Boolean = false) {
        if (_updateStatus.value is UpdateStatus.Checking || _updateStatus.value is UpdateStatus.Downloading) {
            return
        }

        _updateStatus.value = UpdateStatus.Checking

        scope.launch {
            try {
                val manifestUrl = getUpdateManifestUrl(context)
                val url = URL(manifestUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "FocusOS-Android/${BuildConfig.VERSION_NAME}")
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val latestVersionCode = json.optInt("versionCode", 0)
                    val latestVersionName = json.optString("versionName", "")
                    val releaseNotes = json.optString("releaseNotes", "Performance improvements, security lockdown updates and bug fixes.")
                    val apkUrl = json.optString("apkUrl", "")
                    val fileSizeMB = json.optString("fileSizeMB", "15 MB")
                    val isMandatory = json.optBoolean("isMandatory", false)
                    val releaseDate = json.optString("releaseDate", "Latest")

                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val currentVersionName = BuildConfig.VERSION_NAME

                    Log.d(TAG, "Current build: $currentVersionCode ($currentVersionName), Latest online: $latestVersionCode ($latestVersionName)")

                    if (latestVersionCode > currentVersionCode && apkUrl.isNotEmpty()) {
                        val updateInfo = UpdateInfo(
                            versionName = latestVersionName.ifEmpty { "v${latestVersionCode}" },
                            versionCode = latestVersionCode,
                            releaseNotes = releaseNotes,
                            apkUrl = apkUrl,
                            fileSizeMB = fileSizeMB,
                            isMandatory = isMandatory,
                            releaseDate = releaseDate
                        )
                        _updateStatus.value = UpdateStatus.UpdateAvailable(updateInfo)
                    } else {
                        _updateStatus.value = UpdateStatus.NoUpdate(currentVersionName)
                    }
                } else {
                    Log.w(TAG, "Update check server responded with HTTP $responseCode")
                    if (isManual) {
                        _updateStatus.value = UpdateStatus.Error("Unable to reach update server (HTTP $responseCode).")
                    } else {
                        _updateStatus.value = UpdateStatus.Idle
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for updates", e)
                if (isManual) {
                    _updateStatus.value = UpdateStatus.Error(e.localizedMessage ?: "Network connection error while checking for updates.")
                } else {
                    _updateStatus.value = UpdateStatus.Idle
                }
            }
        }
    }

    /**
     * Downloads the APK file in the background with real-time progress.
     */
    fun startDownload(context: Context, info: UpdateInfo) {
        if (_updateStatus.value is UpdateStatus.Downloading) return

        _updateStatus.value = UpdateStatus.Downloading(0, 0f, 0f)

        scope.launch {
            try {
                val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val targetFile = File(updatesDir, "FocusOS_${info.versionName}.apk")
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                val url = URL(info.apkUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "FocusOS-Android/${BuildConfig.VERSION_NAME}")
                }

                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    throw IllegalStateException("Server returned HTTP $responseCode during APK download")
                }

                val contentLength = connection.contentLength
                val totalMB = if (contentLength > 0) contentLength / (1024f * 1024f) else 15f

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = connection.inputStream
                    outputStream = FileOutputStream(targetFile)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastUpdatePercent = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        val percent = if (contentLength > 0) ((totalBytesRead * 100) / contentLength).toInt() else 0
                        val downloadedMB = totalBytesRead / (1024f * 1024f)

                        if (percent != lastUpdatePercent) {
                            lastUpdatePercent = percent
                            _updateStatus.value = UpdateStatus.Downloading(percent, downloadedMB, totalMB)
                        }
                    }
                    outputStream.flush()
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }

                if (targetFile.exists() && targetFile.length() > 1000) {
                    _updateStatus.value = UpdateStatus.ReadyToInstall(targetFile, info)
                    // Launch installer automatically
                    withContext(Dispatchers.Main) {
                        installApk(context, targetFile)
                    }
                } else {
                    _updateStatus.value = UpdateStatus.Error("Downloaded update file is corrupted or incomplete.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading APK", e)
                _updateStatus.value = UpdateStatus.Error(e.localizedMessage ?: "Failed to download update APK.")
            }
        }
    }

    /**
     * Triggers the native Android Package Installer for the downloaded APK.
     * All existing user data, Room database records, stats, and settings will remain 100% safe.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Log.e(TAG, "APK file does not exist at ${apkFile.absolutePath}")
                return
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context.applicationContext,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            _updateStatus.value = UpdateStatus.Error("Install failed: ${e.localizedMessage}")
        }
    }
}
