package com.exercise.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("exercise_prefs", Context.MODE_PRIVATE)

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("all")
    val showFavoritesOnly = MutableStateFlow(false)

    private val _todayWorkout = MutableStateFlow<List<Exercise>?>(null)
    val todayWorkout: StateFlow<List<Exercise>?> = _todayWorkout.asStateFlow()

    val filteredExercises: StateFlow<List<Exercise>> = combine(
        _exercises, searchQuery, selectedCategory, showFavoritesOnly, _favorites
    ) { list, query, category, favOnly, favs ->
        val categoryName = ExerciseData.getCategoryDisplayName(category)
        list.filter { ex ->
            val matchesCategory = category == "all" || ex.category == categoryName
            val matchesFav = !favOnly || ex.id in favs
            val matchesSearch = query.isBlank() ||
                ex.name.contains(query, ignoreCase = true) ||
                ex.muscleGroup.contains(query, ignoreCase = true) ||
                ex.target.contains(query, ignoreCase = true) ||
                ex.bodyPart.contains(query, ignoreCase = true) ||
                ExerciseData.getEquipmentDisplayName(ex.equipment).contains(query, ignoreCase = true)
            matchesCategory && matchesFav && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _exercises.value = loadExercisesFromAssets(app)
            _isLoading.value = false
        }
    }

    fun toggleFavorite(id: String) {
        val updated = _favorites.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        _favorites.value = updated
        prefs.edit().putStringSet("favorites", updated).apply()
    }

    fun randomExercise(): Exercise? = _exercises.value.randomOrNull()

    fun generateTodayWorkout(count: Int = 6) {
        val all = _exercises.value
        if (all.isEmpty()) {
            _todayWorkout.value = emptyList()
            return
        }
        val queues = all.groupBy { it.category }
            .values
            .map { ArrayDeque(it.shuffled()) }
        val picked = mutableListOf<Exercise>()
        var idx = 0
        while (picked.size < count) {
            val nonEmpty = queues.filter { it.isNotEmpty() }
            if (nonEmpty.isEmpty()) break
            picked.add(nonEmpty[idx % nonEmpty.size].removeFirst())
            idx++
        }
        _todayWorkout.value = picked
    }

    fun clearTodayWorkout() {
        _todayWorkout.value = null
    }

    private fun loadFavorites(): Set<String> =
        prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
}