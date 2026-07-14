# 📱 健身训练 App - 自动更新配置指南

## 🎯 功能说明

应用已集成自动更新功能，支持：
- ✅ 检查新版本
- ✅ 下载更新
- ✅ 自动安装

---

## 🚀 快速配置（3分钟完成）

### 第1步：创建 GitHub 仓库

1. 登录 GitHub
2. 点击右上角 `+` → `New repository`
3. 仓库名：`exercise-app`（或其他名字）
4. 选择 `Public`（公开）
5. 点击 `Create repository`

### 第2步：修改配置文件

打开 `app/src/main/java/com/exercise/app/UpdateConfig.kt`

修改以下两行：

```kotlin
// TODO: 替换为你的 GitHub 用户名
const val GITHUB_USERNAME = "your-github-username"  // ← 改成你的用户名

// TODO: 替换为你的仓库名
const val REPOSITORY_NAME = "exercise-app"  // ← 改成你的仓库名
```

### 第3步：创建版本检查文件

在你的仓库根目录创建 `version.json` 文件：

```json
{
  "versionCode": 2,
  "versionName": "1.1",
  "description": "1. 新增了收藏功能\n2. 修复了若干bug\n3. 优化了界面显示",
  "apkSize": "5.2MB"
}
```

### 第4步：上传 APK 文件

1. 编译生成 APK：
   ```bash
   ./gradlew assembleRelease
   ```

2. 在 GitHub 仓库页面：
   - 点击 `Releases` → `Draft a new release`
   - 填写版本号（如 `v1.1`）
   - 上传 `app/build/outputs/apk/release/app-release.apk`
   - 重命名为 `app.apk`
   - 点击 `Publish release`

---

## 📋 版本号说明

| 字段 | 说明 | 示例 |
|------|------|------|
| `versionCode` | 整数，每次更新递增 | `1`, `2`, `3`... |
| `versionName` | 版本名称，显示给用户 | `"1.0"`, `"1.1"` |
| `description` | 更新内容描述 | `"修复了bug"` |
| `apkSize` | APK文件大小（可选） | `"5.2MB"` |

---

## 🔄 更新流程

```
用户点击检查更新
    ↓
从 GitHub 获取 version.json
    ↓
比较 versionCode
    ↓
有新版本？ ──否──→ 提示"已是最新版本"
    │是
    ↓
显示更新对话框
    ↓
用户点击"立即更新"
    ↓
下载 APK 文件
    ↓
自动安装
```

---

## 🛠️ 常见问题

### Q1: 检查更新失败？
**原因**：网络问题或配置错误

**解决**：
1. 检查 `GITHUB_USERNAME` 和 `REPOSITORY_NAME` 是否正确
2. 确保仓库是 `Public`（公开的）
3. 检查网络连接

### Q2: 下载失败？
**原因**：APK文件不存在或网络问题

**解决**：
1. 确保已上传 APK 到 Releases
2. APK 文件名必须是 `app.apk`
3. 检查网络连接

### Q3: 安装失败？
**原因**：存储权限或签名问题

**解决**：
1. 允许应用安装未知来源应用
2. 确保 APK 签名与已安装版本一致

---

## 📝 完整配置示例

假设你的 GitHub 用户名是 `zhangsan`，仓库名是 `fitness-app`：

### UpdateConfig.kt：
```kotlin
const val GITHUB_USERNAME = "zhangsan"
const val REPOSITORY_NAME = "fitness-app"
```

### version.json：
```json
{
  "versionCode": 2,
  "versionName": "1.1",
  "description": "1. 新增了收藏功能\n2. 优化了搜索体验",
  "apkSize": "5.2MB"
}
```

### 仓库地址：
- 仓库：`https://github.com/zhangsan/fitness-app`
- 版本检查：`https://raw.githubusercontent.com/zhangsan/fitness-app/main/version.json`
- 下载地址：`https://github.com/zhangsan/fitness-app/releases/latest/download/app.apk`

---

## ✅ 配置完成检查清单

- [ ] 已创建 GitHub 仓库
- [ ] 已修改 `UpdateConfig.kt` 中的用户名和仓库名
- [ ] 已创建 `version.json` 文件
- [ ] 已上传 APK 到 Releases
- [ ] 已测试检查更新功能

---

## 🎉 完成！

现在你的应用支持自动更新了！

**每次发布新版本时**：
1. 修改 `version.json` 中的版本号
2. 上传新的 APK 文件
3. 用户点击检查更新即可获得最新版本

---

**有问题？** 检查以下文件：
- `UpdateConfig.kt` - 配置文件
- `UpdateManager.kt` - 更新管理器
- `version.json` - 版本信息文件
