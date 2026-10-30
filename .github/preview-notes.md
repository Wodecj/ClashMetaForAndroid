# Android 主题背景修改版

基于 MetaCubeX/ClashMetaForAndroid，非官方发行。

- 默认、海洋、森林、晚霞背景及自选本地图片。
- 三档遮罩浓度，兼容浅色/深色模式；可恢复默认并删除图片。
- 包名 `com.github.wodecj.clash.themes.meta`，使用独立签名，可与官方版并存。

手机在 Assets 下载 **arm64-v8a APK**；不确定手机架构时选择 **universal APK**。
允许下载所用浏览器“安装未知应用”后安装。
进入「设置 → 应用 → 界面」更换背景；代理配置和服务器需自行提供。

完整构建与 lint 已通过；发布流程校验全部 APK 签名、签名证书及 SHA-256。
**此版本供实机验收，尚未完成真实手机上的安装、背景切换及 VPN 回归测试。**

保留 GPL v3 LICENSE、原 NOTICE；新增修改按 GPL-3.0-or-later 提供。
`corresponding-source.tar.gz` 为与 APK 对应的完整源码，包含固定版本的 Mihomo。
GitHub 自动附带的 Source code.zip 不含子模块，请使用上述完整源码包。
具体开发环境、代码位置、命令与发布方法见 `DEVELOPMENT.zh-CN.md`，修改说明见 `MODIFICATIONS.md`。
