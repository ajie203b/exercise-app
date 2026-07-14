# 构建 APK 指南

## 当前状态

✅ 项目代码已完成  
✅ 345 个练习数据已准备  
✅ 345 个 GIF 动画已包含  
❌ 需要 Android SDK 才能构建 APK  

## 快速获取 APK 的方法

### 🚀 方法一：GitHub Actions（最简单，无需安装任何软件）

**步骤：**

1. **创建 GitHub 账号**（如果没有）
   - 访问 https://github.com/signup

2. **创建新仓库**
   - 访问 https://github.com/new
   - 仓库名：`exercise-app`
   - 选择 Public
   - 点击 "Create repository"

3. **上传项目文件**
   - 点击 "uploading an existing file"
   - 将 `ExerciseApp` 目录中的所有文件拖拽上传
   - 点击 "Commit changes"

4. **等待自动构建**
   - 点击 "Actions" 标签
   - 等待构建完成（约 5-10 分钟，显示绿色 ✓）

5. **下载 APK**
   - 点击最新的构建任务
   - 向下滚动到 "Artifacts" 部分
   - 点击 "app-debug" 下载
   - 解压获得 `app-debug.apk`

---

### 🛠️ 方法二：Android Studio（推荐长期使用）

**步骤：**

1. **下载 Android Studio**
   - 访问 https://developer.android.com/studio
   - 下载 Windows 版本（约 1GB）

2. **安装 Android Studio**
   - 运行安装程序
   - 选择 "Standard" 安装
   - 等待安装完成（约 10-15 分钟）

3. **打开项目**
   - 启动 Android Studio
   - 选择 `File` → `Open`
   - 导航到 `D:\AAA-resource\app\exerises\ExerciseApp`
   - 点击 OK

4. **等待同步**
   - 首次打开需要下载依赖（约 5-10 分钟）
   - 等待右下角进度条完成

5. **构建 APK**
   - 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - 等待构建完成（约 2-3 分钟）

6. **获取 APK**
   - 点击右下角的 "locate" 链接
   - 或者手动查找：`app\build\outputs\apk\debug\app-debug.apk`

---

### 👥 方法三：请朋友帮忙

如果您有使用 Android Studio 的朋友：

1. 将 `ExerciseApp` 文件夹打包（ZIP 压缩）
2. 发送给朋友
3. 请他们按照方法二构建
4. 让他们把生成的 APK 文件发回给您

---

## 安装 APK 到手机

### 步骤 1: 传输 APK 到手机
- 使用 USB 数据线
- 通过微信/QQ 发送
- 使用邮件附件
- 使用云盘（百度网盘、Google Drive 等）

### 步骤 2: 允许安装未知来源应用
1. 打开手机 "设置"
2. 找到 "安全" 或 "隐私"
3. 开启 "允许安装未知来源应用"
4. 选择您用来打开 APK 的应用（如文件管理器）

### 步骤 3: 安装 APK
1. 在手机上打开 "文件管理器"
2. 找到下载的 APK 文件
3. 点击 APK 文件
4. 按照提示点击 "安装"
5. 等待安装完成
6. 点击 "打开" 启动应用

---

## 使用安卓模拟器（如果没有安卓手机）

### 推荐模拟器
1. **BlueStacks**（最流行）
   - 下载：https://www.bluestacks.com
   - 适合游戏和普通应用

2. **NoxPlayer**
   - 下载：https://www.bignox.com
   - 轻量级，性能好

3. **LDPlayer**
   - 下载：https://www.ldplayer.net
   - 中国用户友好

### 安装步骤
1. 下载并安装模拟器
2. 启动模拟器
3. 将 APK 文件拖拽到模拟器窗口
4. 或者在模拟器中打开浏览器下载 APK
5. 按照提示安装

---

## 常见问题

### Q: GitHub Actions 构建失败怎么办？
A: 可能的原因：
- 网络问题：稍后重试
- 文件上传不完整：确保上传了所有文件
- 代码错误：检查 Actions 日志中的错误信息

### Q: Android Studio 安装太慢怎么办？
A: 
- 使用国内镜像下载
- 配置代理服务器
- 使用迅雷等下载工具

### Q: APK 安装后无法打开怎么办？
A:
- 检查手机 Android 版本（需要 8.0 以上）
- 确保手机有足够的存储空间
- 尝试卸载后重新安装

### Q: 应用闪退怎么办？
A:
- 检查 Logcat 日志
- 确保手机内存充足
- 尝试重启应用或手机

---

## 获取帮助

如果遇到问题，请提供：
1. 您使用的操作系统版本
2. Android Studio 版本（如果使用）
3. 错误信息截图
4. 构建日志（如果有的话）

我会尽力帮助您解决问题！

---

## 最快的解决方案

**如果您现在就需要 APK，建议：**

1. 使用 GitHub Actions（约 15 分钟）
   - 无需安装任何软件
   - 自动构建
   - 免费

2. 请朋友帮忙（约 10 分钟）
   - 最快的方式
   - 无需学习新工具

3. 使用云开发环境（约 20 分钟）
   - Gitpod: https://gitpod.io
   - 无需本地安装
