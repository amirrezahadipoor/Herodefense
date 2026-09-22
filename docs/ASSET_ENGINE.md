# Asset Engine (Phase 28 — premium-v2)

> **Status (2026-09-16).** The authoritative gate list is the one the validator prints when it runs: it
> reports its *measured* gates separately from its *config-presence* checks, so a check that only greps the
> pipeline source is never counted as a quality gate (roadmap R1.6). Numbers in this document are the
> contract; the values that actually ship are in `android/assets/generated/asset_manifest.json`.


How Hero Defense art is produced, reviewed, and promoted. Pipeline code lives
in `tools/blender` (procedural scenes + EEVEE/Workbench rendering) and
`tools/visual` (review sheets, promotion, validation). Every promoted PNG
is an opaque contract: its manifest records the exact engine that built it.

## 28.0 Headless-Blender spike — verdict: SPLIT GO (2026-09-15)

Question: can the EEVEE pipeline render in a clean GPU-less sandbox?

| Probe | Result |
|---|---|
| Blender 4.2.23 starts headless (user-space syslibs, no sudo) | ✅ GO |
| `BLENDER_WORKBENCH` 1-frame render (llvmpipe) | ✅ GO (~5 s, verified PNG) |
| `BLENDER_EEVEE_NEXT` 1-frame render, bare `--background` | ❌ NO-GO — `EGL Error (0x3009): EGL_BAD_MATCH` after ~2 min of shader compile |
| `BLENDER_EEVEE_NEXT` under `xvfb-run` (+ user-space Xvfb/XKB) | ❌ NO-GO — same `EGL_BAD_MATCH`, then killed |
| CI `generate-visual-assets.yml` (ubuntu-latest + xvfb) | ✅ GO — 27 runs on record, recent all green |

Sandbox recipe (reproducible, no sudo): `scripts/install-blender-temp.sh`
with `HERO_TOOLS_ROOT` on a large disk (`/tmp` on the CI runner is a 1 GB tmpfs, `env:ci-tmp-disk`, and does
NOT fit Blender), plus `apt-get download` + `dpkg-deb -x` of `libxkbcommon0`
libx11-6 libxi6 libxxf86vm1 libxfixes3 libxrender1 libgl1 libegl1 libsm6
libice6 libxext6 (libxau6 libxdmcp6 libbsd0 libmd0) into a sysroot on
`LD_LIBRARY_PATH`, with `LIBGL_ALWAYS_SOFTWARE=1`.

Production-path decision (locked by this spike):

1. **EEVEE categories** (characters, bosses, trees, arena, equipment, vfx
   look-dev) render via CI `generate-visual-assets.yml`
   (`workflow_dispatch` → batch), or on any GPU machine — never in this
   sandbox.
2. **In-sandbox production** = pipeline code + validation + review-sheet
   upgrades, `BLENDER_WORKBENCH` categories (gear overlays today), and 2D
   VFX/icon/item art through the same review → validate → promote flow.
3. Every batch, whatever its renderer, must pass `validate_generated_assets.py`
   and a human-visible review sheet before promotion.

---

## 28.0b The arena's cover (2026-09-22)

The four fields the arena round added place two kinds of outcrop -- one that stops bodies and arrows, one that
stops only bodies -- and both used to be drawn with the crystal landmarks. A landmark says "this is the edge of the
arena"; cover has to say the opposite, and it has to say which of the two kinds it is from across the field,
because that difference is a rule the player is playing against.

So the arena batch now renders the cover itself: four families, three variants each, from the same stone sheet as
the rest of the arena.

| family | kind | construction | why it reads as its own thing |
| --- | --- | --- | --- |
| `standing_stone` | shelter | tapered monolith, chiselled bands, capstone, flanking rock, moss footing | the only family that is one tall column |
| `ruin_slab` | shelter | leaning slab, broken course, rubble bed, ring memory | leans, so its shadow is longer on one side |
| `thorn_hedge` | low | arched branches, thorn rake, moss clumps, low mound | wider than it is tall, and pointed |
| `mossy_boulder` | low | rounded core, shoulder facet, lichen plates, scattered pebbles | a single mass, no points at all |

Two contracts are enforced rather than described: `tools/blender/tests/test_arena_obstacle_source.py` holds the
standing families to at least twice the authored height of the low ones and refuses two families that build the
same mix of primitives (a family is a construction, not a recolour), and the review sheet
`arena_obstacle_lineup.png` re-measures that separation on the painted pixels, because that is what a player
compares across the field. Cover never carries `runtimeGlow`: emissive light is the vocabulary this game reserves
for the crystals, and a rock that lit up would be claiming to be a landmark.

## 1) How to run the pipeline

### 1.1 Prerequisites

- **Blender 4.2.23** (checksum-pinned, see `tools/blender/hd_pipeline/config.py: BLENDER_VERSION`).
  Never commit the binary — install to a disposable cache:
  ```bash
  export HERO_TOOLS_ROOT=/tmp/herodefense-tools   # large disk, not the 1 GB tmpfs (`env:ci-tmp-disk`)
  bash scripts/install-blender-temp.sh
  export PATH="$HERO_TOOLS_ROOT/blender-4.2.23/blender:$PATH"
  ```
  The script also vendored the X11/EGL sysroot (libxkbcommon, libGL, libEGL …) and sets `LIBGL_ALWAYS_SOFTWARE=1`
  for the Workbench path.

- **Python 3.10+** with `Pillow` for review/validation image checks:
  ```bash
  python3 -m pip install --break-system-packages pillow
  ```

- **Disk:** `/tmp` on the CI runner is only ~1 GB (`env:ci-tmp-disk`) — the Blender install alone is over
  400 MB (`env:blender-install`).
  Always set `HERO_TOOLS_ROOT` (and `GRADLE_USER_HOME`, `HERO_PROJECT_CACHE_DIR`) to a
  cache outside the repository.

### 1.2 Batch matrix

| Batch name | What it renders | Frame class | Category | Renderer |
|---|---|---|---|---|
| `pilot` | Hero + one enemy | character | pilot | EEVEE (CI) |
| `premium-pilot` | 6 premium hero variants | character | pilot | EEVEE (CI) |
| `characters` | Rootling, Stonekin, Gloom Wolf, Fungal Brute | character | enemies | EEVEE (CI) |
| `enemies` | Same as characters (legacy alias) | character | enemies | EEVEE (CI) |
| `bosses` | Ancient Golem, Thorn Matriarch, Ember Wyrm, Void Knight | boss | bosses | EEVEE (CI) |
| `world-tree` | Healthy, Damaged, Sapling | tree | world-tree | EEVEE (CI) |
| `equipment` | 41 equipment items (incl. icons) | character/item | equipment | Workbench (in-sandbox) + EEVEE for hero-socket |
| `equipment-overlay` | 14 hero-worn boots+weapon subset | character | equipment_overlay | Workbench |
| `arena` | Portrait backdrop 720×1280 + depth bands | arena | arena | EEVEE (CI) |
| `environment` | Ground tiles ×3, crystal props ×3 | environment | arena/environment | EEVEE (CI) |
| `ui` | 16 icons + 12 skin states (button/panel/slot ×4 states) | item | ui | 2D Workbench/vector |
| `ui-supplement` | 6 potions + 2 missing reward semantics | item | ui-supplement | 2D |
| `skill-icons` | 5 skill glyphs | item | skill-icons | 2D |
| `ceremony` | Planting ceremony hero + sapling growth | character/tree | ceremony | EEVEE (CI) |
| `vfx` | Impact flash, shockwave ring (8-frame strips) | vfx | vfx | EEVEE (CI) |
| `projectile` | Arrow shaft/head/fletching (64×64) | projectile | projectile | EEVEE/Workbench |

### 1.3 Running the pipeline

**On CI (preferred for EEVEE):**
1. Push your `tools/blender` changes to `main`.
2. In GitHub Actions, run **Generate visual assets** (`.github/workflows/generate-visual-assets.yml`) → *Run workflow* → pick `batch` from the dropdown → *Run*. The job installs Blender in the large cache, renders headlessly via `xvfb-run`, and uploads the artifact `generated-<batch>.zip`.

**On a GPU machine (local):**
```bash
blender --background --factory-startup --python tools/blender/generate_assets.py -- \
  --batch <name> --output /tmp/herodefense-generated/<name>

# Examples:
blender --background --factory-startup --python tools/blender/generate_assets.py -- --batch bosses --output /tmp/herodefense-generated/bosses
blender --background --factory-startup --python tools/blender/generate_assets.py -- --batch equipment-overlay --output /tmp/herodefense-generated/equipment-overlay
blender --background --factory-startup --python tools/blender/generate_assets.py -- --batch all --output /tmp/herodefense-generated/all
```

**In-sandbox dry run (no EGL required, Workbench only):**
```bash
# Only batches that use BLENDER_WORKBENCH (equipment-overlay, ui, etc.) can complete here.
# The EEVEE batches will correctly fail with EGL_BAD_MATCH — that is the expected SPLIT GO behavior.
blender --background --factory-startup --python tools/blender/generate_assets.py -- --batch equipment-overlay --output /tmp/herodefense-generated/equipment-overlay --isolate-frames
```

### 1.4 Output contract

Every run writes to `<output>/`:

```
asset_manifest.json   # top-level: pipelineVersion, engineVersion, renderTierTop, batch, assets[]
sprites/*.png + .atlas
equipment/*.png + .atlas
icons/*.png
vfx/*.png + .atlas
projectile/*.png + .atlas
```

The top-level manifest records the **engine version per batch** (see §3). Per-asset entries record `visualQuality`, `renderSupersample`, `renderSamples`, `modelRevision`, `rigProfile`, `triangles`, `pivot`, `frameSize`, and `clips`.

---

## 2) Review → Validate → Promote

No PNG is committed without passing all three gates in order. The pipeline is intentionally sequential — a bad batch fails fast.

### 2.1 Review sheets (28.2)

```
python3 tools/visual/create_<category>_review.py --input /tmp/herodefense-generated/<batch> --output docs/art_reviews/<REVIEW>.png
```

Each sheet exercises **four appeal views per sprite** (§12 shape language) and **five grade variants** (§12.4):
- `FULL | 50% | GRAY | SHAPE` — the 50% view is nearest-neighbor (runtime pixels, no smoothing), grayscale uses luma, silhouette is solid `#E8F3E8` keeping original alpha.
- `BASE | DAWN 1-50 | AMBER 51-100 | TEAL 101-150 | HOLLOW 151-200` — Dawn is identity (same as BASE), Amber warms, Teal cools, Hollow darkens + lifts teal shadows; all preserve alpha exactly.

Generated sheets live in `docs/art_reviews/` and are hash-bound into the promotion audit (see promote scripts). The 28.2 upgrade guarantees every batch has a silhouette-only column and a grade strip.

### 2.2 Validation (28.5)

```
python3 tools/visual/validate_generated_assets.py <output-or-android/assets/generated>
```

The validator is the **CI fail-fast gate** — it runs in both `test-core.yml` and `build-android.yml` on every push, *before* Gradle assembles the APK.

Checks (automatable where Pillow is available, manifest-only otherwise):

| Check | What it asserts | Automatable |
|---|---|---|
| `pipelineVersion ≥ 3` | premium-v2 output | manifest |
| `renderSupersample == 2` (or `3` for top-tier) | Working-render scale | manifest |
| `opaqueRenderSamples ≥ 16`, `overlayRenderSamples ≥ 8` | Sample floors (top-tier → 32 / 24) | manifest |
| `pipelineVersion`, `blenderVersion`, `engineVersion` present | Provenance | manifest |
| `pivot.units == normalized-bottom-left` + range 0–1 | Pivot contract | manifest |
| **Pivot stability** | `x≈0.5±0.06` for all; `y≈0.12±0.04` for character/boss, `0.06±0.04` for tree, `0.5±0.04` for arena/environment/item/vfx/projectile | manifest |
| **Edge safety** | No opaque pixel (α>10) touches the 1px inner border of any frame; exempt `arena_backdrop` (full-bleed) | image (Pillow) |
| **Silhouette coverage** | Opaque ratio (α>20) within `[0.002, 0.95]` general, tighter per-family: equipment `0.002–0.20`, sapling `0.002–0.60`, character/boss/tree/item/env `0.02–0.85`; catches empty/full/bleed batches | image |
| **Grade / silhouette alpha** | Every `STAGE_GRADE` + `silhouette_view` + `grayscale_view` preserves alpha exactly | image (review_strips) |
| `alphaMode == STRAIGHT_RGBA`, `bit_depth==8`, `color_type==6` | Texture contract | image header |
| Frame & page geometry, `decodedBytes`, page size ≤2048 | Atlas integrity | manifest + header |
| Icon 96×96 RGBA | Equipment icons | manifest + header |
| `decodedCatalog` within `decodedCatalogBudgetBytes` (525,000,000 bytes in the shipped manifest, raised from 390,000,000 on 2026-09-17 at the owner's direction so the genuine master renders of R5.2 fit; the composed catalog measures **518,959,104 bytes** -- 6,040,896 of headroom -- in `perf:2026-09-17-composed-tier-residency`, and the same run measures the live combat set at 207,765,504 bytes against the raised `decodedCombatResidencyBudgetBytes` of 209,715,200; arithmetic in `code:main/java/com/amirrezahadipoor/herodefense/render/RuntimeResidency.java`) | Memory budget | manifest, enforced by core `RuntimeResidencyTest` |
| No undeclared/missing PNGs | Manifest completeness | filesystem |

If Pillow is not installed the validator runs in manifest-only mode and prints `Pillow not available — skipped image-level checks`.

### 2.3 Promotion

```
python3 tools/visual/promote_<batch>_batch.py --source /tmp/herodefense-generated/<batch> --destination android/assets/generated
```

Each promote script:
- Re-validates the candidate manifest (tier floors, model revisions, engineVersion).
- Verifies hash-bound review evidence: `docs/art_reviews/<BATCH>_REVIEW.md` contains `**Decision:** ACCEPTED` plus the SHA256 of both the candidate manifest and the review audit.
- Copies PNGs/atlases/icons into `android/assets/generated/<family>/`, merges the candidate `assets[]` into the committed `asset_manifest.json` (replacing only that batch's keys, sorted), and is idempotent — running twice leaves the tree unchanged.

**Do not hand-copy PNGs.** Promotion is the only path into `android/assets/generated`.

---

## 3) Engine versioning

Every batch is version-stamped so `28.7` can prove no old-engine asset remains.

### 3.1 Top-level manifest

```json
{
  "pipelineVersion": 3,
  "blenderVersion": "4.2.23",
  "engineVersion": "28.7-premium-v2-3x32",
  "styleGuide": "docs/VISUAL_STYLE_GUIDE.md",
  "renderTierTop": [3, 32],
  "renderTierTopClasses": ["boss", "tree"],
  "renderTierTopKeyPrefix": "hero",
  "generatedBatch": "bosses",
  "generatedAt": "2026-09-15T14:00:00Z",
  "generatedCommit": "ac47254…",
  "assets": [ … ]
}
```

- `engineVersion`: `<roadmap-phase>-premium-v2-<supersample>x<samples>` (e.g., `28.6-premium-v2-2x16`, `28.7-premium-v2-3x32`). Bumped whenever `tools/blender/hd_pipeline/config.py` (renderTier, FRAME_SIZE, samples) or `docs/VISUAL_STYLE_GUIDE.md` palette/shape changes.
- `generatedAt` / `generatedCommit`: ISO-8601 UTC timestamp and the `git rev-parse HEAD` at generation time — the audit appendix for 28.7.
- `assets[].visualQuality`: always `"premium-v2"` for the 28.x series; top-tier assets also carry the higher `renderSupersample`/`renderSamples`.

### 3.2 Per-asset record (excerpt)

```json
{
  "key": "ancient_golem",
  "family": "boss",
  "frameClass": "boss",
  "visualQuality": "premium-v2",
  "renderSupersample": 2,
  "renderSamples": 16,
  "modelRevision": "heartstone-colossus-v2",
  "rigProfile": "premium-heavy-humanoid-v2",
  "pivot": {"units": "normalized-bottom-left", "x": 0.5, "y": 0.12}
}
```

After 28.7, every `assets[]` entry has `engineVersion` inherited from the top-level (or duplicated per-asset for the audit grep). The **audit command** for 28.7 is:

```bash
python3 tools/visual/validate_generated_assets.py android/assets/generated
python3 -c "
import json; m=json.load(open('android/assets/generated/asset_manifest.json'));
assert m.get('engineVersion','').startswith('28.7'), 'not 28.7';
print(f\"engine {m['engineVersion']}  batch {m['generatedBatch']}\");
bad=[a['key'] for a in m['assets'] if a.get('visualQuality')!='premium-v2'];
assert not bad, f'old quality {bad}';
# tier check
from tools.blender.hd_pipeline.config import render_tier
for a in m['assets']:
  ss, sm = render_tier(a['key'], a['frameClass'])
  assert (a['renderSupersample'], a['renderSamples']) == (ss, sm), f\"{a['key']} tier mismatch\"
print(f\"{len(m['assets'])} assets all on engine 28.7 tier-correct\")
"
```

If any old-engine asset remains, the validator and the tier check fail.

### 3.3 Version history (locked)

| Engine | Date | Change |
|---|---|---|
| `28.0-SPLIT-GO` | 2026-09-15 | Headless-Blender spike verdict, SPLIT decision |
| `28.3-premium-v2-2x16/3x32` | 2026-09-15 | Raise working-resolution/sample floors per category (hero/bosses/trees → 3×32) |
| `28.4-premium-v2-categories` | 2026-09-15 | Add vfx/projectile/equipment-overlay categories + atlas gates |
| `28.5-premium-v2-validate` | 2026-09-15 | Edge safety + pivot stability + silhouette + grade checks, CI fail-fast |
| **`28.6-premium-v2-runbook`** | **2026-09-15** | **This runbook + engineVersion stamping** |
| `28.7-premium-v2-3x32-full` | *next* | Full re-render on the finished 28.6 engine, no old-engine asset remains |

---

## 4) Accept / Reject checklist (human gate)

Use this checklist on every review sheet before promotion. A single **REJECT** blocks the batch; the artist fixes the source and re-renders.

### 4.1 Appeal (style guide §12–13)

- [ ] **Real size:** subject reads instantly at its in-game pixel size on a mid-range phone (no squinting).
- [ ] **50% scale:** silhouette still distinct at half size (nearest-neighbor, no blur hides it).
- [ ] **Grayscale:** forms separate by value alone; no two materials collapse to the same gray.
- [ ] **Silhouette:** pure `#E8F3E8` shape is instantly recognizable — hero = archer with bow, boss = tri-lith crest, tree = layered foliage.
- [ ] **Light / dark checker:** no lost edges on either background; outline `#142126` holds.
- [ ] **Palette 60/30/10** — broad dark forest masses, local-color forms, restrained gold/rarity accents.
- [ ] **Material read** — wood / cloth / skin / stone / metal / crystal / glass distinguishable by value + highlight, not micro-noise.
- [ ] **Rarity language** — Common matte, Uncommon slight emissive, Rare glow, Legendary particle-glow tier — shader change, never baked into the albedo.

### 4.2 Color grading (guide §12.4)

- [ ] `BASE` and `DAWN 1-50` are identical (reference).
- [ ] `AMBER 51-100` warms reds, cools blues by the locked recipe.
- [ ] `TEAL 101-150` cools, `HOLLOW 151-200` darkens + lifts teal shadows without hue-shifting outlines.
- [ ] No banding, no alpha change, no clipping to 0/255 on mid-tones.

### 4.3 Technical (validator mirrors this)

- [ ] Edge safety: no opaque pixel touches the 1px frame border (arena_backdrop exempt).
- [ ] Pivot stability: `0.5, 0.12` for character/boss, `0.5, 0.06` for tree, `0.5, 0.5` otherwise, within tolerance.
- [ ] Silhouette coverage not empty (<0.2%) nor full (>95%) for non-arena.
- [ ] Page ≤2048px, `decodedCatalog` within the manifest budget (390 MB, `perf:2026-09-17-residency`), icons 96×96 RGBA straight.
- [ ] `visualQuality` and `engineVersion` equal what the shipped manifest declares and the validator accepts (`studio-v3`, `78.0-integrity-recovery-runtime-tier` at the time of writing).
- [ ] Review sheet hash-bound: markdown contains `**Decision:** ACCEPTED` and the SHA256 of both manifest and audit, and all sheet PNGs are byte-identical to the audit record.

### 4.4 Production hygiene

- [ ] `.blend` not committed at all (`tools/blender/*.py` is the source of truth; a committed blend would also be a
  multi-megabyte blob in a repository that keeps its payload under 30 MB, `budget:apk_budget.json`).
- [ ] No SDK/Blender/cache in `android/assets/generated` — only PNG/atlas/json/manifest.
- [ ] `validate_generated_assets.py` green on both `test-core` and `build-android` CI before merge.

**Verdict:** `ACCEPTED` only if all boxes above are green. `REJECTED` lists the failing gate(s) and the fix (e.g., "Golem fist clips at 50% — widen silhouette, re-render hero_attack 03–05").

---

## 5) CI wiring

- `test-core.yml` → `Validate generated assets (edge safety, pivot stability, silhouette + grade — fail fast)` — runs *before* `:core:test`.
- `build-android.yml` → same validation — runs *before* `:android:assembleDebug`.
- `generate-visual-assets.yml` → `workflow_dispatch` batch picker, installs Blender to `$HERO_TOOLS_ROOT`, `xvfb-run blender --background …`, uploads `generated-<batch>.zip`.
- Any validation failure aborts the workflow in <10 s — no APK is built, no wasted emulator minutes.

---

## 6) Troubleshooting

| Symptom | Fix |
|---|---|
| `EGL_BAD_MATCH` on `--background` EEVEE | Expected in the sandbox — use CI or a GPU machine (SPLIT GO). |
| `pipelineVersion < 3` | Re-run with the current `generate_assets.py` (pipeline 3 = premium-v2). |
| `pivot unstable` | Check `_pivot_for()` in `generate_assets.py` — per-class pivot must match §2.2 table. |
| `edge safety violation` | Add 1–2 px transparent padding in the Blender camera margin or re-pack with 1px gutter in `atlas_layout.py`. |
| `silhouette coverage` empty/full | The model is off-camera or fills the frame — re-center the rig at `(0,0,1.15)` and re-check `CAMERA_TARGET`. |
| `decodedCatalog exceeds budget` | The batch's page count grew — re-pack with `atlas_layout.py` `maxAtlasPageSize=2048` or split the batch. |
| `undeclared PNG` | A hand-copied PNG slipped in — delete and re-promote via the promote script only. |

---

## 7) Texture compression (R8.1)

The catalog ships as PNG unless a sheet clears the encoder's gate, in which case the container beside it ships
too and a device that decodes ETC2 reads that instead. The encoder is first-party, the containers are KTX v1, and
every number below can be re-taken with the commands at the end of this section.

**The encoder.** `tools/texture/etc2.py` implements ETC2_RGB8 and the punchthrough format. Its punchthrough colour
layout is the one Google's `swiftshader` decoder reads, and that decoder's own output for two streams this encoder
wrote is committed as `tools/texture/tests/driver_vectors.py` -- so a layout this repository gets wrong fails a
test rather than failing on a device. The base colours of a half are searched *together across the three
channels*, because the two-bit index is shared between them; `JOINT_LEVEL_RADIUS` is how wide that search looks,
and zero (two levels per channel, eight combinations) is the shipped width.

**What was measured on 2026-09-17** (`perf:2026-09-17-texture-encoding-sweep`): 111 sheets, colour half 25.09 dB
worst, 33.01 dB median, 40.51 dB best; Google's `etc1` on the same pixels 32.28 dB median, which this encoder
beats on **111 of 111** sheets. 34 sheets are masks (0.995 or more of their alpha is already 0 or 255) and 16 of
those clear the 32 dB bar, so **16 containers ship** -- 11,520,000 encoded bytes standing in for 92,160,000
bytes of RGBA8888 on the sheets that take them (`perf:2026-09-17-texture-encoding-sweep`, which also carries the per-sheet line):
one backdrop and fifteen equipment sheets, the worst of them round-tripping at 32.07 dB and the best at
40.36. The other 95 stay PNG and the report names the reason for every one of them: 77 are under the alpha
gate -- a sheet whose alpha is softer than 0.995 would have up to 6 % of its pixels hardened into a mask by
the format, and the twelve combat sheets sit at 0.9369 to 0.9936 (`perf:2026-09-17-texture-encoding-sweep`) -- and 18 are masks
whose colour round trip is under the bar, 25.6 to 30.9 dB (`perf:2026-09-17-texture-encoding-sweep`). No mip chain ships yet -- the containers hold one level, so this format's mipmap clause is still
open.

**The container and the loader.** `tools/texture/ktx.py` writes KTX v1 and `encode_textures.py` puts a sheet's
container beside it under `compressed/etc2/`, which is the path `TexturePayloadPolicy.containerPath` predicts and
`AtlasPageSource.containerBeside` looks in -- pinned by a test that names both halves of the rule, because they
disagreed once. At runtime `SheetPayloads` is the only place a sheet becomes a texture: it asks
`AtlasPageSource` (which reads the container's own header and refuses one this device cannot decode, or one that
is not the sheet's size), then hands the file to libGDX's `KTXTextureData`. Both the page path (an atlas) and the
single-sheet path (a backdrop, an icon) go through it, and `HeroDefenseGame.create()` installs the answer once,
from the GL version string.

**The rules that keep it honest.** `tools/texture/tests/test_shipped_containers.py` re-derives the decision from
the bundle itself: every container must have the sheet it was encoded from, be accounted for in
`docs/perf/texture-encoding-report.json` with the same payload length, decode within the bar over the
pixels that show, and reproduce the sheet's mask exactly. A container that was hand-copied in, or left behind by
an older encoder, fails that test.

**Reproduce it.**

```bash
python3 tools/texture/encode_textures.py --combat --report-only                   # per sheet, no files written
python3 tools/texture/encode_textures.py --combat --reference ./etc1_reference    # with the reference beside it
python3 -m unittest discover -s tools/texture/tests                                # encoder, containers, bundle
```

The reference is Google's `etc1` (`etc1_utils.cpp`, Apache-2.0), and it is deliberately not vendored: the module
docstring of `encode_textures.py` names the URL and the two-line harness that calls `etc1_encode_image`, so the
comparison can be re-taken without shipping anyone else's code in this repository.

---

*This runbook is the single source of truth for how art moves from `tools/blender` into `android/assets/generated`. If the code and this document disagree, the code's validator wins — then update this document to match.*

