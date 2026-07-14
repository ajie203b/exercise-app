package com.exercise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.exercise.app.ui.theme.ExerciseAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化更新管理器
        updateManager = UpdateManager(this)
        updateManager.init()

        val exercises = loadExercisesFromAssets(this)

        setContent {
            ExerciseAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExerciseApp(
                        exercises = exercises,
                        updateManager = updateManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        if (::updateManager.isInitialized) {
            updateManager.destroy()
        }
    }
}
