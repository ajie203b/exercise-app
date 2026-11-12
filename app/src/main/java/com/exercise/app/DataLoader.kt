package com.exercise.app

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val gson = Gson()

private val allowedEquipment = setOf(
    "body weight", "roller", "wheel roller", "rope",
    "徒手", "健腹轮", "跳绳"
)

suspend fun loadExercisesFromAssets(context: Context): List<Exercise> = withContext(Dispatchers.IO) {
    try {
        val jsonString = context.assets.open("exercises.json")
            .bufferedReader()
            .use { it.readText() }
        val allExercises: Array<Exercise> = gson.fromJson(jsonString, Array<Exercise>::class.java)
        allExercises.filter { it.equipment in allowedEquipment }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}