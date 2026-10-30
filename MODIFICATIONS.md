# Clash Meta Themes — changes by Wodecj, 2026-10-30

Based on MetaCubeX/ClashMetaForAndroid commit
`1a4f949213fcbb7f0c9e2a4a21138642d84b8f22`.
Mihomo submodule remains pinned to `3f27ef39f8c96bbfbc8b0b9bb10f5c9ef60149d8`.

- Added default/ocean/forest/sunset/custom local-image activity backgrounds.
- Added persistent theme settings, 70/85/95% readability overlays, reset and local image deletion.
- Image import is limited to 20 MiB, decoded with sampling, and saved atomically in private app storage.
- Changed default application ID to `com.github.wodecj.clash.themes` (meta flavor adds `.meta`).
- Fork version: 2.11.36-themes.1, version code 211037.
- Release signing requires a private keystore path; removed debug-key fallback for release builds.
- Fixed upstream CMake version detection for detached/shallow checkouts and source archives.
- Replaced the upstream file browser back override with an AndroidX lifecycle callback to pass lint.
- Added a dedicated Android build workflow with source archive and SHA-256 checksums.
- Moved inherited automation to `.github/upstream-workflows/` for reference only.

Original LICENSE and NOTICE are retained. New changes are provided under GPL-3.0-or-later.
This is an independent modified distribution; it is not an official MetaCubeX release.
No background photos are bundled. Built-in gradients are defined in code.

Privacy addition: a selected background is processed only on the device, stored in the app's
private files directory and never uploaded by this feature. Reset deletes that local copy.
Existing upstream network and VPN behavior is described in PRIVACY_POLICY.md.

## Version 2 — 2026-10-30

- Replaced three overlay presets with a 0–100% slider (1% increments).
- Dragging previews the overlay across open screens without recreating activities; releasing saves the percentage.
- Added percentage labels and endpoint explanations in English and Simplified Chinese.
- Migrates old Soft/Balanced/Strong preferences to 70/85/95%; clamps saved percentages to 0–100.
- 0% applies no overlay; 100% fully covers the background. Cards and toolbars retain their own surfaces.
- Version name 2.11.36-themes.2, version code 211038, same application ID and private signing certificate for in-place upgrades.
