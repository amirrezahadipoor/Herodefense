# Render hash log (roadmap R5.3)

A Blender batch is the most expensive thing this repository does, and until R5.3 it was all-or-nothing: a
job that timed out threw away everything it had packed, a re-run re-rendered byte-identical sheets, and
nothing recorded *what* a render had produced. `render_hash_log.py` answers the three questions that
follow from that, from the artifacts alone.

## What the log records, and what it refuses to record

Every `.png`, `.atlas` and `.json` under a render directory, with the sha256 and the size of each, plus the
asset keys the batch's manifest declares. It records **no** timestamp, **no** commit hash and **no**
absolute path, because two logs of the same bytes have to be byte-identical, and that is what makes a
comparison meaningful. `asset_manifest.json` itself is not hashed for the same reason: it carries
`generatedAt` and `generatedCommit`, so its bytes change on every run for reasons that have nothing to do
with the render. Its keys are recorded instead. Scratch frame directories (`_frames/`) and the log itself
are skipped.

```console
$ python3 tools/render/render_hash_log.py write android/assets/generated --log /tmp/log.json
wrote /tmp/log.json covering 321 file(s)
```

## What changed since the last render

```console
$ python3 tools/render/render_hash_log.py diff /tmp/previous.json /tmp/log.json
added 0, changed 1, removed 0, keys added 0, keys removed 0
  changed: sprites/rootling.png
```

`--fail-on-change` exits non-zero instead, for the promotion case: "this render is the reviewed one" is a
claim a gate can check rather than a claim in a commit message.

## What an interrupted render still owes

```console
$ python3 tools/render/render_hash_log.py resume /tmp/hero-defense-rendered/asset_manifest.json \
      /tmp/hero-defense-rendered/render_hash_log.json --root /tmp/hero-defense-rendered
2 asset key(s) still owed by this render
  missing: rootling: sprites/rootling.png is empty
  missing: stonekin: sprites/stonekin.atlas missing
```

`--keys` prints the list space-separated, which is what the render workflow feeds back into the
generator's `--only`. "Missing" means the render never finished the key (a file is absent, empty or absent
from the log); "changed" means every file is there but at least one no longer hashes to what the log
recorded.

## How the workflow uses it (`generate-visual-assets.yml`)

1. **restore** the masters directory from `actions/cache`, keyed by `hashFiles('tools/blender/**',
   'tools/render/**', 'scripts/install-blender-temp.sh')` plus the batch. Any change to the generator or the
   pipeline invalidates the cache, so a resumed render is always a render of the *same code*.
2. **ask** the log what the batch still owes (`resume --keys`) and keep the restored log as
   `/tmp/previous-render-log.json` before it is rewritten.
3. **render** the batch, restricted to the owed keys when there were any.
4. **write** the log again and print the diff against the previous one into the job's step summary, so the
   artifact page says what the render changed. Both are uploaded even when a later step fails, because a
   partial render is exactly the thing that has to be diffable.

### What resume does not cover

The resume list is built from the manifest, which the generator writes when a batch *finishes*. A batch
interrupted before its first manifest write has nothing to be resumed from, and it renders from the start;
its sheets are still in the cache, so it costs the render step and not the promotion work. The alternative
would be a per-file manifest written during the render, which the generator's sheet-at-a-time structure
does not have.

## Tests

```console
$ python3 -m unittest discover -s tools/render/tests -v
```

Ten tests: what the log records and refuses to record, that two logs of one tree are identical, that a
difference is classified rather than merely detected, that a clean re-render diffs clean, and the four ways
a key can be owed again (missing, empty, never rendered, altered).
