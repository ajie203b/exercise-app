package com.exercise.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseApp(
    exercises: List<Exercise>,
    updateManager: UpdateManager? = null
) {
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    // 更新相关状态
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showCheckingDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showDownloadCompleteDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    val context = LocalContext.current

    BackHandler(enabled = selectedExercise != null) {
        selectedExercise = null
    }

    // 设置更新回调
    LaunchedEffect(updateManager) {
        updateManager?.onUpdateChecked = { info ->
            showCheckingDialog = false
            if (info != null) {
                // 有新版本：显示更新信息对话框
                updateInfo = info
                showUpdateDialog = true
            } else {
                // 没有新版本：显示提示（不返回主页面）
                Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show()
            }
        }

        updateManager?.onDownloadProgress = { progress ->
            downloadProgress = progress
        }

        updateManager?.onDownloadComplete = { file ->
            showDownloadDialog = false
            downloadedFile = file
            showDownloadCompleteDialog = true
        }

        updateManager?.onError = { error ->
            showCheckingDialog = false
            showDownloadDialog = false
            showUpdateDialog = false
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    val filteredExercises = exercises.filter { exercise ->
        val matchesCategory = selectedCategory == "all" ||
                exercise.category == selectedCategory ||
                exercise.category == ExerciseData.getCategoryDisplayName(selectedCategory)
        val matchesSearch = searchQuery.isEmpty() ||
                exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                exercise.target.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // 显示对话框
    if (showCheckingDialog) {
        CheckingUpdateDialog()
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            versionName = updateInfo!!.versionName,
            description = updateInfo!!.description,
            apkSize = updateInfo!!.apkSize,
            onConfirm = {
                // 点击"立即更新"后自动开始下载
                showUpdateDialog = false
                showDownloadDialog = true
                downloadProgress = 0f
                updateManager?.startDownload(updateInfo!!)
            },
            onDismiss = {
                showUpdateDialog = false
                updateInfo = null
            }
        )
    }

    if (showDownloadDialog) {
        DownloadProgressDialog(
            progress = downloadProgress,
            onDismiss = {
                showDownloadDialog = false
                updateManager?.cancelDownload()
            }
        )
    }

    if (showDownloadCompleteDialog && downloadedFile != null) {
        DownloadCompleteDialog(
            onInstall = {
                showDownloadCompleteDialog = false
                updateManager?.installApk(downloadedFile!!)
            },
            onDismiss = {
                showDownloadCompleteDialog = false
            }
        )
    }

    if (selectedExercise != null) {
        ExerciseDetailScreen(
            exercise = selectedExercise!!,
            onBack = { selectedExercise = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("健身训练") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(onClick = {
                            showCheckingDialog = true
                            updateManager?.checkUpdate()
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "检查更新"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索练习...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                    singleLine = true
                )

                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(ExerciseData.categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name },
                            label = { Text("${category.icon} ${category.displayName}") }
                        )
                    }
                }

                Text(
                    text = "共 ${filteredExercises.size} 个练习",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { selectedExercise = exercise }
                        )
                    }
                }
            }
        }
    }
}
