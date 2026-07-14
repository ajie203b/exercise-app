# 健身训练 App

一个基于 Jetpack Compose 的安卓健身训练应用，包含 345 个健身练习。

## 功能特点

- 📱 现代 Material Design 3 UI
- 🏋️ 345 个健身练习（徒手、健腹轮、跳绳）
- 🎬 每个练习配有 GIF 动画演示
- 🔍 搜索和分类筛选功能
- 📝 详细的训练步骤说明
- 💪 肌肉群和锻炼目标信息

## 器材筛选

本 App 只包含以下器材的练习：
- **徒手** (body weight) - 325 个练习
- **健腹轮** (roller/wheel roller) - 10 个练习
- **跳绳** (rope) - 10 个练习
## 软件图标
<img width="252" height="238" alt="image" src="https://github.com/user-attachments/assets/b5094649-e505-427f-ba8e-6bfd974b3d37" />

## 页面图
<img width="641" height="1350" alt="image" src="https://github.com/user-attachments/assets/10bf3538-774a-4340-9412-66f19614ca54" />


## 构建和运行

### 前提条件

1. 安装 Android Studio (推荐 Hedgehog 或更新版本)
2. 安装 Android SDK 34
3. 配置 JAVA_HOME 环境变量

### 构建步骤

1. 用 Android Studio 打开 `ExerciseApp` 目录
2. 等待 Gradle 同步完成
3. 连接安卓设备或启动模拟器
4. 点击运行按钮 ▶️

### 命令行构建

```bash
# 构建 debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 项目结构

```
ExerciseApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/exercise/app/
│   │   │   ├── MainActivity.kt          # 主 Activity
│   │   │   ├── ExerciseApp.kt           # 主 UI 组件
│   │   │   ├── ExerciseCard.kt          # 练习卡片组件
│   │   │   ├── ExerciseDetailScreen.kt  # 练习详情页面
│   │   │   ├── Exercise.kt              # 数据模型
│   │   │   └── DataLoader.kt            # 数据加载器
│   │   ├── assets/
│   │   │   ├── exercises.json           # 筛选后的练习数据
│   │   │   └── images/                  # GIF 动画文件
│   │   └── res/
│   │       ├── values/                  # 字符串和主题资源
│   │       └── xml/                     # 配置文件
│   └── build.gradle.kts                 # App 构建配置
├── build.gradle.kts                     # 项目构建配置
├── settings.gradle.kts                  # 项目设置
└── prepare_assets.js                    # 数据准备脚本
```

## 数据来源

数据集来自 [exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset)，包含 1324 个健身练习。

## 许可证

本项目仅供个人学习使用。
