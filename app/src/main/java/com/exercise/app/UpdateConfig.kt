package com.exercise.app

/**
 * 更新配置
 */
object UpdateConfig {
    // ==================== 需要修改的配置 ====================

    // GitHub 配置
    const val GITHUB_USERNAME = "ajie203b"
    const val REPOSITORY_NAME = "exercise-app"

    // 版本检查文件地址（固定格式）
    const val VERSION_CHECK_URL =
        "https://raw.githubusercontent.com/$GITHUB_USERNAME/$REPOSITORY_NAME/main/version.json"

    // 下载地址（固定格式）
    const val APK_DOWNLOAD_URL =
        "https://github.com/$GITHUB_USERNAME/$REPOSITORY_NAME/releases/latest/download/app.apk"

    // =====================================================

    /**
     * 获取当前版本信息
     * @return Pair(版本号, 版本名称)
     */
    fun getCurrentVersion(): Pair<Int, String> {
        // 这个会在运行时被 UpdateManager 替换
        return Pair(1, "1.0")
    }
}
