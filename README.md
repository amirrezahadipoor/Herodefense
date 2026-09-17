# Hero Defense

Android-only single-hero action-defense game made with Java and libGDX.

## Modules

- `core`: platform-independent gameplay, simulation, persistence models, and tests.
- `android`: the only launcher, packaging target, and instrumentation-test target.

There are intentionally no desktop, iOS, or browser modules. Input is touch-only.

## Where the project stands

[`docs/ROADMAP_TO_1000.md`](docs/ROADMAP_TO_1000.md) is the plan and the work log. Every item carries its own
evidence, and the dated log at the end records what failed as well as what shipped.

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

## Build cache policy

Use `./scripts/gradle.sh <task>` for all local/CI Gradle invocations. The script keeps the wrapper distribution, dependency cache, and project cache under `${TMPDIR:-/tmp}` rather than in the repository. GitHub Actions sets the same locations explicitly.
