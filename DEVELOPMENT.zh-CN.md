# Android 主题背景版：开发、打包与分发

项目目录：`D:\aproject\ClashMetaForAndroid-Themes`。
仓库：https://github.com/Wodecj/ClashMetaForAndroid 。
上游及修改记录见 MODIFICATIONS.md；此项目只支持 Android。

## 1. 使用方式

安装 APK 后进入「设置 → 应用 → 界面」：

1. 「主题背景」选择默认、海洋、森林、晚霞或自选图片。
2. 「选择背景图片」通过系统文件选择器导入图片，成功后自动启用。
3. 「背景遮罩浓度」拖动滑条在 0–100% 间调节，实时预览，松手保存。0% 完整显示原图，100% 完全遮住背景；可与原有深色模式组合。
4. 「恢复默认并删除自选图片」恢复默认外观。

背景覆盖继承 BaseActivity 的主要页面；themes.4 起，代理页节点卡片和顶部栏也随遮罩滑条调整透明度；0% 时未选中卡片透明，选中节点保留半透明高亮。低浓度时为文字增加对比阴影。弹窗保持可读的表面色。
图片最大 20 MiB，最长边采样到不超过 1920 像素，居中裁剪。取消选择保持现状。
首次启动仍需自行导入有效的代理配置；软件不附带订阅或服务器。

## 2. 精确开发环境

以源码构建配置为准，上游 README 的 JDK 11 说明已过时。

| 组件 | 版本 / 要求 |
| --- | --- |
| IDE | Android Studio，可直接打开项目根目录；IDE 本身不决定构建版本 |
| JDK | 21，建议 Eclipse Temurin 21 |
| Gradle | Wrapper 固定 8.10.2，不必单独装 Gradle |
| Android Gradle Plugin / Kotlin | 8.8.0 / 2.1.0 |
| SDK | platform android-35，build-tools 35.0.0 |
| NDK | 29.0.14206865 |
| CMake | 3.22.1 |
| Go | 上游 MetaCubeX/go 定制 1.26 工具链，并应用 .github/patch 中的补丁 |
| 系统 | Windows 10/11 可做界面开发；完整原生构建优先用 GitHub Actions 的 Ubuntu 24.04 |
| 手机 | 源码 minSdk 21（Android 5.0），targetSdk 35；兼容性仍需实机验证 |

当前工作流构建 arm64-v8a、armeabi-v7a、x86、x86_64 和 universal APK。
普通新手机优先 arm64-v8a，不清楚架构就用 universal。

## 3. 拉取和本地配置（PowerShell）

```powershell
cd D:\aproject
git clone --recurse-submodules https://github.com/Wodecj/ClashMetaForAndroid.git ClashMetaForAndroid-Themes
cd ClashMetaForAndroid-Themes
git submodule update --init --recursive

# 按实际安装路径修改；只对当前终端生效
$env:JAVA_HOME = 'D:\AndroidDev\jdk-21'
$env:ANDROID_HOME = 'D:\AndroidDev\sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

# 用 Android Studio 的 SDK Manager 安装，或调用已安装的 sdkmanager
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'ndk;29.0.14206865' 'cmake;3.22.1'
sdkmanager --licenses
@'
sdk.dir=D:/AndroidDev/sdk
custom.application.id=com.github.wodecj.clash.themes
'@ | Set-Content -Encoding ascii local.properties
```

需安装并配置上表的 Go，不能直接假设普通官方最新版可替代定制版。
在 Git Bash/WSL 中进入 Go 安装根目录逐个执行 `patch -p1 < 项目/.github/patch/文件.patch`。
工作流提供了可复用的完整 Linux 环境与补丁命令；Windows 原生工具链尚未验证。

```powershell
.\gradlew.bat :app:assembleMetaDebug
# 调试 APK 仅用于本地调试；正式分发使用下方 release 流程。
adb install -r '实际的调试APK路径'
```

如果下载失败，配置自己的可用代理（不要将个人代理地址写入提交的公共配置）。
JVM 的代理需通过 Gradle systemProp.http/https.proxyHost、proxyPort 设置；git 的代理独立配置。

## 4. 代码位置

- `design/.../model/BackgroundTheme.kt`：背景枚举及遮罩值。
- `design/.../store/UiStore.kt`：持久化背景设置。
- `design/.../AppSettingsDesign.kt`：设置控件及请求。
- `app/.../AppSettingsActivity.kt`：系统图片选择器、导入、恢复默认。
- `app/.../ThemeBackground.kt`：大小限制、采样、原子写入、共享图片缓存、背景容器。
- `app/.../BaseActivity.kt`：在主要界面安装背景。
- `design/src/main/res/values*/background_strings.xml`：中英文文案。

路径中的 `...` 表示 `src/main/java/com/github/kr328/clash`（design 模块另接 `/design`）。

## 5. 签名与正式打包

此工作区生成的专用密钥在 `.private/themes-release.jks`，密码备份在 `.private/signing-backup.json`。
它们被 Git 忽略；请自行保存离线备份。更新同包名应用需要继续使用同一密钥。
禁止用上游仓库内公开的 release.keystore 签发正式分发包。

新电脑可交互创建自己的密钥（不要在命令行明文输入密码）：

```powershell
keytool -genkeypair -keystore themes-release.jks -alias themes -keyalg RSA -keysize 3072 -validity 10000
```

在根目录新建被忽略的 signing.properties：

```properties
keystore.path=.private/themes-release.jks
keystore.password=你自己的密码
key.alias=themes
key.password=你自己的密码
```

```powershell
.\gradlew.bat --no-daemon :app:assembleMetaRelease :app:lintMetaRelease
Get-ChildItem app\build\outputs\apk\meta\release\*.apk
```

正式包名默认 `com.github.wodecj.clash.themes.meta`，可与官方版并存。
升级时保持包名和签名，递增 build.gradle.kts 中 versionCode 与 versionName。
不可用本版直接覆盖官方 APK，因为签名不同。

## 6. 推荐：GitHub Actions 打包

仓库 Actions → Themes APK → Run workflow。推送 main 也会构建。
已为此仓库配置两个 Actions Secrets：

- `THEMES_KEYSTORE_BASE64`：专用 keystore 的 Base64。
- `THEMES_KEYSTORE_PASSWORD`：密码，key alias 固定 themes。

新仓库可用下面的命令配置（需要已登录 gh，敏感值不会写入源码）：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path '.private\themes-release.jks'))) | gh secret set THEMES_KEYSTORE_BASE64
# gh 交互提示中输入密码
gh secret set THEMES_KEYSTORE_PASSWORD
gh workflow run themes-apk.yml
gh run list --workflow themes-apk.yml
gh run download <run-id> -n ClashMeta-Themes-Android -D dist
```

产物包括 APK、完整对应源码（含固定提交的 Mihomo）、许可证、修改说明、提交号与 SHA-256。
Artifacts 默认保存 30 天，下载通常要求 GitHub 登录；给普通用户应发布到 Releases。

## 7. 发布及手机下载安装

真机验收完成后执行；tag 必须指向构建时的 SOURCE_COMMIT.txt 提交：

```powershell
gh release create v2.11.36-themes.4 --target <构建提交SHA> --title 'Clash Meta Themes 1' --notes-file MODIFICATIONS.md dist\*.apk dist\corresponding-source.tar.gz dist\LICENSE dist\NOTICE dist\SHA256SUMS.txt dist\SOURCE_COMMIT.txt
```

确认仓库为公开后，分享 Releases 页：https://github.com/Wodecj/ClashMetaForAndroid/releases 。
手机打开发布页 → Assets → 下载 arm64-v8a 或 universal APK → 为下载所用浏览器允许安装未知应用 → 安装。
GitHub 发布页需要用户网络可达。不要把 AAB 当成可直接安装文件；本流程直接分发 APK。
Google Play 是另一条发布渠道，需要开发者账户、当前目标 SDK / VPN 政策及隐私申报审核，本流程不代表已满足上架条件。

## 8. 许可证与源码交付

根 LICENSE 为 GPL v3；保留原版权、NOTICE、应用中的许可说明，修改文件注明修改者和日期。
本次新增修改按 GPL-3.0-or-later 提供，分发整个衍生应用须遵守原 GPL 条款。
分发 APK 时，在同一下载位置提供对应版本的完整可构建源码、子模块和构建脚本、许可文本；工作流已生成源码包。
GitHub 自动生成的 Source code.zip 不含子模块，不能代替本流程的 corresponding-source.tar.gz。
保留上游依赖各自的许可，发布前也要核对内核、规则数据及所有后续新增资源的许可。
图片由用户自行选择；项目不分发第三方壁纸，也不获得官方商标授权。

官方依据：
- https://github.com/MetaCubeX/ClashMetaForAndroid
- https://www.gnu.org/licenses/gpl-3.0.html （第 5、6 节）
- https://developer.android.com/studio/publish/app-signing

## 9. 发布前真机验收

- 四种内置模式与自选图片；取消选择、损坏图片、超过 20 MiB、删除原相册图片、重启进程。
- 遮罩 0%、50%、100% 及拖动预览、松手保存、旧版设置迁移与浅色/深色，首页/设置/配置/代理/日志页面、横竖屏、大字体。
- Android 5/7/10/13/15 中实际覆盖的版本，尤其图片选择器与 VPN 授权。
- 导入订阅、启动/停止 VPN、节点切换、后台服务和通知；背景改动不应影响代理行为。
- 安装首版，再用相同签名、更大 versionCode 覆盖升级。

未连接手机或模拟器时，编译与 lint 通过不等于已完成上述运行时验收。

## 10. 启动更新检查

从 themes.3 起，打开主界面约 1.5 秒后异步请求本仓库的 GitHub Releases Latest API。
仅提示更高版本的正式 Release，忽略草稿、预发布、旧版本以及没有适配 APK 的发布。
同一进程最多每分钟请求一次、同一版本每次进程启动最多弹一次；断网、超时、GitHub 限流时静默失败，不阻止代理功能。
“明天提醒”延后该版本 24 小时，“忽略此版本”不再提示该版本，之后的新版本仍可提示。
“下载更新”按手机架构选择本仓库 HTTPS APK 链接并交由浏览器下载，安装仍需用户确认。

发布新版本时必须递增版本号并沿用 `v2.11.36-themes.4` 这样的四段编号格式，上传 APK 后发布为正式 Latest。
先手动安装 themes.3 或更高版本才能使用更新提醒；旧 APK 不会自动获得此功能。
更新检查不发送订阅和代理配置，但正常网络请求会让 GitHub 收到 IP 地址及应用 User-Agent。

实现：`app/.../update/ReleaseUpdate.kt`、`AppUpdatePrompt.kt`，在 MainActivity 的 onResume/onStop 中管理检查及弹窗生命周期。
单元测试：`app/src/test/java/com/github/kr328/clash/update/ReleaseUpdatesTest.kt`。
执行：`.\gradlew.bat :app:testMetaReleaseUnitTest :app:assembleMetaRelease :app:lintMetaRelease`。
