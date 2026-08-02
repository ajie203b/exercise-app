package com.exercise.app

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 应用更新管理器
 * 支持从 GitHub Releases 检查版本并自动下载更新
 * 使用系统 DownloadManager，避免第三方库兼容性问题
 */
class UpdateManager(private val activity: Activity) {

    // 当前应用版本号
    private var currentVersionCode: Int = 1

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 更新状态回调
    var onUpdateChecked: ((UpdateInfo?) -> Unit)? = null
    var onDownloadProgress: ((Float) -> Unit)? = null
    var onDownloadComplete: ((File) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    // 系统下载管理器
    private val systemDownloadManager: DownloadManager by lazy {
        activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private var currentDownloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    private var downloadObserverJob: Job? = null

    /**
     * 初始化更新管理器
     */
    fun init() {
        currentVersionCode = getVersionCode()
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        scope.launch {
            try {
                val updateInfo = fetchUpdateInfo()

                if (updateInfo != null && updateInfo.versionCode > currentVersionCode) {
                    onUpdateChecked?.invoke(updateInfo)
                } else {
                    onUpdateChecked?.invoke(null)
                }
            } catch (e: Exception) {
                onError?.invoke("检查更新失败，请检查网络连接")
            }
        }
    }

    /**
     * 从 GitHub 获取更新信息（使用 HttpURLConnection 避免崩溃）
     */
    private suspend fun fetchUpdateInfo(): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(UpdateConfig.VERSION_CHECK_URL)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    return@withContext null
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                if (response.isBlank()) {
                    return@withContext null
                }

                val json = JSONObject(response)

                UpdateInfo(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    apkUrl = UpdateConfig.APK_DOWNLOAD_URL,
                    description = json.getString("description"),
                    apkSize = json.optString("apkSize", "")
                )
            } catch (e: Exception) {
                null
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * 使用系统 DownloadManager 执行下载
     */
    fun startDownload(updateInfo: UpdateInfo) {
        try {
            // 取消之前的下载
            cancelDownload()

            val fileName = "exercise_app_v${updateInfo.versionName}.apk"
            val downloadDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val targetFile = File(downloadDir, fileName)
            // 如果文件已存在则删除重新下载
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl)).apply {
                setTitle("正在下载健身训练 v${updateInfo.versionName}")
                setDescription("下载中...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            currentDownloadId = systemDownloadManager.enqueue(request)

            // 注册下载完成广播
            registerDownloadReceiver(targetFile)

            // 启动进度监听
            startDownloadObserver()

        } catch (e: Exception) {
            onError?.invoke("下载启动失败: ${e.message}")
        }
    }

    /**
     * 监听下载进度
     */
    private fun startDownloadObserver() {
        downloadObserverJob?.cancel()
        downloadObserverJob = scope.launch {
            try {
                var downloading = true
                while (downloading && currentDownloadId != -1L) {
                    val query = DownloadManager.Query().setFilterById(currentDownloadId)
                    val cursor: Cursor? = systemDownloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesDownloadedIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        if (statusIndex >= 0 && bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0) {
                            val status = cursor.getInt(statusIndex)
                            val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                            val bytesTotal = cursor.getLong(bytesTotalIndex)

                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    downloading = false
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    downloading = false
                                    onError?.invoke("下载失败，请重试")
                                }
                                DownloadManager.STATUS_RUNNING,
                                DownloadManager.STATUS_PENDING -> {
                                    if (bytesTotal > 0) {
                                        val progress = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                                        onDownloadProgress?.invoke(progress)
                                    }
                                }
                            }
                        }
                    }
                    cursor?.close()
                    delay(500)
                }
            } catch (e: Exception) {
                // 观察器异常不影响下载
            }
        }
    }

    /**
     * 注册下载完成广播接收器
     */
    private fun registerDownloadReceiver(targetFile: File) {
        try {
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                    if (id == currentDownloadId) {
                        unregisterDownloadReceiver()
                        downloadObserverJob?.cancel()
                        if (targetFile.exists() && targetFile.length() > 0) {
                            onDownloadComplete?.invoke(targetFile)
                        } else {
                            onError?.invoke("下载文件不完整，请重试")
                        }
                    }
                }
            }
            activity.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        } catch (e: Exception) {
            // 忽略注册失败
        }
    }

    /**
     * 注销下载广播接收器
     */
    private fun unregisterDownloadReceiver() {
        try {
            downloadReceiver?.let {
                activity.unregisterReceiver(it)
            }
        } catch (e: Exception) {
            // 忽略注销失败
        }
        downloadReceiver = null
    }

    /**
     * 安装 APK 文件
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
     * 取消下载
     */
    fun cancelDownload() {
        try {
            if (currentDownloadId != -1L) {
                systemDownloadManager.remove(currentDownloadId)
            }
        } catch (e: Exception) {
            // 忽略取消失败
        }
        currentDownloadId = -1
        downloadObserverJob?.cancel()
        unregisterDownloadReceiver()
    }

    /**
     * 获取当前版本号
     */
    private fun getVersionCode(): Int {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                activity.packageManager.getPackageInfo(activity.packageName, 0)
                    .longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * 清理资源
     */
    fun destroy() {
        scope.cancel()
        cancelDownload()
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
