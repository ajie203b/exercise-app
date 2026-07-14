# 健身训练 App 安装指南

## 方法一：使用 Android Studio（推荐）

### 1. 安装 Android Studio

1. 访问 https://developer.android.com/studio
2. 下载最新版本的 Android Studio
3. 按照安装向导完成安装

### 2. 配置 Android SDK

1. 打开 Android Studio
2. 进入 `File` → `Settings` → `Appearance & Behavior` → `System Settings` → `Android SDK`
3. 确保已安装：
   - Android SDK Platform 34
   - Android SDK Build-Tools 34
   - Android SDK Platform-Tools

### 3. 打开项目

1. 在 Android Studio 中选择 `File` → `Open`
2. 导航到 `ExerciseApp` 目录并打开
3. 等待 Gradle 同步完成（可能需要几分钟）

### 4. 运行 App

1. 连接安卓设备或启动模拟器
   - **真机**：启用开发者选项和 USB 调试
   - **模拟器**：`Tools` → `AVD Manager` → `Create Virtual Device`
2. 点击工具栏的运行按钮 ▶️
3. 选择目标设备
4. App 将自动安装并启动

## 方法二：使用命令行

### 1. 安装 Java JDK

1. 下载并安装 JDK 17 或更高版本
2. 设置 JAVA_HOME 环境变量

### 2. 配置 Android SDK

1. 设置 ANDROID_HOME 环境变量指向 SDK 路径
2. 将 SDK 的 `platform-tools` 添加到 PATH

### 3. 构建 APK

```bash
cd ExerciseApp

# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

### 4. 安装到设备

```bash
# 确保设备已连接
adb devices

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 方法三：直接安装 APK

如果你有已构建的 APK 文件：

1. 将 APK 文件传输到安卓设备
2. 在设备上打开文件管理器
3. 找到并点击 APK 文件
4. 按照提示完成安装
5. 如果提示"未知来源"，需要在设置中允许安装

## 常见问题

### Q: Gradle 同步失败
A: 检查网络连接，确保可以访问 Google 和 Maven 仓库。可能需要配置代理。

### Q: 找不到 Android SDK
A: 在 `local.properties` 文件中设置正确的 SDK 路径：
```
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### Q: 模拟器运行缓慢
A: 启用硬件加速：
- Intel CPU: 安装 Intel HAXM
- AMD CPU: 启用 Windows Hypervisor Platform

### Q: App 崩溃
A: 检查 Logcat 输出，查看错误信息。常见原因：
- 内存不足
- Android 版本不兼容
- 缺少权限

## 系统要求

- **操作系统**: Windows 10/11, macOS 10.14+, 或 Linux
- **内存**: 至少 8GB RAM（推荐 16GB）
- **磁盘空间**: 至少 10GB 可用空间
- **Android**: 最低 SDK 26 (Android 8.0)

## 获取帮助

如果遇到问题：
1. 查看 Android Studio 的 Build 输出
2. 检查 Logcat 中的错误日志
3. 确保所有依赖都已正确下载
4. 尝试 `File` → `Sync Project with Gradle Files`
