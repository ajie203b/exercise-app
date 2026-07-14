# 如何获取 APK 安装包

## 方法一：使用 Android Studio（推荐）

### 步骤 1: 安装 Android Studio
1. 访问 https://developer.android.com/studio
2. 下载并安装 Android Studio
3. 首次启动时，选择 "Standard" 安装类型，它会自动安装 Android SDK

### 步骤 2: 打开项目
1. 启动 Android Studio
2. 选择 `File` → `Open`
3. 导航到 `ExerciseApp` 目录并打开
4. 等待 Gradle 同步完成（右下角显示进度条）

### 步骤 3: 构建 APK
1. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. 等待构建完成
3. 点击右下角的 "locate" 链接打开 APK 所在目录
4. APK 文件位于 `app/build/outputs/apk/debug/app-debug.apk`

---

## 方法二：使用 GitHub Actions（无需本地环境）

### 步骤 1: 创建 GitHub 仓库
1. 访问 https://github.com/new
2. 创建一个新仓库（如 `exercise-app`）

### 步骤 2: 上传代码
```bash
cd ExerciseApp
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/你的用户名/exercise-app.git
git push -u origin main
```

### 步骤 3: 等待自动构建
1. 打开 GitHub 仓库页面
2. 点击 "Actions" 标签
3. 等待构建完成（约 5-10 分钟）

### 步骤 4: 下载 APK
1. 点击最新的构建任务
2. 在 "Artifacts" 部分下载 `app-debug` 压缩包
3. 解压后获得 `app-debug.apk` 文件

---

## 方法三：使用在线构建服务

### 使用 Appetize.io
1. 访问 https://appetize.io
2. 上传 APK 文件（如果已有）或使用他们的构建服务

### 使用 Ionic Appflow
1. 访问 https://ionic.io/appflow
2. 上传项目并构建

---

## 方法四：使用命令行（需要 Android SDK）

### 前提条件
1. 安装 JDK 17+
2. 安装 Android SDK
3. 设置环境变量 ANDROID_HOME

### 构建步骤
```bash
cd ExerciseApp

# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

### APK 位置
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 方法五：使用预构建的 APK

如果您无法构建 APK，可以：

1. **请朋友帮忙构建**
   - 将 `ExerciseApp` 目录打包发送给有 Android 开发环境的朋友
   - 请他们构建 APK 并发回给您

2. **使用云开发环境**
   - Gitpod: https://gitpod.io
   - GitHub Codespaces: https://github.com/features/codespaces
   - 创建账号，上传项目，在线构建

---

## 安装 APK 到手机

### 方法 A: 直接安装
1. 将 APK 文件传输到手机（USB、微信、邮件等）
2. 在手机上打开 "文件管理器"
3. 找到并点击 APK 文件
4. 如果提示 "未知来源"，需要在设置中允许安装
5. 按照提示完成安装

### 方法 B: 使用 ADB 安装
1. 手机开启 "开发者选项" 和 "USB 调试"
2. 连接手机到电脑
3. 运行命令：
   ```bash
   adb install app-debug.apk
   ```

---

## 常见问题

### Q: 没有安卓手机怎么办？
A: 可以使用安卓模拟器：
- Android Studio 自带模拟器
- 第三方模拟器：BlueStacks, NoxPlayer, LDPlayer

### Q: 构建失败怎么办？
A: 常见原因：
1. 网络问题 - 配置代理或使用 VPN
2. SDK 版本不匹配 - 确保安装了 SDK 34
3. 内存不足 - 增加 Gradle 内存设置

### Q: APK 安装失败怎么办？
A: 检查以下设置：
1. 手机设置 → 安全 → 允许未知来源
2. 确保手机有足够的存储空间
3. 检查 APK 文件是否完整

---

## 最快的解决方案

如果您现在就需要 APK，最快的方法是：

1. **使用 GitHub Actions**（约 10 分钟）
   - 创建 GitHub 仓库
   - 上传代码
   - 等待自动构建
   - 下载 APK

2. **请朋友帮忙**（约 5 分钟）
   - 找一个有 Android Studio 的朋友
   - 发送项目文件
   - 请他们构建并发送 APK

---

## 需要帮助？

如果遇到问题，请提供：
1. 您使用的操作系统
2. 是否已安装 Android Studio
3. 错误信息截图
4. 构建日志

我会尽力帮助您解决问题！
