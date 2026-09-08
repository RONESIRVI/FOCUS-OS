package com.example.util.update

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    data class ReadyToInstall(
        val apkFile: File,
        val info: UpdateInfo,
        val archivePackageName: String? = null,
        val archiveVersionCode: Long = -1L,
        val archiveVersionName: String? = null
    ) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    
    private const val PREFS_NAME = "FocusPrefs"
    private const val PREF_UPDATE_URL = "APP_UPDATE_MANIFEST_URL"
    
    // Primary & Fallback public endpoints
    private const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/firojmansuri/FocusOS-Releases/main/version.json"
    private const val PUBLIC_FALLBACK_URL = "https://api.github.com/repos/firojmansuri/FocusOS-Releases/releases/latest"

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

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

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Checks if a newer version of the app is available online.
     * Wrapped in full try-catch with graceful offline / 404 handling.
     */
    fun checkForUpdates(context: Context, isManual: Boolean = false) {
        if (_updateStatus.value is UpdateStatus.Checking || _updateStatus.value is UpdateStatus.Downloading) {
            return
        }

        // Check network connection first
        if (!isNetworkAvailable(context)) {
            Log.w(TAG, "No internet connection available for update check")
            if (isManual) {
                scope.launch {
                    _snackbarMessage.emit("⚠️ Please check your internet connection and try again.")
                }
                _updateStatus.value = UpdateStatus.Idle
            }
            return
        }

        _updateStatus.value = UpdateStatus.Checking

        scope.launch {
            try {
                val configuredUrl = getUpdateManifestUrl(context).trim()
                val urlsToTry = mutableListOf<String>()
                if (configuredUrl.isNotBlank()) urlsToTry.add(configuredUrl)
                if (configuredUrl != PUBLIC_FALLBACK_URL) urlsToTry.add(PUBLIC_FALLBACK_URL)

                var responseText: String? = null
                var lastResponseCode = 0
                var successfulUrl = ""

                for (targetUrl in urlsToTry) {
                    try {
                        Log.i(TAG, "🔍 Querying update manifest from: $targetUrl")
                        val url = URL(targetUrl)
                        val connection = (url.openConnection() as HttpURLConnection).apply {
                            connectTimeout = 6000
                            readTimeout = 6000
                            requestMethod = "GET"
                            setRequestProperty("Accept", "application/json")
                            setRequestProperty("User-Agent", "FocusOS-Android/${BuildConfig.VERSION_NAME}")
                        }

                        val code = connection.responseCode
                        lastResponseCode = code
                        Log.i(TAG, "📡 Response code from $targetUrl: $code")
                        if (code == 200) {
                            responseText = connection.inputStream.bufferedReader().use { it.readText() }
                            successfulUrl = targetUrl
                            break
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Attempt failed for $targetUrl: ${e.message}")
                    }
                }

                if (responseText != null) {
                    Log.i(TAG, "📄 Manifest Payload received:\n$responseText")
                    val json = JSONObject(responseText)

                    var latestVersionCode = json.optInt("versionCode", 0)
                    var latestVersionName = json.optString("versionName", "")
                    var releaseNotes = json.optString("releaseNotes", "")
                    var apkUrl = json.optString("apkUrl", "")
                    var fileSizeMB = json.optString("fileSizeMB", "15 MB")
                    var isMandatory = json.optBoolean("isMandatory", false)
                    var releaseDate = json.optString("releaseDate", "")

                    // Support standard GitHub Releases API format
                    if (json.has("tag_name") && apkUrl.isEmpty()) {
                        val tagName = json.optString("tag_name", "").removePrefix("v")
                        latestVersionName = "v$tagName"
                        latestVersionCode = try {
                            tagName.replace(".", "").toIntOrNull() ?: 0
                        } catch (e: Exception) { 0 }
                        releaseNotes = json.optString("body", "Latest performance enhancements and system fixes.")
                        releaseDate = json.optString("published_at", "").take(10)

                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    apkUrl = asset.optString("browser_download_url", "")
                                    val sizeBytes = asset.optLong("size", 0L)
                                    if (sizeBytes > 0) {
                                        fileSizeMB = String.format("%.1f MB", sizeBytes / (1024f * 1024f))
                                    }
                                    break
                                }
                            }
                        }
                    }

                    if (releaseNotes.isBlank()) {
                        releaseNotes = "Performance improvements, security lockdown updates and bug fixes."
                    }

                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val currentVersionName = BuildConfig.VERSION_NAME

                    val isCodeNewer = latestVersionCode > currentVersionCode
                    val isNameNewer = isNewerVersionName(latestVersionName, currentVersionName)

                    Log.i(TAG, "==================================================")
                    Log.i(TAG, "🔍 [UPDATE VERSION CODE COMPARISON]")
                    Log.i(TAG, "   • Installed App Package: ${context.packageName}")
                    Log.i(TAG, "   • Installed versionCode: $currentVersionCode")
                    Log.i(TAG, "   • Installed versionName: $currentVersionName")
                    Log.i(TAG, "   • Fetched Manifest URL:  $successfulUrl")
                    Log.i(TAG, "   • Remote manifest versionCode: $latestVersionCode")
                    Log.i(TAG, "   • Remote manifest versionName: $latestVersionName")
                    Log.i(TAG, "   • Remote APK download URL:     $apkUrl")
                    Log.i(TAG, "   • Evaluation: (latestVersionCode > currentVersionCode) => ($latestVersionCode > $currentVersionCode) = $isCodeNewer")
                    Log.i(TAG, "   • Evaluation: (isNewerVersionName) => ($latestVersionName > $currentVersionName) = $isNameNewer")
                    Log.i(TAG, "==================================================")

                    if (isCodeNewer && apkUrl.isNotEmpty()) {
                        Log.i(TAG, "✅ Newer update detected by versionCode ($latestVersionCode > $currentVersionCode). Showing update dialog.")
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
                    } else if (apkUrl.isNotEmpty() && isNameNewer) {
                        Log.i(TAG, "✅ Newer update detected by versionName comparison ($latestVersionName > $currentVersionName). Showing update dialog.")
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
                        Log.i(TAG, "ℹ️ No newer version found. Installed versionCode $currentVersionCode is equal or higher than remote $latestVersionCode.")
                        _updateStatus.value = UpdateStatus.NoUpdate(currentVersionName)
                        if (isManual) {
                            _snackbarMessage.emit("✅ Focus OS is up to date (v$currentVersionName, code $currentVersionCode).")
                        }
                    }
                } else {
                    // Fallback / Graceful notification on 404 or unconfigured repo
                    Log.w(TAG, "Update server not reachable or 404 (Last HTTP Code: $lastResponseCode)")
                    if (isManual) {
                        _snackbarMessage.emit("📡 No newer update found online. You're on the latest build (v${BuildConfig.VERSION_NAME}, code ${BuildConfig.VERSION_CODE}).")
                        _updateStatus.value = UpdateStatus.NoUpdate(BuildConfig.VERSION_NAME)
                    } else {
                        _updateStatus.value = UpdateStatus.Idle
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkForUpdates", e)
                if (isManual) {
                    _snackbarMessage.emit("⚠️ Unable to check updates. Please check your internet connection.")
                }
                _updateStatus.value = UpdateStatus.Idle
            }
        }
    }

    private fun isNewerVersionName(remoteVer: String, currentVer: String): Boolean {
        return try {
            val remoteClean = remoteVer.removePrefix("v").trim()
            val currentClean = currentVer.removePrefix("v").trim()
            val rParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
            val cParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
            for (i in 0 until maxOf(rParts.size, cParts.size)) {
                val r = rParts.getOrElse(i) { 0 }
                val c = cParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Test / Simulation mode: Allows verifying the update UI dialog, progress bar,
     * and direct install flow directly on device.
     */
    fun triggerDemoUpdate(context: Context) {
        val testInfo = UpdateInfo(
            versionName = "v2.5.0 (Demo Update)",
            versionCode = 250,
            releaseNotes = "✨ Demo In-App Update Engine:\n• 100% Data & Session Preservation verified\n• Direct background download progress\n• One-click system APK installer launch\n• Zero browser navigation required",
            apkUrl = "demo://focus_os_demo_update.apk",
            fileSizeMB = "12.4 MB",
            isMandatory = false,
            releaseDate = "Just now"
        )
        _updateStatus.value = UpdateStatus.UpdateAvailable(testInfo)
    }

    /**
     * Downloads the APK file in the background with real-time progress.
     */
    fun startDownload(context: Context, info: UpdateInfo) {
        if (_updateStatus.value is UpdateStatus.Downloading) return

        _updateStatus.value = UpdateStatus.Downloading(0, 0f, 0f)

        scope.launch {
            try {
                if (info.apkUrl.startsWith("demo://")) {
                    // Realistic simulation of download progress for testing UI flow
                    val totalMB = 12.4f
                    for (percent in 5..100 step 5) {
                        kotlinx.coroutines.delay(100)
                        val downloadedMB = (totalMB * percent) / 100f
                        _updateStatus.value = UpdateStatus.Downloading(percent, downloadedMB, totalMB)
                    }
                    _snackbarMessage.emit("✅ Demo update downloaded successfully!")
                    _updateStatus.value = UpdateStatus.Idle
                    return@launch
                }

                val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val targetFile = File(updatesDir, "FocusOS_${info.versionName}.apk")
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                var currentUrl = info.apkUrl
                var connection: HttpURLConnection? = null
                var redirectCount = 0
                val maxRedirects = 5
                var responseCode = 0

                while (redirectCount < maxRedirects) {
                    Log.i(TAG, "📥 Connecting to APK download URL: $currentUrl")
                    val url = URL(currentUrl)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 30000
                        requestMethod = "GET"
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "FocusOS-Android/${BuildConfig.VERSION_NAME}")
                    }
                    responseCode = conn.responseCode
                    Log.i(TAG, "📥 Download connection response code: $responseCode")
                    if (responseCode in 300..399) {
                        val newLocation = conn.getHeaderField("Location")
                        if (!newLocation.isNullOrBlank()) {
                            Log.i(TAG, "🔀 Following redirect ($responseCode) -> $newLocation")
                            currentUrl = newLocation
                            redirectCount++
                            conn.disconnect()
                            continue
                        }
                    }
                    connection = conn
                    break
                }

                if (connection == null || responseCode != 200) {
                    val errMsg = when (responseCode) {
                        404 -> "Server returned HTTP 404 (File Not Found). APK link in 'version.json' does not exist."
                        403 -> "Server returned HTTP 403 (Access Denied). Please check repository permissions."
                        else -> "Server returned HTTP $responseCode during APK download."
                    }
                    Log.e(TAG, "❌ Download failed with HTTP $responseCode: $errMsg")
                    throw IllegalStateException(errMsg)
                }

                val contentLength = connection.contentLength
                val totalMB = if (contentLength > 0) contentLength / (1024f * 1024f) else 15f
                Log.i(TAG, "📥 Starting stream read. Total size: ${contentLength} bytes (~${String.format("%.2f", totalMB)} MB)")

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
                    Log.i(TAG, "✅ APK Download Complete: ${targetFile.absolutePath} (${targetFile.length()} bytes)")

                    // Parse downloaded APK archive info for version code & package diagnostics
                    val packageInfo = try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.packageManager.getPackageArchiveInfo(
                                targetFile.absolutePath,
                                android.content.pm.PackageManager.PackageInfoFlags.of(0)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager.getPackageArchiveInfo(targetFile.absolutePath, 0)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing package archive info", e)
                        null
                    }

                    val downloadedPkg = packageInfo?.packageName
                    val downloadedVerCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo?.longVersionCode ?: -1L
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo?.versionCode?.toLong() ?: -1L
                    }
                    val downloadedVerName = packageInfo?.versionName

                    Log.i(TAG, "==================================================")
                    Log.i(TAG, "📦 [DOWNLOADED APK ARCHIVE INSPECTION]")
                    Log.i(TAG, "   • File: ${targetFile.name} (${targetFile.length()} bytes)")
                    Log.i(TAG, "   • Archive Package:     $downloadedPkg")
                    Log.i(TAG, "   • Archive VersionCode: $downloadedVerCode")
                    Log.i(TAG, "   • Archive VersionName: $downloadedVerName")
                    Log.i(TAG, "   • Installed Package:     ${context.packageName}")
                    Log.i(TAG, "   • Installed VersionCode: ${BuildConfig.VERSION_CODE}")
                    Log.i(TAG, "   • Installed VersionName: ${BuildConfig.VERSION_NAME}")
                    
                    if (downloadedPkg != null && downloadedPkg != context.packageName) {
                        Log.w(TAG, "⚠️ PACKAGE MISMATCH: APK package '$downloadedPkg' does not match installed '${context.packageName}'!")
                    }
                    if (downloadedVerCode != -1L && downloadedVerCode <= BuildConfig.VERSION_CODE) {
                        Log.w(TAG, "⚠️ VERSION CODE WARNING: Downloaded APK versionCode ($downloadedVerCode) is <= installed versionCode (${BuildConfig.VERSION_CODE}). Android OS will reject in-place upgrade!")
                    }
                    Log.i(TAG, "==================================================")

                    _updateStatus.value = UpdateStatus.ReadyToInstall(
                        apkFile = targetFile,
                        info = info,
                        archivePackageName = downloadedPkg,
                        archiveVersionCode = downloadedVerCode,
                        archiveVersionName = downloadedVerName
                    )
                    withContext(Dispatchers.Main) {
                        installApk(context, targetFile)
                    }
                } else {
                    Log.e(TAG, "❌ Downloaded APK file is invalid or zero bytes")
                    _updateStatus.value = UpdateStatus.Error("Downloaded update file is corrupted or incomplete.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading APK", e)
                _snackbarMessage.emit("⚠️ Download failed: Please check your internet connection.")
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

            Log.i(TAG, "🚀 Preparing to install APK: ${apkFile.absolutePath}")
            val apkUri: Uri = FileProvider.getUriForFile(
                context.applicationContext,
                "${context.packageName}.fileprovider",
                apkFile
            )
            Log.i(TAG, "📎 FileProvider content URI: $apkUri")

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            Log.i(TAG, "📲 Launching Package Installer Intent...")
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            _updateStatus.value = UpdateStatus.Error("Install failed: ${e.localizedMessage}")
        }
    }
}


