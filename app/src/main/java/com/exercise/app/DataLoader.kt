package com.exercise.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadExercisesFromAssets(context: Context): List<Exercise> {
    return try {
        val jsonString = context.assets.open("exercises.json")
            .bufferedReader()
            .use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<Exercise>>() {}.type
        val allExercises: List<Exercise> = gson.fromJson(jsonString, type)

        val equipmentFilter = setOf(
            "body weight", "roller", "wheel roller", "rope",
            "徒手", "健腹轮", "跳绳"
        )
        allExercises.filter { it.equipment in equipmentFilter }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
