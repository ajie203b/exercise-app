@echo off
echo 健身训练 App 构建脚本
echo ====================
echo.

echo 检查 Android Studio...
where studio64.exe >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: 未找到 Android Studio，请确保已安装并配置环境变量
    echo.
)

echo 检查 Gradle...
where gradle.bat >nul 2>&1
if %errorlevel% neq 0 (
    echo 使用 Gradle Wrapper...
    set GRADLE_CMD=gradlew.bat
) else (
    set GRADLE_CMD=gradle
)

echo.
echo 选择操作:
echo 1. 构建 Debug APK
echo 2. 构建 Release APK
echo 3. 清理项目
echo 4. 退出
echo.

set /p choice="请输入选项 (1-4): "

if "%choice%"=="1" (
    echo.
    echo 构建 Debug APK...
    call %GRADLE_CMD% assembleDebug
    if %errorlevel% equ 0 (
        echo.
        echo 构建成功！APK 文件位于: app\build\outputs\apk\debug\app-debug.apk
    )
) else if "%choice%"=="2" (
    echo.
    echo 构建 Release APK...
    call %GRADLE_CMD% assembleRelease
    if %errorlevel% equ 0 (
        echo.
        echo 构建成功！APK 文件位于: app\build\outputs\apk\release\app-release-unsigned.apk
    )
) else if "%choice%"=="3" (
    echo.
    echo 清理项目...
    call %GRADLE_CMD% clean
    echo 清理完成！
) else if "%choice%"=="4" (
    echo 退出...
    exit /b 0
) else (
    echo 无效选项，请重新运行脚本
)

echo.
pause
