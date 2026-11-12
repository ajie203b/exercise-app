package com.exercise.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseApp(
    updateManager: UpdateManager? = null,
    viewModel: ExerciseViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val filteredExercises by viewModel.filteredExercises.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val todayWorkout by viewModel.todayWorkout.collectAsStateWithLifecycle()

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showCheckingDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showDownloadCompleteDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    val context = LocalContext.current

    BackHandler(enabled = selectedExercise != null) { selectedExercise = null }

    LaunchedEffect(updateManager) {
        updateManager?.onUpdateChecked = { info ->
            showCheckingDialog = false
            if (info != null) {
                updateInfo = info
                showUpdateDialog = true
            } else {
                Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show()
            }
        }
        updateManager?.onDownloadProgress = { progress -> downloadProgress = progress }
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

    if (showCheckingDialog) CheckingUpdateDialog()

    val currentUpdateInfo = updateInfo
    if (showUpdateDialog && currentUpdateInfo != null) {
        UpdateDialog(
            versionName = currentUpdateInfo.versionName,
            description = currentUpdateInfo.description,
            apkSize = currentUpdateInfo.apkSize,
            onConfirm = {
                showUpdateDialog = false
                showDownloadDialog = true
                downloadProgress = 0f
                updateManager?.startDownload(currentUpdateInfo)
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

    val currentDownloadedFile = downloadedFile
    if (showDownloadCompleteDialog && currentDownloadedFile != null) {
        DownloadCompleteDialog(
            onInstall = {
                showDownloadCompleteDialog = false
                updateManager?.installApk(currentDownloadedFile)
            },
            onDismiss = { showDownloadCompleteDialog = false }
        )
    }

    val workout = todayWorkout
    if (workout != null) {
        TodayWorkoutSheet(
            workout = workout,
            favorites = favorites,
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onRegenerate = { viewModel.generateTodayWorkout() },
            onExerciseClick = {
                selectedExercise = it
                viewModel.clearTodayWorkout()
            },
            onDismiss = { viewModel.clearTodayWorkout() }
        )
    }

    val detailExercise = selectedExercise
    if (detailExercise != null) {
        ExerciseDetailScreen(
            exercise = detailExercise,
            isFavorite = detailExercise.id in favorites,
            onToggleFavorite = { viewModel.toggleFavorite(detailExercise.id) },
            onBack = { selectedExercise = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("健身训练", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.generateTodayWorkout() }) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = "今日训练")
                        }
                        IconButton(onClick = {
                            val random = viewModel.randomExercise()
                            if (random != null) selectedExercise = random
                        }) {
                            Icon(Icons.Filled.Casino, contentDescription = "随机动作")
                        }
                        IconButton(onClick = {
                            showCheckingDialog = true
                            updateManager?.checkUpdate()
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "检查更新")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索动作、部位或器材…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "清除搜索")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = showFavoritesOnly,
                            onClick = { viewModel.showFavoritesOnly.value = !showFavoritesOnly },
                            label = { Text("收藏") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    items(ExerciseData.categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category.name,
                            onClick = { viewModel.selectedCategory.value = category.name },
                            label = { Text("${category.icon} ${category.displayName}") }
                        )
                    }
                }

                Text(
                    text = "共 ${filteredExercises.size} 个练习" + if (showFavoritesOnly) " · 仅收藏" else "",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    filteredExercises.isEmpty() -> {
                        EmptyState(favoritesOnly = showFavoritesOnly)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredExercises, key = { it.id }) { exercise ->
                                ExerciseCard(
                                    exercise = exercise,
                                    isFavorite = exercise.id in favorites,
                                    onToggleFavorite = { viewModel.toggleFavorite(exercise.id) },
                                    onClick = { selectedExercise = exercise }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(favoritesOnly: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (favoritesOnly) Icons.Outlined.FavoriteBorder else Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = if (favoritesOnly) "还没有收藏的动作" else "未找到匹配的练习",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (favoritesOnly) "点击卡片上的 ♥ 即可收藏常用动作" else "试试其他关键词或切换分类",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}