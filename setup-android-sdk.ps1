# Android SDK 配置脚本
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   Android SDK 配置向导" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$studioPath = "D:\AAA-resource\photo"
$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"

# 检查 Android Studio
Write-Host "检查 Android Studio..." -ForegroundColor Yellow
if (Test-Path "$studioPath\bin\studio64.exe") {
    Write-Host "✓ Android Studio 已找到" -ForegroundColor Green
    Write-Host "  路径: $studioPath" -ForegroundColor White
} else {
    Write-Host "✗ Android Studio 未找到" -ForegroundColor Red
    exit 1
}

# 检查 SDK 状态
Write-Host ""
Write-Host "检查 Android SDK..." -ForegroundColor Yellow
if (Test-Path $sdkPath) {
    Write-Host "✓ SDK 目录存在" -ForegroundColor Green
    Write-Host "  路径: $sdkPath" -ForegroundColor White

    # 检查组件
    $components = @(
        @{Name="platform-tools (adb)"; Path="platform-tools\adb.exe"},
        @{Name="platforms"; Path="platforms"},
        @{Name="build-tools"; Path="build-tools"}
    )

    Write-Host ""
    Write-Host "SDK 组件状态:" -ForegroundColor Yellow
    $missing = @()
    foreach ($comp in $components) {
        if (Test-Path "$sdkPath\$($comp.Path)") {
            Write-Host "  ✓ $($comp.Name)" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $($comp.Name) (缺失)" -ForegroundColor Red
            $missing += $comp.Name
        }
    }

    if ($missing.Count -gt 0) {
        Write-Host ""
        Write-Host "需要安装以下组件:" -ForegroundColor Yellow
        foreach ($m in $missing) {
            Write-Host "  - $m" -ForegroundColor White
        }
    }
} else {
    Write-Host "⚠ SDK 目录不存在，将创建" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $sdkPath -Force | Out-Null
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   下一步操作" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "请按以下步骤操作:" -ForegroundColor White
Write-Host ""
Write-Host "1. 启动 Android Studio" -ForegroundColor Yellow
Write-Host "   命令: & '$studioPath\bin\studio64.exe'" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 首次启动配置:" -ForegroundColor Yellow
Write-Host "   - 选择 'Don't import settings'" -ForegroundColor White
Write-Host "   - 选择 'Standard' 安装类型" -ForegroundColor White
Write-Host "   - 等待 SDK 下载完成（约 10-15 分钟）" -ForegroundColor White
Write-Host ""
Write-Host "3. 安装必要的 SDK 组件:" -ForegroundColor Yellow
Write-Host "   - 点击 'More Actions' -> 'SDK Manager'" -ForegroundColor White
Write-Host "   - 在 'SDK Platforms' 标签页，勾选 'Android 14 (API 34)'" -ForegroundColor White
Write-Host "   - 在 'SDK Tools' 标签页，确保勾选:" -ForegroundColor White
Write-Host "     * Android SDK Build-Tools 34" -ForegroundColor White
Write-Host "     * Android SDK Command-line Tools" -ForegroundColor White
Write-Host "     * Android SDK Platform-Tools" -ForegroundColor White
Write-Host "     * Android Emulator (如果需要模拟器)" -ForegroundColor White
Write-Host "   - 点击 'Apply' 下载安装" -ForegroundColor White
Write-Host ""
Write-Host "4. 配置完成后，告诉我" -ForegroundColor Cyan
Write-Host "   我会帮您构建 ExerciseApp 项目" -ForegroundColor White
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "是否现在启动 Android Studio? (Y/N)" -ForegroundColor Yellow
$response = Read-Host
if ($response -eq 'Y' -or $response -eq 'y') {
    Write-Host ""
    Write-Host "正在启动 Android Studio..." -ForegroundColor Green
    Start-Process "$studioPath\bin\studio64.exe"
    Write-Host ""
    Write-Host "Android Studio 已启动，请按照上述步骤配置 SDK" -ForegroundColor White
    Write-Host "配置完成后告诉我，我会帮您继续" -ForegroundColor Cyan
}
