package com.exercise.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "ExerciseViewModel"
private const val PREFS_NAME = "exercise_prefs"
private const val KEY_FAVORITES = "favorites"
private const val KEY_TODAY_WORKOUT = "today_workout"
private const val STOP_TIMEOUT_MS = 5_000L
private const val DEFAULT_WORKOUT_SIZE = 6

/**
 * 列表页完整状态快照。
 *
 * 用一个不可变对象承载筛选结果与加载态，UI 只需订阅一次即可拿到全部信息，
 * 避免多个 StateFlow 在重组时各自触发刷新。
 */
data class ExerciseUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val totalCount: Int = 0,
    val favorites: Set<String> = emptySet(),
    val selectedCategory: String = ExerciseData.ALL_CATEGORY,
    val favoritesOnly: Boolean = false
) {
    val hasData: Boolean get() = totalCount > 0
}

class ExerciseViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    /** 全量数据：供详情页按 id 还原、随机动作与今日训练使用，不受筛选影响 */
    val allExercises: StateFlow<List<Exercise>> = _allExercises.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExerciseData.ALL_CATEGORY)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    private val _todayWorkout = MutableStateFlow<List<Exercise>?>(null)
    val todayWorkout: StateFlow<List<Exercise>?> = _todayWorkout.asStateFlow()

    private var loadJob: Job? = null

    private data class Source(
        val list: List<Exercise>,
        val loading: Boolean,
        val error: String?
    )

    private data class Filters(
        val query: String,
        val category: String,
        val favoritesOnly: Boolean,
        val favorites: Set<String>
    )

    val uiState: StateFlow<ExerciseUiState> = combine(
        combine(_allExercises, _isLoading, _errorMessage) { list, loading, error ->
            Source(list, loading, error)
        },
        combine(_searchQuery, _selectedCategory, _favoritesOnly, _favorites) { query, category, onlyFav, favs ->
            Filters(query, category, onlyFav, favs)
        }
    ) { source, filters ->
        buildState(source, filters)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ExerciseUiState()
    )

    init {
        reload()
    }

    /** 重新加载资源数据；失败时保留错误信息供 UI 展示重试入口 */
    fun reload() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            loadExercisesFromAssets(getApplication())
                .onSuccess { list ->
                    _allExercises.value = list
                    if (list.isEmpty()) {
                        _errorMessage.value = "没有可用的练习数据"
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "加载练习数据失败", error)
                    _errorMessage.value = "练习数据加载失败"
                }

            _isLoading.value = false
        }
    }

    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavoritesOnly() {
        _favoritesOnly.value = !_favoritesOnly.value
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = ExerciseData.ALL_CATEGORY
        _favoritesOnly.value = false
    }

    fun toggleFavorite(id: String) {
        val updated = _favorites.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }.toSet()
        _favorites.value = updated
        // putStringSet 会保留传入集合的引用，务必传副本
        prefs.edit().putStringSet(KEY_FAVORITES, updated.toSet()).apply()
    }

    fun randomExercise(): Exercise? = _allExercises.value.randomOrNull()

    /**
     * 打开今日训练：当天已有计划则原样呈现，没有才随机生成。
     * 「换一组」走 [generateTodayWorkout]，语义上更符合"今日计划"的预期。
     */
    fun showTodayWorkout() {
        if (_todayWorkout.value != null) return
        _todayWorkout.value = restoreTodayWorkout() ?: buildRandomWorkout(DEFAULT_WORKOUT_SIZE)
    }

    /**
     * 生成今日训练：按部位轮转抽取，尽量覆盖不同部位且不重复。
     */
    fun generateTodayWorkout(count: Int = DEFAULT_WORKOUT_SIZE) {
        _todayWorkout.value = buildRandomWorkout(count)
    }

    fun clearTodayWorkout() {
        _todayWorkout.value = null
    }

    private fun buildRandomWorkout(count: Int): List<Exercise> {
        val pool = _allExercises.value
        if (pool.isEmpty()) return emptyList()

        val queues = pool.groupBy { it.category }
            .values
            .map { ArrayDeque(it.shuffled()) }
            .shuffled()

        val picked = ArrayList<Exercise>(count)
        var cursor = 0
        while (picked.size < count) {
            val remaining = queues.filter { it.isNotEmpty() }
            if (remaining.isEmpty()) break
            picked += remaining[cursor % remaining.size].removeFirst()
            cursor++
        }
        persistTodayWorkout(picked)
        return picked
    }

    /** 今日训练按「日期 + id 列表」持久化，进程被回收后仍能恢复同一套计划 */
    private fun persistTodayWorkout(workout: List<Exercise>) {
        if (workout.isEmpty()) return
        val payload = todayKey() + "|" + workout.joinToString(",") { it.id }
        prefs.edit().putString(KEY_TODAY_WORKOUT, payload).apply()
    }

    private fun restoreTodayWorkout(): List<Exercise>? {
        val raw = prefs.getString(KEY_TODAY_WORKOUT, null) ?: return null
        val separator = raw.indexOf('|')
        if (separator <= 0) return null
        // 跨天即失效，避免把昨天的计划当成今天
        if (raw.substring(0, separator) != todayKey()) return null

        val byId = _allExercises.value.associateBy { it.id }
        val restored = raw.substring(separator + 1)
            .split(',')
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { byId[it] }
            .toList()
        return restored.takeIf { it.isNotEmpty() }
    }

    private fun todayKey(): String = LocalDate.now().toString()

    private fun buildState(source: Source, filters: Filters): ExerciseUiState {
        val keyword = filters.query.trim().lowercase()
        val categoryName = ExerciseData.categoryDisplayName(filters.category)

        val visible = source.list.asSequence()
            .filter { filters.category == ExerciseData.ALL_CATEGORY || it.category == categoryName }
            .filter { !filters.favoritesOnly || it.id in filters.favorites }
            .filter { keyword.isEmpty() || it.searchText.contains(keyword) }
            .toList()

        return ExerciseUiState(
            isLoading = source.loading,
            errorMessage = source.error,
            exercises = visible,
            totalCount = source.list.size,
            favorites = filters.favorites,
            selectedCategory = filters.category,
            favoritesOnly = filters.favoritesOnly
        )
    }

    private fun loadFavorites(): Set<String> =
        prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
}
