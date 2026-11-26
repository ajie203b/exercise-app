package com.exercise.app

import android.app.Application

class ExerciseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 图片加载器只初始化一次，避免随 Activity 重建重复创建缓存
        CoilConfig.init(this)
    }
}
