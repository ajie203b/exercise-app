package com.exercise.app

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exercise.app.ui.components.AppFilterChip
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

private const val TICK_MS = 200L
private val DURATION_PRESETS = listOf(30, 60, 90, 120)

/**
 * 计时相关文本统一按 UTC 数字格式化，避免阿拉伯语等区域把数字显示成本地字符。
 */
private fun formatTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d", safe / 60, safe % 60)
}

/**
 * 训练辅助：倒计时 / 秒表 + 次数、组数计数。
 *
 * 计时使用 [SystemClock.elapsedRealtime] 作为时基，而不是累加 `delay(1000)`：
 * 后者在后台调度或卡顿时会持续漂移，跑几分钟就明显偏差。
 * 状态全部走 [rememberSaveable]，旋转屏幕或滑出屏幕再回来都不会归零。
 */
@Composable
fun TrainingAidSection(modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current

    var countdown by rememberSaveable { mutableStateOf(true) }
    var durationSec by rememberSaveable { mutableStateOf(60) }
    var running by rememberSaveable { mutableStateOf(false) }
    var countdownElapsedMs by rememberSaveable { mutableStateOf(0L) }
    var stopwatchElapsedMs by rememberSaveable { mutableStateOf(0L) }
    var startedAt by rememberSaveable { mutableStateOf(0L) }
    var reps by rememberSaveable { mutableStateOf(0) }
    var sets by rememberSaveable { mutableStateOf(0) }

    // 仅用于驱动重绘的时基快照，不参与持久化
    var nowMs by remember { mutableStateOf(SystemClock.elapsedRealtime()) }

    val limitMs = durationSec * 1000L
    val activeElapsedMs = if (countdown) countdownElapsedMs else stopwatchElapsedMs
    val liveElapsedMs = if (running) {
        activeElapsedMs + (nowMs - startedAt).coerceAtLeast(0L)
    } else {
        activeElapsedMs
    }
    val finished = countdown && limitMs > 0 && liveElapsedMs >= limitMs
    val displaySeconds = if (countdown) {
        ((limitMs - liveElapsedMs).coerceAtLeast(0L) / 1000L).toInt()
    } else {
        (liveElapsedMs / 1000L).toInt()
    }

    fun stopRunning() {
        if (!running) return
        val delta = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
        if (countdown) {
            countdownElapsedMs = (countdownElapsedMs + delta).coerceAtMost(limitMs)
        } else {
            stopwatchElapsedMs += delta
        }
        running = false
    }

    LaunchedEffect(running, countdown, durationSec) {
        if (!running) return@LaunchedEffect
        while (isActive) {
            val now = SystemClock.elapsedRealtime()
            nowMs = now
            if (countdown) {
                val elapsed = countdownElapsedMs + (now - startedAt).coerceAtLeast(0L)
                if (elapsed >= limitMs) {
                    countdownElapsedMs = limitMs
                    running = false
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    break
                }
            }
            delay(TICK_MS)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "训练辅助",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppFilterChip(
                    selected = countdown,
                    onClick = {
                        stopRunning()
                        countdown = true
                    },
                    label = "倒计时"
                )
                AppFilterChip(
                    selected = !countdown,
                    onClick = {
                        stopRunning()
                        countdown = false
                    },
                    label = "秒表"
                )
            }

            Text(
                text = formatTime(displaySeconds),
                // tnum：等宽数字，秒数跳动时整体宽度不会左右抖
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                    letterSpacing = 1.sp
                ),
                color = if (finished) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (finished) {
                Text(
                    text = "时间到！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (countdown) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DURATION_PRESETS.forEach { sec ->
                        AppFilterChip(
                            selected = durationSec == sec,
                            onClick = {
                                durationSec = sec
                                countdownElapsedMs = 0L
                                running = false
                                nowMs = SystemClock.elapsedRealtime()
                            },
                            label = "${sec}s"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (running) {
                            stopRunning()
                        } else {
                            if (countdown && countdownElapsedMs >= limitMs) {
                                countdownElapsedMs = 0L
                            }
                            val now = SystemClock.elapsedRealtime()
                            startedAt = now
                            nowMs = now
                            running = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (running) "暂停" else "开始")
                }
                OutlinedButton(
                    onClick = {
                        running = false
                        countdownElapsedMs = 0L
                        stopwatchElapsedMs = 0L
                        startedAt = 0L
                        nowMs = SystemClock.elapsedRealtime()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("重置")
                }
            }

            HorizontalDivider()

            StepperRow(
                label = "次数",
                value = reps,
                onDecrement = { if (reps > 0) reps-- },
                onIncrement = { reps++ }
            )
            StepperRow(
                label = "组数",
                value = sets,
                onDecrement = { if (sets > 0) sets-- },
                onIncrement = { sets++ }
            )
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "减少$label")
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 56.dp),
                textAlign = TextAlign.Center
            )
            FilledIconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "增加$label")
            }
        }
    }
}
