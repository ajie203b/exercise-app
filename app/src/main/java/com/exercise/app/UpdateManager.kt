package com.exercise.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.azhon.appupdate.manager.DownloadManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL

/**
 * 应用更新管理器
 * 支持从 GitHub Releases 检查版本并自动下载更新
 */
class UpdateManager(private val activity: Activity) {

    companion object {
        // 当前应用版本号
        private var currentVersionCode: Int = 1
        private var currentVersionName: String = "1.0"
    }

    private var downloadManager: DownloadManager? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 更新状态回调
    var onUpdateChecked: ((UpdateInfo?) -> Unit)? = null
    var onDownloadProgress: ((Float) -> Unit)? = null
    var onDownloadComplete: ((File) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    /**
     * 初始化更新管理器
     */
    fun init() {
        currentVersionCode = getVersionCode()
        currentVersionName = getVersionName()
    }

    /**
     * 检查更新
     * @param silent 是否静默检查（不显示提示）
     */
    fun checkUpdate(silent: Boolean = false) {
        scope.launch {
            try {
                val updateInfo = fetchUpdateInfo()

                if (updateInfo != null && updateInfo.versionCode > currentVersionCode) {
                    onUpdateChecked?.invoke(updateInfo)
                } else if (!silent) {
                    // 没有新版本
                    onUpdateChecked?.invoke(null)
                }
            } catch (e: Exception) {
                if (!silent) {
                    onError?.invoke("检查更新失败: ${e.message}")
                }
                onUpdateChecked?.invoke(null)
            }
        }
    }

    /**
     * 从 GitHub 获取更新信息
     */
    private suspend fun fetchUpdateInfo(): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val url = UpdateConfig.VERSION_CHECK_URL
                val response = URL(url).readText()
                val json = JSONObject(response)

                UpdateInfo(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    apkUrl = UpdateConfig.APK_DOWNLOAD_URL,
                    description = json.getString("description"),
                    apkSize = json.optString("apkSize", "")
                )
            } catch (e: Exception) {
                // 如果获取失败，可能是网络问题或文件不存在
                null
            }
        }
    }

    /**
     * 执行更新下载
     */
    fun startDownload(updateInfo: UpdateInfo) {
        downloadManager = DownloadManager.Builder(activity).run {
            apkUrl(updateInfo.apkUrl)
            apkName("exercise_app_v${updateInfo.versionName}.apk")
            smallIcon(R.mipmap.ic_launcher)
            apkVersionCode(updateInfo.versionCode)
            apkVersionName(updateInfo.versionName)
            apkSize(updateInfo.apkSize)
            apkDescription(updateInfo.description)
            build()
        }
        downloadManager?.download()
    }

    /**
     * 安装APK文件
     */
    fun installApk(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(
                        activity,
                        "${activity.packageName}.fileprovider",
                        file
                    ),
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            onError?.invoke("安装失败: ${e.message}")
        }
    }

    /**
     * 检查存储权限（Android 11+）
     */
    fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * 请求存储权限（Android 11+）
     */
    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }

    /**
     * 取消下载
     */
    fun cancelDownload() {
        downloadManager?.cancel()
    }

    /**
     * 获取当前版本号
     */
    private fun getVersionCode(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.packageManager.getPackageInfo(activity.packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * 获取当前版本名称
     */
    private fun getVersionName(): String {
        return try {
            activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    /**
     * 清理资源
     */
    fun destroy() {
        scope.cancel()
        downloadManager?.cancel()
    }
}

/**
 * 更新信息数据类
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val description: String,
    val apkSize: String
)
