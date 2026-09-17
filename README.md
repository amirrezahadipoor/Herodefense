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
| [`AUDIT_2026-09-17.md`](docs/audit/AUDIT_2026-09-17.md) — the same rubric, re-measured at Phase 97 | `12e4dc0` | **722 / 920 in scope** (release preparation is not scored, so there is no 1,000-point figure) |

The roadmap's Gate 1 asks for ≥ 900 of 920 at Phase 97 and the re-audit measures 722, so the gate is **not
met** and no document here claims a finished score: the missing points are itemised sub-item by sub-item in
the audit, and the numbers behind it are frozen beside it as `docs/audit/MEASUREMENTS_2026-09-17.json` and
re-takable with `tools/audit/measure_round.py`.

## Build cache policy

Use `./scripts/gradle.sh <task>` for all local/CI Gradle invocations. The script keeps the wrapper distribution, dependency cache, and project cache under `${TMPDIR:-/tmp}` rather than in the repository. GitHub Actions sets the same locations explicitly.
