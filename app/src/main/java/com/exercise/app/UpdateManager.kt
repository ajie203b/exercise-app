package com.exercise.app

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private const val TAG = "UpdateManager"
private const val TIMEOUT_MS = 10_000
private const val MAX_MANIFEST_CHARS = 64 * 1024
private const val POLL_INTERVAL_MS = 400L
private const val APK_MIME = "application/vnd.android.package-archive"
private const val APK_PREFIX = "exercise_app_v"
private const val APK_SUFFIX = ".apk"

/** 更新流程的互斥状态，同一时刻只会有一个对话框生效 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val progress: Float) : UpdateState
    data class Downloaded(val file: File) : UpdateState
}

/** 一次性提示（Toast），与状态区分开以避免对话框被「已是最新版本」之类的事件卡住 */
sealed interface UpdateEvent {
    data class Message(val text: String) : UpdateEvent
}

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val description: String,
    val apkSize: String,
    val sha256: String? = null
)

private class UpdateCheckException(message: String) : IOException(message)

/**
 * 应用更新管理器。
 *
 * 设计要点：
 * 1. 单一状态源（[state]）+ 事件通道（[events]），UI 不再维护 8 个互相牵制的布尔量；
 * 2. 下载进度由轮询 DownloadManager 得到，不再注册动态广播接收器 —— 少一条需要注销的注册，
 *    也避免 Activity 重建时接收器泄漏或重复触发；
 * 3. [release] 只解绑协程，不再删除正在下载的任务（旧实现会在旋转屏幕时取消下载）；
 * 4. 下载完成后可选做 SHA-256 校验，安装前检查「未知来源」权限并给出引导。
 */
class UpdateManager(
    context: Context,
    private val scope: CoroutineScope
) {

    private val appContext: Context = context.applicationContext

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val _events = Channel<UpdateEvent>(Channel.BUFFERED)
    val events: Flow<UpdateEvent> = _events.receiveAsFlow()

    private val downloadManager: DownloadManager? by lazy {
        appContext.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    }

    private var downloadId: Long = -1L
    private var downloadJob: Job? = null
    private var pendingUpdate: UpdateInfo? = null
    private var downloadedFile: File? = null

    /** 检查更新；网络失败与「已是最新」会给出不同提示（旧实现把两者混为一谈） */
    fun checkForUpdate() {
        val current = _state.value
        if (current is UpdateState.Checking || current is UpdateState.Downloading) return

        _state.value = UpdateState.Checking
        scope.launch {
            runCatching { fetchUpdateInfo() }
                .onSuccess { info ->
                    if (info.versionCode.toLong() > currentVersionCode()) {
                        val cached = reusedDownload(info)
                        if (cached != null) {
                            // 用户上次选了「稍后安装」：直接复用已校验的安装包，避免重复下载 30MB+
                            _state.value = UpdateState.Downloaded(cached)
                        } else {
                            pendingUpdate = info
                            _state.value = UpdateState.Available(info)
                        }
                    } else {
                        _state.value = UpdateState.Idle
                        emit("当前已是最新版本")
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "检查更新失败", error)
                    _state.value = UpdateState.Idle
                    emit(
                        when (error) {
                            is UpdateCheckException -> error.message ?: "检查更新失败"
                            else -> "检查更新失败，请检查网络后重试"
                        }
                    )
                }
        }
    }

    /** 开始下载（下载源固定为 [UpdateConfig.APK_DOWNLOAD_URL]，不接受远程清单指定的地址） */
    fun download() {
        val info = pendingUpdate ?: return
        if (_state.value is UpdateState.Downloading) return

        val manager = downloadManager
        if (manager == null) {
            emit("当前设备不支持应用内下载")
            _state.value = UpdateState.Idle
            return
        }
        if (!isTrustedUrl(info.apkUrl)) {
            emit("下载地址不可信，已取消更新")
            _state.value = UpdateState.Idle
            return
        }

        val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (dir == null) {
            emit("无法访问下载目录")
            _state.value = UpdateState.Idle
            return
        }

        val fileName = apkFileName(info)
        cleanupStaleApks(dir, keep = fileName)
        val target = File(dir, fileName)
        if (target.exists() && !target.delete()) {
            Log.w(TAG, "旧安装包删除失败: ${target.absolutePath}")
        }

        val request = DownloadManager.Request(Uri.parse(info.apkUrl)).apply {
            setTitle("正在下载健身训练 v${info.versionName}")
            setDescription("下载中…")
            setMimeType(APK_MIME)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val id = runCatching { manager.enqueue(request) }.getOrElse { error ->
            Log.e(TAG, "下载启动失败", error)
            emit("下载启动失败，请稍后重试")
            _state.value = UpdateState.Idle
            return
        }

        downloadId = id
        _state.value = UpdateState.Downloading(0f)
        observeDownload(id, info, target)
    }

    /** 用户主动取消：只有这里会真正移除 DownloadManager 任务 */
    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null

        if (downloadId != -1L) {
            runCatching { downloadManager?.remove(downloadId) }
                .onFailure { Log.w(TAG, "取消下载失败", it) }
        }
        downloadId = -1L
        downloadedFile = null
        _state.value = UpdateState.Idle
    }

    /** 关闭对话框但不取消下载（例如「稍后更新」/「稍后安装」） */
    fun dismiss() {
        if (_state.value !is UpdateState.Downloading) {
            _state.value = UpdateState.Idle
        }
    }

    /** 安装已下载的 APK；缺少「安装未知应用」权限时跳转授权页并提示 */
    fun installApk() {
        val file = downloadedFile
        if (file == null || !file.exists() || file.length() == 0L) {
            emit("安装包不存在或已损坏，请重新下载")
            _state.value = UpdateState.Idle
            return
        }

        if (!appContext.packageManager.canRequestPackageInstalls()) {
            emit("请允许「安装未知应用」后重试")
            runCatching {
                appContext.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${appContext.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                Log.w(TAG, "无法打开未知来源设置页", it)
                emit("请到系统设置中允许安装未知来源应用")
            }
            return
        }

        runCatching {
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
            appContext.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, APK_MIME)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            // 安装器已拉起，收起对话框；安装包路径保留，用户取消后可再次触发
            _state.value = UpdateState.Idle
        }.onFailure { error ->
            Log.e(TAG, "启动安装失败", error)
            emit("安装失败，请稍后重试")
        }
    }

    /** 与宿主生命周期解绑：只取消协程，不影响系统下载任务 */
    fun release() {
        downloadJob?.cancel()
        downloadJob = null
    }

    private fun observeDownload(id: Long, info: UpdateInfo, target: File) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            try {
                while (isActive) {
                    val snapshot = queryDownload(id)
                    if (snapshot == null) {
                        failWith("下载任务已丢失，请重新下载")
                        return@launch
                    }
                    when (snapshot.status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            finishDownload(info, target)
                            return@launch
                        }
                        DownloadManager.STATUS_FAILED -> {
                            failWith("下载失败（错误码 ${snapshot.reason}），请重试")
                            return@launch
                        }
                        else -> {
                            if (snapshot.totalBytes > 0) {
                                val progress =
                                    (snapshot.downloadedBytes.toFloat() / snapshot.totalBytes)
                                        .coerceIn(0f, 1f)
                                _state.value = UpdateState.Downloading(progress)
                            }
                        }
                    }
                    delay(POLL_INTERVAL_MS)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // 轮询异常不应该让流程卡在「下载中」
                Log.w(TAG, "下载进度轮询异常", error)
                failWith("下载中断，请重试")
            }
        }
    }

    private suspend fun finishDownload(info: UpdateInfo, target: File) {
        if (!target.exists() || target.length() == 0L) {
            failWith("下载文件不完整，请重试")
            return
        }

        val expected = info.sha256
        if (!expected.isNullOrBlank() && !verifySha256(target, expected)) {
            target.delete()
            failWith("安装包校验失败，已删除，请重新下载")
            return
        }

        downloadedFile = target
        _state.value = UpdateState.Downloaded(target)
    }

    private suspend fun verifySha256(file: File, expected: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
                    .equals(expected.trim(), ignoreCase = true)
            }.getOrElse {
                Log.w(TAG, "校验安装包失败", it)
                false
            }
        }

    private fun failWith(message: String) {
        _state.value = UpdateState.Idle
        emit(message)
    }

    private fun emit(message: String) {
        _events.trySend(UpdateEvent.Message(message))
    }

    private fun queryDownload(id: Long): DownloadSnapshot? {
        val cursor = downloadManager?.query(DownloadManager.Query().setFilterById(id)) ?: return null
        return cursor.use {
            if (!it.moveToFirst()) return null
            DownloadSnapshot(
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
                downloadedBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                ),
                totalBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                ),
                reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
            )
        }
    }

    private fun currentVersionCode(): Long = runCatching {
        PackageInfoCompat.getLongVersionCode(
            appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        )
    }.getOrDefault(1L)

    private fun apkFileName(info: UpdateInfo): String =
        "$APK_PREFIX${info.versionName}$APK_SUFFIX"

    /**
     * 是否已有同版本、且已完成校验的安装包可复用。
     * [downloadedFile] 只在文件存在且校验通过后才会被赋值，因此这里无需再次校验。
     */
    private fun reusedDownload(info: UpdateInfo): File? = downloadedFile
        ?.takeIf { it.exists() && it.length() > 0L && it.name == apkFileName(info) }

    private fun cleanupStaleApks(dir: File, keep: String) {
        runCatching {
            dir.listFiles { file ->
                file.isFile && file.name.startsWith(APK_PREFIX) &&
                    file.name.endsWith(APK_SUFFIX) && file.name != keep
            }?.forEach { stale ->
                if (!stale.delete()) Log.w(TAG, "旧安装包清理失败: ${stale.name}")
            }
        }
    }

    private fun isTrustedUrl(url: String): Boolean = runCatching {
        val parsed = URI(url)
        parsed.scheme.equals("https", ignoreCase = true) &&
            parsed.host?.lowercase()?.endsWith("github.com") == true
    }.getOrDefault(false)

    private suspend fun fetchUpdateInfo(): UpdateInfo = withContext(Dispatchers.IO) {
        val connection = (URL(UpdateConfig.VERSION_CHECK_URL).openConnection() as HttpURLConnection)
            .apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                requestMethod = "GET"
                useCaches = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-cache")
            }

        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw UpdateCheckException("检查更新失败（HTTP $code）")
            }

            val body = connection.inputStream
                .bufferedReader(StandardCharsets.UTF_8)
                .use { it.readBounded(MAX_MANIFEST_CHARS) }

            if (body.isBlank()) throw UpdateCheckException("版本信息为空")

            val json = JSONObject(body)
            val versionCode = json.optInt("versionCode", 0)
            val versionName = json.optString("versionName").trim()
            if (versionCode <= 0 || versionName.isEmpty()) {
                throw UpdateCheckException("版本信息格式不正确")
            }

            UpdateInfo(
                versionCode = versionCode,
                versionName = versionName,
                apkUrl = UpdateConfig.APK_DOWNLOAD_URL,
                description = json.optString("description").trim(),
                apkSize = json.optString("apkSize").trim(),
                sha256 = json.optString("sha256").trim()
                    .takeIf { it.length == SHA256_HEX_LENGTH }
                    ?.lowercase()
            )
        } finally {
            connection.disconnect()
        }
    }

    private data class DownloadSnapshot(
        val status: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val reason: Int
    )

    private companion object {
        const val SHA256_HEX_LENGTH = 64
    }
}

/** 读取最多 [limit] 个字符，避免异常响应把内存吃满 */
private fun java.io.Reader.readBounded(limit: Int): String {
    val builder = StringBuilder()
    val buffer = CharArray(4 * 1024)
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        if (builder.length + read > limit) throw IOException("响应体过大")
        builder.append(buffer, 0, read)
    }
    return builder.toString()
}
