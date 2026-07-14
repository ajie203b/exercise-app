# 查找 Android Studio 安装位置
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   查找 Android Studio 安装位置" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 常见安装路径
$possiblePaths = @(
    "C:\Program Files\Android\Android Studio",
    "C:\Program Files (x86)\Android\Android Studio",
    "$env:LOCALAPPDATA\Android\Android Studio",
    "$env:PROGRAMFILES\Android\Android Studio",
    "D:\Android Studio",
    "C:\Android Studio"
)

$found = $false

Write-Host "正在搜索 Android Studio..." -ForegroundColor Yellow
Write-Host ""

foreach ($path in $possiblePaths) {
    if (Test-Path "$path\bin\studio64.exe") {
        Write-Host "✓ 找到 Android Studio!" -ForegroundColor Green
        Write-Host "  路径: $path" -ForegroundColor White
        Write-Host "  可执行文件: $path\bin\studio64.exe" -ForegroundColor White
        $found = $true

        # 检查 SDK
        $sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
        if (Test-Path $sdkPath) {
            Write-Host ""
            Write-Host "✓ 找到 Android SDK!" -ForegroundColor Green
            Write-Host "  SDK 路径: $sdkPath" -ForegroundColor White

            # 检查必要的组件
            $requiredComponents = @(
                "$sdkPath\platform-tools\adb.exe",
                "$sdkPath\platforms",
                "$sdkPath\build-tools"
            )

            Write-Host ""
            Write-Host "检查 SDK 组件:" -ForegroundColor Yellow
            foreach ($component in $requiredComponents) {
                if (Test-Path $component) {
                    Write-Host "  ✓ $(Split-Path $component -Leaf)" -ForegroundColor Green
                } else {
                    Write-Host "  ✗ $(Split-Path $component -Leaf) (缺失)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host ""
            Write-Host "⚠ 未找到 Android SDK" -ForegroundColor Yellow
            Write-Host "  预期位置: $sdkPath" -ForegroundColor White
            Write-Host "  请在 Android Studio 中安装 SDK" -ForegroundColor Yellow
        }

        break
    }
}

if (-not $found) {
    Write-Host "✗ 未找到 Android Studio" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "1. 是否已安装 Android Studio？" -ForegroundColor White
    Write-Host "2. 安装路径是否正确？" -ForegroundColor White
    Write-Host ""
    Write-Host "常见安装位置:" -ForegroundColor Yellow
    foreach ($path in $possiblePaths) {
        Write-Host "  - $path" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "如果已安装在其他位置，请告诉我完整路径。" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
