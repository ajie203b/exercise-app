package com.exercise.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 更新相关对话框的统一入口。
 *
 * 由 [UpdateState] 单向驱动 UI，避免多个布尔量组合出「同时弹出两个对话框」之类的非法状态。
 */
@Composable
fun UpdateDialogHost(
    state: UpdateState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        UpdateState.Idle -> Unit
        UpdateState.Checking -> CheckingUpdateDialog()
        is UpdateState.Available -> UpdateDialog(
            versionName = state.info.versionName,
            description = state.info.description,
            apkSize = state.info.apkSize,
            onConfirm = onDownload,
            onDismiss = onDismiss
        )
        is UpdateState.Downloading -> DownloadProgressDialog(
            progress = state.progress,
            onCancel = onCancelDownload
        )
        is UpdateState.Downloaded -> DownloadCompleteDialog(
            file = state.file,
            onInstall = onInstall,
            onDismiss = onDismiss
        )
    }
}

/** 发现新版本对话框 */
@Composable
fun UpdateDialog(
    versionName: String,
    description: String,
    apkSize: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "发现新版本 v$versionName",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                if (apkSize.isNotEmpty()) {
                    Text(
                        text = "大小: $apkSize",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "更新内容:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description.ifBlank { "问题修复与体验优化" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("立即更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后更新")
            }
        }
    )
}

/** 下载进度对话框：下载中禁止点击外部关闭，只允许显式取消 */
@Composable
fun DownloadProgressDialog(
    progress: Float,
    onCancel: () -> Unit
) {
    val percent = (progress.coerceIn(0f, 1f) * 100).roundToInt()
    AlertDialog(
        onDismissRequest = { /* 下载中禁止点击外部关闭 */ },
        title = {
            Text(
                text = "正在下载更新…",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "$percent%",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("取消下载")
            }
        }
    )
}

/** 下载完成对话框 - 提示用户安装 */
@Composable
fun DownloadCompleteDialog(
    file: File,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "下载完成",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "更新包已下载完成，点击下方按钮进行安装。",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "大小: ${formatSize(file.length())}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("立即安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后安装")
            }
        }
    )
}

/** 检查更新中对话框 */
@Composable
fun CheckingUpdateDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "检查更新中…",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "请稍候",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { }
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes <= 0L -> "未知"
    bytes >= 1024 * 1024 -> String.format(Locale.ROOT, "%.1f MB", bytes / 1024.0 / 1024.0)
    else -> String.format(Locale.ROOT, "%d KB", bytes / 1024)
}
