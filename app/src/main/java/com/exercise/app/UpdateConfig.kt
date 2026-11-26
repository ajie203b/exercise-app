package com.exercise.app

/**
 * 更新配置。
 *
 * 只保留真正需要的地址常量：当前版本号一律从 PackageManager 读取，
 * 避免出现「配置里写死 1.0」与真实版本不一致的历史问题。
 */
object UpdateConfig {

    const val GITHUB_USERNAME = "ajie203b"
    const val REPOSITORY_NAME = "exercise-app"

    /** 版本清单（固定格式：versionCode / versionName / description / apkSize / 可选 sha256） */
    const val VERSION_CHECK_URL =
        "https://raw.githubusercontent.com/$GITHUB_USERNAME/$REPOSITORY_NAME/main/version.json"

    /** 安装包下载地址；刻意不使用远程清单里的地址，避免清单被篡改后指向任意 APK */
    const val APK_DOWNLOAD_URL =
        "https://github.com/$GITHUB_USERNAME/$REPOSITORY_NAME/releases/latest/download/app.apk"
}
