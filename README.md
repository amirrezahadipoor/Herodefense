# Hero Defense

Android-only single-hero action-defense game made with Java and libGDX.

## Modules

- `core`: platform-independent gameplay, simulation, persistence models, and tests.
- `android`: the only launcher, packaging target, and instrumentation-test target.

There are intentionally no desktop, iOS, or browser modules. Input is touch-only.

## Where the project stands

[`docs/RULES.md`](docs/RULES.md) is the seven standing rules the work is done under -- no fake evidence, no
weakened test, generated numbers, one item per commit pushed immediately, failures recorded, no completion
claim on prose alone, nothing designed around revenue. The roadmap those rules came from was deleted on
2026-09-18 at the owner's direction, along with its 1,511-line progress log; the work itself is in the tree and
the gates that keep it honest are the tests, `tools/` and the CI workflows.

The repository was measured three times against the same ten-category, 920-point rubric: **505** on 2026-09-16,
**731** on 2026-09-17, and **777** on 2026-09-18 at commit `bf3776e`. The roadmap's Gate 1 asks for ≥ 900, so
it is **not met**, and the missing points are itemised in the roadmap's Definition of Done.

The three audit documents were deleted on 2026-09-18 at the owner's direction, together with four superseded
documents that contradicted the current state, so that the repository stays small enough to migrate by hand
without an agent copying the wrong thing. The scores above are quoted from those documents; the counts behind
them are all still re-takable with `tools/audit/measure_round.py`, and the per-item evidence in the roadmap is
untouched.

## Build and test

```bash
./scripts/gradle.sh :core:test :core:ciStaticAnalysis   # the platform-independent game and its analyzers
python3 tools/audit/measure_round.py                    # every count the audits quote
```

The Android layer (`:android:assembleDebug`, the emulator journeys) builds in GitHub Actions; there is no
desktop, iOS or browser target to run the game on a workstation.

## What is heavy here, and how to copy this repository safely

Three directories hold almost all of the weight, and only one of them is the game:

| Path | Size | What it is | The rule |
|---|---|---|---|
| `android/assets/generated/` | 31 MB | the shipped sprites, atlases, KTX containers and `asset_manifest.json` | This is what goes in the APK. `docs/asset_hashes.json` and `AssetIntegrityTest` bind it; regenerate it only with the tools in `tools/visual/`, never by hand |
| `docs/art_reviews/` | 29 MB | 99 device-review images with 26 review records and 15 `surface_audit.json` files beside them | **Read-only evidence.** Twelve tests hash every image (sha256, byte size, width, height) against its own `surface_audit.json`, and 224 references to the review documents are baked into the shipped manifest. Do not move, rename, resize, re-encode or "tidy" anything here -- any of those breaks the build. Copy it verbatim or not at all |
| `docs/materials/` | 2.5 MB | the 39 material maps, re-derivable from their masters | Bound by `tools/visual/validate_material_maps.py`. Regenerate with `generate_material_maps.py`, do not edit |

Everything else in the repository -- all the source, all the tests, all the tools and all the documents -- is
under 6 MB together. When a copy or a migration of this repository goes wrong, it is nearly always one of these
three directories being partially copied, renamed or re-encoded, so they are named here rather than left to be
discovered.

## Build cache policy

Use `./scripts/gradle.sh <task>` for all local/CI Gradle invocations. The script keeps the wrapper distribution, dependency cache, and project cache under `${TMPDIR:-/tmp}` rather than in the repository. GitHub Actions sets the same locations explicitly.
