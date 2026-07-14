package com.exercise.app

import com.google.gson.annotations.SerializedName

data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    @SerializedName("body_part")
    val bodyPart: String,
    val equipment: String,
    val instructions: Map<String, String>,
    @SerializedName("instruction_steps")
    val instructionSteps: Map<String, List<String>>,
    @SerializedName("muscle_group")
    val muscleGroup: String,
    @SerializedName("secondary_muscles")
    val secondaryMuscles: List<String>,
    val target: String,
    val image: String,
    @SerializedName("gif_url")
    val gifUrl: String,
    @SerializedName("media_id")
    val mediaId: String,
    @SerializedName("created_at")
    val createdAt: String,
    val attribution: String?
) {
    val gifFileName: String
        get() = gifUrl.substringAfterLast("/")
}

data class ExerciseCategory(
    val name: String,
    val displayName: String,
    val icon: String
)

object ExerciseData {
    val categories = listOf(
        ExerciseCategory("all", "全部", "🏋️"),
        ExerciseCategory("waist", "腰部", "💪"),
        ExerciseCategory("back", "背部", "🔙"),
        ExerciseCategory("chest", "胸部", "🫁"),
        ExerciseCategory("upper legs", "大腿", "🦵"),
        ExerciseCategory("upper arms", "上臂", "💪"),
        ExerciseCategory("cardio", "有氧", "❤️"),
        ExerciseCategory("lower legs", "小腿", "🦵"),
        ExerciseCategory("shoulders", "肩膀", "🏋️"),
        ExerciseCategory("lower arms", "前臂", "💪"),
        ExerciseCategory("neck", "颈部", "🧘")
    )

    fun getEquipmentDisplayName(equipment: String): String {
        return when (equipment) {
            "body weight" -> "徒手"
            "roller" -> "健腹轮"
            "wheel roller" -> "健腹轮"
            "rope" -> "跳绳"
            else -> equipment
        }
    }

    fun getCategoryDisplayName(category: String): String {
        return categories.find { it.name == category }?.displayName ?: category
    }
}
