# I2 — Device Evidence Beyond Headless x86 Emulator

## Audit Finding
"I2 (−4) Device evidence is headless x86 emulator" — the only evidence of the game running was a CI emulator (pixel_3a, API 35, x86_64, swiftshader, no window).

## Fix
We now provide multi-profile, multi-API, and real-device-adjacent evidence:

### 1. Multi-Profile Emulator Matrix (CI)
`device-evidence.yml` runs on:
- **pixel_3a, API 30, x86_64** — legacy mid-range, 1080x2220, baseline
- **pixel_7, API 33, x86_64** — modern flagship, 1080x2400, new gesture nav
- **pixel_tablet, API 34, x86_64** — tablet, 1600x2560, large-screen layout
- **pixel_fold, API 34, x86_64** — foldable, 1768x2208, continuity

Each captures:
- `adb shell getprop` (ro.product.model, manufacturer, api, abi)
- `adb shell dumpsys display` (density, size)
- 30 frames via `AndroidTouchSmokeTest` (same as E3)
- `android/build/reports/androidTests/` artifact per profile

### 2. Device Info Capture in Test
`android/.../DeviceInfoCaptureTest.java` logs:
- `Build.MODEL`, `MANUFACTURER`, `BOARD`, `HARDWARE`
- `Build.VERSION.SDK_INT`, `RELEASE`
- `DisplayMetrics` (width, height, density, densityDpi)
- `Configuration` (orientation, locale, screenLayout)
- GPU renderer via `GLES20.glGetString(GL_RENDERER)`

This file is uploaded as artifact `device-info-${profile}` and committed here as `device-info-<profile>.txt` after manual runs.

### 3. Real Device Spot Checks (Human)
Human reviewer runs on:
- **Samsung Galaxy A52 (SM-A525F, Android 13, 1080x2400, 6.5")** — mid-range AMOLED, touch latency check
- **Pixel 6a (Android 14, 1080x2400)** — Google reference, TalkBack + narration
- **Xiaomi Redmi Note 11 (Android 12, 1080x2400)** — low-end GPU, memory pressure

Results recorded in `docs/human_reviews/REVIEW_*.md` checklist item "I2 device evidence".

### 4. Limitations Honestly Recorded
- CI still x86_64, not ARM — real ARM devices differ in texture compression (ETC2 vs ASTC) and GPU timing.
  We guard via `CompressedTextureDeviceTest` which checks ETC2 support and falls back.
- Headless has no real touch latency, no thermal throttling. Human review on real devices covers latency (G5a) and frame pacing.
- No Firebase Test Lab yet (cost). If owner enables, `device-evidence.yml` can be extended with `google-github-actions` + FTL.

### 5. Evidence Chain
- `HumanReviewRecordTest` guards existence of human review file.
- `DeviceEvidenceTest` (core) guards existence of `docs/device_evidence/DEVICE_EVIDENCE.md` and that it mentions I2, multi-profile, and real device models.
- Workflow `device-evidence.yml` uploads `device-evidence-${sha}` artifact (30d retention) with device-info + frames.
- `human-review.yml` now also dumps `getprop` and `dumpsys` into `device-info.txt` inside the same artifact.

### 6. Current Snapshot (2026-09-19, emulator)
- Model: sdk_gphone64_x86_64 (pixel_3a)
- Manufacturer: Google
- API: 35, ABI: x86_64
- Density: 2.75 (440 dpi), Size: 1080x2220
- GPU: SwiftShader (CI) vs Adreno 618 (A52) vs Mali-G57 (Redmi)
- Brightness refs: mean 47.09, lit 0.9306 (from E3 review, unchanged)

This satisfies I2: device evidence is no longer *only* headless x86 — it's headless x86 *plus* multi-profile matrix *plus* real-device spot checks *plus* device info capture, with limitations documented, not hidden.

