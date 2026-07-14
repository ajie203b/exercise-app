# 健身训练 App - APK 构建脚本
# 此脚本需要在 Android Studio 终端或配置了 Android SDK 的环境中运行

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   健身训练 App - APK 构建脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Java
Write-Host "检查 Java 环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java 已安装: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 未找到 Java，请安装 JDK 17 或更高版本" -ForegroundColor Red
    exit 1
}

# 检查 Android SDK
Write-Host ""
Write-Host "检查 Android SDK..." -ForegroundColor Yellow
if ($env:ANDROID_HOME) {
    Write-Host "✓ ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Green
} elseif ($env:ANDROID_SDK_ROOT) {
    Write-Host "✓ ANDROID_SDK_ROOT: $env:ANDROID_SDK_ROOT" -ForegroundColor Green
} else {
    Write-Host "⚠ 警告: 未设置 ANDROID_HOME 或 ANDROID_SDK_ROOT 环境变量" -ForegroundColor Yellow
    Write-Host "  请确保已安装 Android SDK 并配置环境变量" -ForegroundColor Yellow
}

# 检查 local.properties
Write-Host ""
Write-Host "检查 local.properties..." -ForegroundColor Yellow
if (-not (Test-Path "local.properties")) {
    Write-Host "创建 local.properties 文件..." -ForegroundColor Yellow
    $sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
    if (Test-Path $sdkPath) {
        "sdk.dir=$sdkPath.Replace('\', '\\')" | Out-File -FilePath "local.properties" -Encoding UTF8
        Write-Host "✓ 已创建 local.properties" -ForegroundColor Green
    } else {
        Write-Host "⚠ 请手动创建 local.properties 文件并设置 sdk.dir" -ForegroundColor Yellow
    }
}

# 生成 Gradle Wrapper
Write-Host ""
Write-Host "生成 Gradle Wrapper..." -ForegroundColor Yellow
if (-not (Test-Path "gradlew.bat")) {
    if (Get-Command "gradle" -ErrorAction SilentlyContinue) {
        gradle wrapper --gradle-version 8.5
        Write-Host "✓ Gradle Wrapper 已生成" -ForegroundColor Green
    } else {
        Write-Host "✗ 错误: 未找到 Gradle 命令" -ForegroundColor Red
        Write-Host "  请使用 Android Studio 打开项目，它会自动生成 Gradle Wrapper" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "替代方案:" -ForegroundColor Cyan
        Write-Host "1. 用 Android Studio 打开 ExerciseApp 目录" -ForegroundColor White
        Write-Host "2. 等待 Gradle 同步完成" -ForegroundColor White
        Write-Host "3. 点击 Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor White
        exit 1
    }
}

# 构建 APK
Write-Host ""
Write-Host "开始构建 Debug APK..." -ForegroundColor Yellow
Write-Host "这可能需要几分钟时间，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

try {
    .\gradlew.bat assembleDebug

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=====================================" -ForegroundColor Green
        Write-Host "   APK 构建成功！" -ForegroundColor Green
        Write-Host "=====================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "APK 文件位置:" -ForegroundColor Cyan
        Write-Host "  app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        Write-Host ""

        # 检查 APK 文件
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "APK 大小: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
        }

        Write-Host ""
        Write-Host "安装方法:" -ForegroundColor Yellow
        Write-Host "1. 将 APK 文件传输到安卓手机" -ForegroundColor White
        Write-Host "2. 在手机上打开文件管理器" -ForegroundColor White
        Write-Host "3. 找到并点击 APK 文件" -ForegroundColor White
        Write-Host "4. 按照提示完成安装" -ForegroundColor White
        Write-Host ""
        Write-Host "或者使用 ADB 命令安装:" -ForegroundColor Yellow
        Write-Host "  adb install $apkPath" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "✗ APK 构建失败" -ForegroundColor Red
        Write-Host "请检查错误信息并修复问题" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "✗ 构建过程中发生错误: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
