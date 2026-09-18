# Performance runs

This directory is where performance numbers live, because a number in a document that nobody can reproduce
is a number that will eventually be wrong. The 2026-09-13 review found exactly that: the repository's own
documentation claimed 42 MB of texture residency while the manifest decoded to 1,277 MiB, and no test or
gate noticed.

## The rule

1. **Measure.** Run a command that prints the number.
2. **Log the run.** `python3 tools/perf/log_run.py --id <date>-<what> --commit <sha> --command "<command>" --metric name=value:unit`
   which writes `docs/perf/runs/<id>.json`.
3. **Regenerate the page.** `python3 tools/perf/render_performance_doc.py --write`
4. **Cite it.** Any other document that states a performance number carries `` `perf:<id>` `` (or
   `` `budget:<file>` `` for a threshold, `` `code:<path>` `` for a design constant, `` `env:<key>` `` for a
   fact about the machine) on the same line. `tools/perf/check_perf_provenance.py` fails the build otherwise.

`docs/perf/PERFORMANCE.md` is generated: editing it by hand fails `render_performance_doc.py --check`.

## Budgets the build enforces

| Budget | What it stops | Where it runs |
|---|---|---|
| `apk_budget.json` | the APK, its assets payload and its per-ABI natives growing past a committed size | `build-android.yml`, after `:android:assembleDebug` |
| `startup_budget.json` | cold start regressing past the committed milliseconds, measured by the platform's own `Displayed` line | `build-android.yml`, on the captured logcat |
| `wave50_memory_budget.json` | the process at wave 50 holding more memory than the committed budget | the instrumented measurement in `android/src/androidTest` (roadmap R8.3) |

## Why a missing measurement is not a failure

The startup gate exits **2** when the logcat capture holds no `Displayed` line. That is deliberate: a
measurement that did not happen must not be reported as a pass, and it must not fail a build for a reason
that has nothing to do with the build. The step prints "STARTUP NOT MEASURED" into the run summary instead,
which is the honest answer.
