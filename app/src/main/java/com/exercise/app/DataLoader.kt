package com.exercise.app

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

private const val TAG = "DataLoader"
private const val ASSET_NAME = "exercises.json"

/** 折叠 JSON 里混入的换行/连续空白，避免步骤文本在卡片里出现诡异断行 */
private val WHITESPACE = Regex("\\s+")

/**
 * 资源数据解析入口。
 *
 * - 通过 DTO + 流式读取解析，避免 Gson 把 null 直接注入到非空 Kotlin 字段（那会在后续使用处崩溃）；
 * - 单条记录异常只丢弃该条，不拖垮整个列表；
 * - 解析失败以 [Result.failure] 返回，由 UI 呈现错误与重试，而不是静默显示空列表。
 */
suspend fun loadExercisesFromAssets(context: Context): Result<List<Exercise>> =
    withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(ASSET_NAME).use { stream ->
                JsonReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    val parsed = Gson()
                        .getAdapter(Array<ExerciseDto>::class.java)
                        .read(reader)
                        ?: throw IllegalStateException("$ASSET_NAME 内容为空")

                    val seenIds = HashSet<String>(parsed.size)
                    parsed.asSequence()
                        .mapNotNull { it.toDomainOrNull() }
                        .filter { seenIds.add(it.id) }
                        .toList()
                }
            }
        }.onFailure { Log.e(TAG, "解析 $ASSET_NAME 失败", it) }
    }

/** 与 assets/exercises.json 一一对应的传输对象；字段全部可空，校验后才会转成领域模型 */
private data class ExerciseDto(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    @SerializedName("body_part") val bodyPart: String? = null,
    val equipment: String? = null,
    val instructions: Map<String, String?>? = null,
    @SerializedName("instruction_steps")
    val instructionSteps: Map<String, List<String?>?>? = null,
    @SerializedName("muscle_group") val muscleGroup: String? = null,
    @SerializedName("secondary_muscles") val secondaryMuscles: List<String?>? = null,
    val target: String? = null,
    val image: String? = null,
    @SerializedName("gif_url") val gifUrl: String? = null,
    @SerializedName("media_id") val mediaId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val attribution: String? = null
)

private fun ExerciseDto.toDomainOrNull(): Exercise? {
    val safeId = id.clean()
    val safeName = name.clean()
    if (safeId.isEmpty() || safeName.isEmpty()) {
        Log.w(TAG, "跳过缺少 id/name 的记录: id=$id name=$name")
        return null
    }

    val safeEquipment = equipment.clean()
    if (safeEquipment.isNotEmpty() && safeEquipment !in ExerciseData.allowedEquipment) {
        return null
    }

    val safeCategory = category.clean()
    return Exercise(
        id = safeId,
        name = safeName,
        category = safeCategory.ifEmpty { "其他" },
        bodyPart = bodyPart.clean().ifEmpty { safeCategory },
        equipment = safeEquipment,
        instructions = instructions.orEmpty()
            .mapNotNull { (key, value) -> value?.let { key to it.normalize() } }
            .toMap(),
        instructionSteps = instructionSteps.orEmpty()
            .mapValues { (_, steps) ->
                steps.orEmpty()
                    .map { it.clean().collapseWhitespace() }
                    .filter { it.isNotEmpty() }
            },
        muscleGroup = muscleGroup.clean(),
        secondaryMuscles = secondaryMuscles.orEmpty()
            .map { it.clean() }
            .filter { it.isNotEmpty() },
        target = target.clean(),
        image = image.clean(),
        gifUrl = gifUrl.clean(),
        mediaId = mediaId.clean(),
        createdAt = createdAt.clean(),
        attribution = attribution.clean().ifEmpty { null }
    )
}

private fun String?.clean(): String = this?.trim().orEmpty()

/** 去首尾空白并把内部换行/连续空白压成单个空格 */
private fun String.normalize(): String = collapseWhitespace()

private fun String.collapseWhitespace(): String =
    if (isEmpty()) this else replace(WHITESPACE, " ").trim()
