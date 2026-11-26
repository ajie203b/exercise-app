package com.exercise.app

/**
 * 练习领域模型。
 *
 * 该类型只承载「已校验」的数据：所有字段均为非空，缺失值在 [loadExercisesFromAssets]
 * 的边界处就被替换为默认值，因此 UI 层无需再处理 null。
 */
data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val bodyPart: String,
    val equipment: String,
    val instructions: Map<String, String>,
    val instructionSteps: Map<String, List<String>>,
    val muscleGroup: String,
    val secondaryMuscles: List<String>,
    val target: String,
    val image: String,
    val gifUrl: String,
    val mediaId: String,
    val createdAt: String,
    val attribution: String?
) {
    /** 演示动画文件名，优先取 gif_url，缺失时回退到 image，最后回退到 media_id */
    val gifFileName: String by lazy {
        listOf(gifUrl, image)
            .asSequence()
            .map { it.substringAfterLast('/').trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: "$mediaId.gif"
    }

    /**
     * 预拼接的小写检索串。
     * 列表筛选在每次输入时都会执行，提前拼好可避免重复的字符串拼接与大小写转换。
     * 检索字段与原实现保持一致：名称、肌肉群、目标、部位、器材。
     */
    val searchText: String by lazy {
        "$name $muscleGroup $target $bodyPart ${ExerciseData.equipmentDisplayName(equipment)}"
            .lowercase()
    }

    /** 训练步骤：优先中文，其次英文 */
    fun steps(): List<String> =
        instructionSteps["zh"] ?: instructionSteps["en"] ?: emptyList()
}

data class ExerciseCategory(
    val name: String,
    val displayName: String,
    val icon: String
)

object ExerciseData {

    const val ALL_CATEGORY = "all"

    val categories: List<ExerciseCategory> = listOf(
        ExerciseCategory(ALL_CATEGORY, "全部", "🏋️"),
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

    /**
     * 数据集中的 category / equipment 已本地化为中文，因此这里用显示名做索引，
     * 同时保留英文别名以兼容上游英文数据集。
     */
    private val categoryDisplayNames: Map<String, String> =
        categories.associate { it.name to it.displayName }

    private val equipmentDisplayNames: Map<String, String> = mapOf(
        "body weight" to "徒手",
        "roller" to "健腹轮",
        "wheel roller" to "健腹轮",
        "rope" to "跳绳"
    )

    /** 允许出现在列表中的器材；中英文两种写法都接受，避免数据源切换时整库被过滤为空 */
    val allowedEquipment: Set<String> = buildSet {
        addAll(equipmentDisplayNames.keys)
        addAll(equipmentDisplayNames.values)
    }

    fun categoryDisplayName(category: String): String =
        categoryDisplayNames[category] ?: category

    fun equipmentDisplayName(equipment: String): String =
        equipmentDisplayNames[equipment] ?: equipment
}
