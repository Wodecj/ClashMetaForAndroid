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

## Version 3 — 2026-10-30

- Added asynchronous startup checks against this repository's GitHub Latest release API.
- Compares version numbers numerically, ignores drafts/previews/older versions and releases without a compatible APK.
- Prompts with release notes and download / remind tomorrow / skip this version actions.
- Chooses the device ABI APK with universal fallback; only accepts HTTPS download URLs belonging to this repository.
- Uses a one-minute in-process request cooldown, a per-session prompt limit, and five-second connect/read timeouts.
- Adds unit tests for release parsing, numerical ordering, ABI fallback and rejected downloads. No FlyClash/Diodos code was copied.
- Version name 2.11.36-themes.3, code 211039, same package and signing certificate.

Privacy addition: opening the main screen contacts api.github.com to check the public latest release.
No token, subscription contents or proxy configuration are sent. GitHub receives normal connection metadata (IP address and app User-Agent).
Downloading is initiated only after the user selects Download update and opens in the system browser.

## Version 4 — 2026-10-31

- Fixed wallpaper being covered by solid proxy node cards and the toolbar/tab strip.
- In non-default background modes these surfaces follow the 0–100% overlay slider; 0% removes unselected node surfaces.
- Keeps selected nodes visibly tinted (at least 65% fill) and adds contrast shadows to text at low concentrations.
- Refreshes surface opacity during live preview and on returning to an existing screen.
- Clips proxy rows to the scrolling content area so text does not overlap transparent toolbar tabs.
- Retains original surfaces when the default theme is selected. Added surface-alpha regression tests.
- Version name 2.11.36-themes.4, code 211040; same signing certificate and package.

## Version 5 — 2026-10-31

- Extended wallpaper transparency to home action cards and recycled profile cards; runtime-bound running/stopped colors retain the chosen alpha.
- Enforced toolbar transparency after layout; profile rows clip below the transparent toolbar.
- Added long-press vertical drag ordering of profile cards, saved locally by UUID and restored after refresh/restart.
- Keeps active profile identity unchanged, appends newly added profiles, ignores deleted/stale IDs.
- Defers profile dataset replacement while a drag is active; keeps the three-dot menu and normal activation clicks.
- Added profile ordering regression tests. Default background retains original solid surfaces.
- Version 2.11.36-themes.5, code 211041, same application ID and signing certificate.

## Version 6 — 2026-10-31

- Made the stopped home status card fully transparent, with zero card elevation, at every overlay concentration and in default background mode as well.
- Binds the real running state explicitly; only running status keeps the colored highlight.
- Removed the forced dark theme from the status card: stopped text/icons follow the page theme, running text/icons stay white.
- Updates text contrast and icon tint when the service state changes. Added stopped/running surface regression tests.
- Version 2.11.36-themes.6, code 211042; same package and signing certificate.

## Version 7 — 2026-10-31

- Added persistent background image fitting choices: Fit (show all, preserve aspect ratio), Fill (center crop), Stretch (fill without preserving aspect ratio).
- Defaults to Fit for new and existing installations without a saved fitting preference, so stored wallpapers no longer silently lose their edges.
- Existing private PNG files retain the full sampled image, so no reimport is needed. Unused screen areas use the page background color.
- Restoring default background also restores Fit. Existing opacity controls, status styles and profile ordering are unchanged.
- Version 2.11.36-themes.7, code 211043; same package and signing certificate.
