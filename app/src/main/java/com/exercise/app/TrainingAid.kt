package com.exercise.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class TimerMode { Countdown, Stopwatch }

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun TrainingAidSection(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(TimerMode.Countdown) }
    var durationSec by remember { mutableStateOf(60) }
    var remainingSec by remember { mutableStateOf(60) }
    var elapsedSec by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var reps by remember { mutableStateOf(0) }
    var sets by remember { mutableStateOf(0) }

    LaunchedEffect(running, mode) {
        if (!running) return@LaunchedEffect
        while (running) {
            delay(1000)
            if (mode == TimerMode.Countdown) {
                if (remainingSec > 0) remainingSec--
                if (remainingSec == 0) running = false
            } else {
                elapsedSec++
            }
        }
    }

    val finished = mode == TimerMode.Countdown && remainingSec == 0 && durationSec > 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("训练辅助", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == TimerMode.Countdown,
                    onClick = { mode = TimerMode.Countdown; running = false },
                    label = { Text("倒计时") }
                )
                FilterChip(
                    selected = mode == TimerMode.Stopwatch,
                    onClick = { mode = TimerMode.Stopwatch; running = false },
                    label = { Text("秒表") }
                )
            }

            Text(
                text = if (mode == TimerMode.Countdown) formatTime(remainingSec) else formatTime(elapsedSec),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = if (finished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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

            if (mode == TimerMode.Countdown) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90, 120).forEach { sec ->
                        FilterChip(
                            selected = durationSec == sec,
                            onClick = {
                                durationSec = sec
                                remainingSec = sec
                                running = false
                            },
                            label = { Text("${sec}s") }
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
                        if (mode == TimerMode.Countdown && remainingSec == 0) remainingSec = durationSec
                        running = !running
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
                        remainingSec = durationSec
                        elapsedSec = 0
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("重置")
                }
            }

            HorizontalDivider()

            StepperRow(label = "次数", value = reps, onDecrement = { if (reps > 0) reps-- }, onIncrement = { reps++ })
            StepperRow(label = "组数", value = sets, onDecrement = { if (sets > 0) sets-- }, onIncrement = { sets++ })
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            FilledIconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "增加$label")
            }
        }
    }
}