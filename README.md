# Hero Defense

Android-only single-hero action-defense game made with Java and libGDX.

## Modules

- `core`: platform-independent gameplay, simulation, persistence models, and tests.
- `android`: the only launcher, packaging target, and instrumentation-test target.

There are intentionally no desktop, iOS, or browser modules. Input is touch-only.

## Where the project stands

See [`ROADMAP.md`](ROADMAP.md) for tracked scope and progress, and
[`docs/ROADMAP_TO_1000.md`](docs/ROADMAP_TO_1000.md) for the plan that answers the independent
audit. There are two audits in this repository, and both are meant to be read together:

| audit | commit | score |
|---|---|---|
| [`AUDIT_2026-09-16.md`](docs/audit/AUDIT_2026-09-16.md) — what the project looked like before the roadmap | `49fa799` | 550 / 1000 |
| [`AUDIT_2026-09-17.md`](docs/audit/AUDIT_2026-09-17.md) — the same rubric, re-measured at Phase 97 | `12e4dc0` | 731 / 920 in scope (release preparation is not scored, so there is no 1,000-point figure) |
| [`AUDIT_2026-09-18.md`](docs/audit/AUDIT_2026-09-18.md) — the same rubric again, after the Back button, the localisation layer and the compressed sheets | `bf3776e` | **777 / 920 in scope** |

The 2026-09-17 audit's own category rows sum to 731; this file said 722 for it until the 2026-09-18 re-audit
found the disagreement, which is recorded in that audit's category 10 rather than quietly corrected.

Since then: Android's Back button is handled on all thirteen screens and verified on an emulator; a Persian
localisation layer ships — a shaping and bidi pipeline checked against 245 vectors that CI re-derives from two
pinned libraries, Vazirmatn under the SIL OFL, 181 strings in 10 tables across ten screens, five screens
mirrored, and a build gate that fails when a source draws its own words; and 16 sheets ship as ETC2 containers,
which is where 5.6 MB of the APK went. What is still missing is itemised in the audit's §6: 17 files still draw
English, the HUD does not mirror, and there are no accessibility options and no store assets.

The roadmap's Gate 1 asks for ≥ 900 of 920 and the re-audit measures 777, so the gate is **not met** and no
document here claims a finished score. The numbers behind it are frozen as
`docs/audit/MEASUREMENTS_2026-09-18.json` and re-takable with `tools/audit/measure_round.py`.

## Build and test

```bash
./scripts/gradle.sh :core:test :core:ciStaticAnalysis   # the platform-independent game and its analyzers
python3 tools/audit/measure_round.py                    # every count the audits quote
```

The Android layer (`:android:assembleDebug`, the emulator journeys) builds in GitHub Actions; there is no
desktop, iOS or browser target to run the game on a workstation.

## Build cache policy

Use `./scripts/gradle.sh <task>` for all local/CI Gradle invocations. The script keeps the wrapper distribution, dependency cache, and project cache under `${TMPDIR:-/tmp}` rather than in the repository. GitHub Actions sets the same locations explicitly.
