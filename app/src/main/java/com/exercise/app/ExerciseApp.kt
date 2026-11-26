package com.exercise.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 应用外层壳：只负责导航（列表 / 详情）、今日训练弹层与更新流程。
 * 具体列表渲染下沉到 [ExerciseListScreen]，更新交互下沉到 [UpdateDialogHost]。
 */
@Composable
fun ExerciseApp(
    viewModel: ExerciseViewModel = viewModel(),
    updateViewModel: UpdateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val todayWorkout by viewModel.todayWorkout.collectAsStateWithLifecycle()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 列表滚动位置提到这一层：进入详情再返回时不会跳回顶部
    val listState = rememberLazyListState()

    // 只保存 id：旋转屏幕或进程回收后可还原，也避免持有整份数据的快照
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedExercise = remember(allExercises, selectedId) {
        selectedId?.let { id -> allExercises.firstOrNull { it.id == id } }
    }

    // 数据加载完成后若仍找不到目标（例如数据版本变化），清空选中避免停在空白页
    LaunchedEffect(selectedId, allExercises, uiState.isLoading) {
        val id = selectedId ?: return@LaunchedEffect
        if (!uiState.isLoading && allExercises.isNotEmpty() && allExercises.none { it.id == id }) {
            selectedId = null
        }
    }

    LaunchedEffect(updateViewModel) {
        updateViewModel.events.collect { event ->
            when (event) {
                is UpdateEvent.Message ->
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(enabled = selectedExercise != null) { selectedId = null }

    UpdateDialogHost(
        state = updateState,
        onDownload = updateViewModel::download,
        onCancelDownload = updateViewModel::cancelDownload,
        onInstall = updateViewModel::install,
        onDismiss = updateViewModel::dismiss
    )

    AnimatedContent(
        targetState = selectedExercise,
        transitionSpec = {
            if (targetState != null) {
                // 进入详情：新页从右侧轻推入，旧页向左退让
                (slideInHorizontally(tween(280)) { full -> full / 5 } + fadeIn(tween(220)))
                    .togetherWith(
                        slideOutHorizontally(tween(280)) { full -> -full / 8 } + fadeOut(tween(160))
                    )
            } else {
                (slideInHorizontally(tween(280)) { full -> -full / 8 } + fadeIn(tween(220)))
                    .togetherWith(
                        slideOutHorizontally(tween(280)) { full -> full / 5 } + fadeOut(tween(160))
                    )
            }
        },
        label = "screen"
    ) { target ->
        if (target != null) {
            ExerciseDetailScreen(
                exercise = target,
                isFavorite = target.id in uiState.favorites,
                onToggleFavorite = { viewModel.toggleFavorite(target.id) },
                onBack = { selectedId = null }
            )
        } else {
            ExerciseListScreen(
                state = uiState,
                query = searchQuery,
                listState = listState,
                onQueryChange = viewModel::setSearchQuery,
                onCategorySelected = viewModel::selectCategory,
                onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
                onClearFilters = viewModel::clearFilters,
                onToggleFavorite = viewModel::toggleFavorite,
                onExerciseClick = { exercise -> selectedId = exercise.id },
                onRandomClick = { viewModel.randomExercise()?.let { selectedId = it.id } },
                onTodayWorkoutClick = { viewModel.showTodayWorkout() },
                onCheckUpdateClick = updateViewModel::checkForUpdate,
                onRetry = viewModel::reload
            )
        }
    }

    val workout = todayWorkout
    if (workout != null) {
        TodayWorkoutSheet(
            workout = workout,
            favorites = uiState.favorites,
            onToggleFavorite = viewModel::toggleFavorite,
            onRegenerate = { viewModel.generateTodayWorkout() },
            onExerciseClick = { exercise ->
                selectedId = exercise.id
                viewModel.clearTodayWorkout()
            },
            onDismiss = { viewModel.clearTodayWorkout() }
        )
    }
}
