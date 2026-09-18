# Playtest protocol (roadmap R3.6)

A session is only evidence if it was recorded when it happened, on a build that can be named, and if every problem
it found turned into something on the roadmap. This document is the protocol; `tools/playtests/` is the machinery
that enforces it, and `docs/playtests/` is the ledger it writes.

## What a session is

A session is one run of the game, played to its end, with the numbers it produced. There are two kinds:

| Kind | Who plays | Where the record comes from |
|---|---|---|
| `human` | a person, on a device or an emulator, through the app | the game writes it at the game-over or victory screen |
| `automated` | the balance simulator, driving the real systems without rendering | `SimulatorSessionCaptureTest` writes it |

Both kinds end up in the same format, `herodefense.run-record/1`, so nothing downstream has to care which is
which. What the format deliberately does **not** contain is anything about the player: a record is a build, a mode,
a seed and the numbers. No names, no device identifiers, no timestamps finer than the second.

## Recording a human session

The game writes the record when a run ends — both endings, the hero falling and the vigil being completed — into
the app's local storage, keeping the newest twelve. Pull the one you just played:

```bash
adb shell run-as com.amirrezahadipoor.herodefense ls files/playtests
adb exec-out run-as com.amirrezahadipoor.herodefense \
    cat files/playtests/run-<timestamp>-w<wave>-<ending>.json > /tmp/session.json
```

Then promote it. The promotion files the raw record as evidence, appends the session to the ledger and validates
the result:

```bash
python3 tools/playtests/promote_run_record.py \
    --record /tmp/session.json \
    --player "amirreza (owner)" \
    --build "$(git rev-parse --short HEAD)" \
    --duration 22 \
    --notes "waves 1-12 read clearly; the wave-20 boss killed me in two hits from full health" \
    --notes "the torch-lit corridor secret is worth keeping"
```

`--duration` and `--notes` are the two things a file cannot know: how long the session took and what the player
thought. Everything else — the build, the seed, the mode, the waves, the ending, the counters — comes out of the
record. If the tool invents a number for you, that is a bug in the tool.

## Recording an automated session

```bash
./gradlew :core:test --tests "*SimulatorSessionCaptureTest"
python3 tools/playtests/promote_run_record.py \
    --record core/build/playtests/simulator-<seed>.json \
    --player "balance simulator" \
    --build "$(git rev-parse --short HEAD)" \
    --duration 1 --notes "routine sweep"
```

The simulator is not a human and a promoted simulator session must not be read as one: it says the shipped build
can be played end to end and what the curve does under a middling policy, not whether the game is enjoyable. Its
value is that it is reproducible — the same seed replays bit-identically, which is exactly what a human session
cannot offer.

## Findings

A session that found nothing still gets recorded; a session that found something also gets a finding:

```json
{
  "id": "finding-brief-vigil-has-no-teeth",
  "session": "session-2026-09-17-simulator-20342418142676294-w30",
  "date": "2026-09-17",
  "severity": "high",
  "summary": "the thirty-wave vigil never takes more than 18% of the hero's health under the simulator's policy",
  "evidence": "the session record: average damage fraction 0.0511, peak 0.1756, reached the final wave at full health",
  "roadmapItem": "R4.1",
  "status": "open"
}
```

Three rules make findings more than a wish list, and the validator enforces all three:

1. **`roadmapItem` must look like `R4.2`.** It used to have to exist in the roadmap; that document was deleted
   on 2026-09-18 at the owner's direction (only `docs/RULES.md` survives), so the id is a historical label and
   only its shape is checked. A finding still cannot point at a plan that is not written
   down. If the item is missing, add it to the roadmap (in English, in the phase it belongs to) or attach the
   finding to the item that will fix it.
2. **`fixed` needs a commit.** The evidence has to contain the hash of the commit that fixed it, so the claim can
   be checked in one `git show`.
3. **`accepted` needs a reason.** Writing `status: "accepted"` is allowed — some findings are deliberate design —
   but the evidence must say *why*, in the sentence that starts with "accepted:".

`severity` is one of `low`, `medium`, `high`, `blocker`; `status` is one of `open`, `fixed`, `accepted`.

## The ledger schema

`docs/playtests/sessions.json`, one object per session under `sessions`:

| Field | Meaning |
|---|---|
| `id` | `session-<date>-<source>-<seed>-w<waves>`, written by the promotion tool |
| `date` | the day the session was played, in the owner's calendar |
| `kind` | `human` or `automated` |
| `player` | who played; for automated sessions, that it was the simulator |
| `build` | the commit the played build came from, as a git hash |
| `protocol` | `docs/PLAYTEST_PROTOCOL.md` |
| `durationMinutes` | how long the session took |
| `wavesReached` | from the record, not from memory |
| `result` | `victory`, `defeat`, `abandoned`, `crashed` |
| `seed`, `mode`, `record` | optional: the run seed, the mode, and the filed raw record |
| `notes` | at least one line: what the player saw |

`docs/playtests/findings.json`, one object per finding under `findings`, with the fields shown above.

## How it is enforced

```bash
python3 tools/playtests/validate_playtest_ledger.py     # the ledger, against the real roadmap
python3 -m unittest discover -s tools/playtests/tests -t .   # the gate and the promotion path, on fixtures
```

Both run in CI next to the test suite, so a ledger entry that cites a roadmap item that does not exist, a session
without notes, a record that disagrees with the session it is filed under, or a "fixed" finding without a commit
fails the build instead of being noticed in review. The negative cases are tested as well as the positive one —
see `tools/playtests/tests/test_playtest_ledger.py`.

## Limits of this protocol, stated plainly

* One recorded human session is one person's opinion with numbers attached. It is evidence, not a study; the
  ledger is not a substitute for playing the game with several people, and no claim in `docs/` should be built on
  a single session.
* The simulator session proves the build is playable and the curve is what the gates say it is. It cannot prove
  the game is fun, and its results are only as good as the policy it plays with.
* A record says what happened, not why. The `notes` line is where the why goes, and a note that says nothing
  ("played fine") is worse than no session.
* Records are capped at twelve on the device. Pull the session you played before playing twelve more.
