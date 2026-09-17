# Environment facts

Facts about the machines this project is built and measured on, as opposed to facts about the game. The
performance gate (`tools/perf/check_perf_provenance.py`) accepts a number in the documentation when it cites
`env:<key>` from this table, so an environment claim is a line in a reviewed file rather than a memory.

| Key | Fact | Where it comes from |
|---|---|---|
| `ci-tmp-disk` | The GitHub runner's `/tmp` is a 1 GB tmpfs | GitHub-hosted runner image layout; observed when the Blender install filled it |
| `blender-install` | A Blender install for the render pipeline is over 400 MB | `scripts/install-blender-temp.sh`, checksum-pinned download |
| `emulator-profile` | The CI emulator is API 35, x86_64, Pixel 3a, `swiftshader_indirect`, no audio | `.github/workflows/build-android.yml` |
| `grader-ubuntu` | The render workflow runs on `ubuntu-latest` with xvfb | `.github/workflows/generate-visual-assets.yml` |
