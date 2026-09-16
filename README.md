# Hero Defense

Android-only single-hero action-defense game made with Java and libGDX.

## Modules

- `core`: platform-independent gameplay, simulation, persistence models, and tests.
- `android`: the only launcher, packaging target, and instrumentation-test target.

There are intentionally no desktop, iOS, or browser modules. Input is touch-only.

See [`ROADMAP.md`](ROADMAP.md) for tracked scope and progress, and
[`docs/ROADMAP_TO_1000.md`](docs/ROADMAP_TO_1000.md) for the plan that answers the independent
audit of 2026-09-16 ([`docs/audit/AUDIT_2026-09-16.md`](docs/audit/AUDIT_2026-09-16.md),
550/1000). Until the roadmap is complete, no document in this repository claims a score.

## Build cache policy

Use `./scripts/gradle.sh <task>` for all local/CI Gradle invocations. The script keeps the wrapper distribution, dependency cache, and project cache under `${TMPDIR:-/tmp}` rather than in the repository. GitHub Actions sets the same locations explicitly.
