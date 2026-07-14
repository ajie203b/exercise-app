# 使用 Android Studio 构建 APK
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   使用 Android Studio 构建 APK" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$studioPath = "D:\AAA-resource\photo"
$projectPath = "D:\AAA-resource\app\exerises\ExerciseApp"

# 检查 Android Studio
Write-Host "检查 Android Studio..." -ForegroundColor Yellow
if (-not (Test-Path "$studioPath\bin\studio64.exe")) {
    Write-Host "✗ Android Studio 未找到" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Android Studio 已找到" -ForegroundColor Green

# 检查项目
Write-Host ""
Write-Host "检查项目..." -ForegroundColor Yellow
if (-not (Test-Path "$projectPath\app\build.gradle.kts")) {
    Write-Host "✗ 项目文件不完整" -ForegroundColor Red
    exit 1
}
Write-Host "✓ 项目文件完整" -ForegroundColor Green

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   构建步骤" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 启动 Android Studio" -ForegroundColor Yellow
Write-Host "2. 打开项目: $projectPath" -ForegroundColor Yellow
Write-Host "3. 等待 Gradle 同步完成" -ForegroundColor Yellow
Write-Host "4. 点击 Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor Yellow
Write-Host "5. APK 位置: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Yellow
Write-Host ""

# 启动 Android Studio 并打开项目
Write-Host "正在启动 Android Studio 并打开项目..." -ForegroundColor Green
Start-Process "$studioPath\bin\studio64.exe" -ArgumentList "$projectPath"

Write-Host ""
Write-Host "Android Studio 已启动！" -ForegroundColor Green
Write-Host ""
Write-Host "请在 Android Studio 中:" -ForegroundColor Cyan
Write-Host "1. 等待 Gradle 同步完成（右下角进度条）" -ForegroundColor White
Write-Host "2. 点击菜单: Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor White
Write-Host "3. 构建完成后，点击右下角的 'locate' 链接查看 APK" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
