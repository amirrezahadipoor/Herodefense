# Arena Refresh v4 — Held Record

**Decision:** HELD
**Scope:** The arena batch's re-render of the portrait backdrop, the three ground patches and the three crystal
landmarks, measured against the pixels the catalog ships today
**Render workflow run:** `35753783929` (batch `arena`, source commit `5fff1f5`)
**Artifact:** `hero-defense-arena-sprites` (run `35752561641`, first dispatch of the unblocked batch)
**Candidate manifest SHA-256:** `3cee7de19a196e1a321d8981c7d8fdbdedc28b416f2c07e4a574267284ceadb9`
**Candidate master engine:** `73.0-studio-v5-hd-pbr-4x48-pbr` (the accepted tier was rendered by
`33.0-studio-v3-3x36-full`)
**Crystal refresh audit SHA-256:** `268ca920a8c11de9a8cf429482ac18e28b1c93be5ecdddf409f1474c28961052`
**Evidence committed:** `docs/art_reviews/arena_crystal_v4/` (two sheets plus the audit JSON)

## Why this record exists

`ARENA_PREMIUM_V2_REVIEW.md` accepted a batch whose entire reason for existing was *lifting the arena's value
range* so text and actors read on real OLED panels: mean backdrop value rose from ≈24 to ≈37 of 255, and the
runtime changes that accompanied it are pinned by the device brightness gates.

The next arena render is measurably darker than that accepted tier, for a reason that has nothing to do with any
single prop: the master engine changed. Every key in the batch lost light, measured on the pixels a player
actually sees (mean luminance over pixels whose alpha is above 16, so transparent padding cannot flatter a
prop):

| key | shipped, painted mean | re-render, painted mean | ratio | gate |
| --- | --- | --- | --- | --- |
| `arena_backdrop` | **37.56** | **22.49** | **0.599×** | floor 0.90× |
| `ground_tile_0` | 34.38 | 18.96 | 0.551× | floor 0.90× |
| `ground_tile_1` | 42.87 | 33.48 | 0.781× | floor 0.90× |
| `ground_tile_2` | 34.54 | 19.40 | 0.562× | floor 0.90× |
| `crystal_prop_0` | 82.21 | 70.50 | 0.858× | floor 0.90× |
| `crystal_prop_1` | 77.99 | 64.72 | 0.830× | floor 0.90× |
| `crystal_prop_2` | 68.99 | 58.45 | 0.847× | floor 0.90× |

A backdrop at 0.60× is not a refresh. It is the display-quality pass being spent in the other direction, on the
largest single surface in the game, three waves before the player notices they are squinting.

## What was measured, and how

- `tools/visual/create_arena_crystal_review.py` measures each landmark against the pixels it would replace and
  is the reason the numbers above exist for the crystals; its audit is the committed evidence.
- `tools/visual/create_arena_batch_review.py` now measures **every** key a batch renders — backdrop, ground,
  landmarks, cover — against the committed baseline, and refuses a candidate that loses more than a tenth of its
  painted mean value. Run against this batch it stops on the backdrop with the same numbers quoted here.
- The threshold is a tenth and not zero because a re-render is allowed to be *different*: the crystals gained
  studs, moss and an emissive core, and the ground patches gained a crisper rim. What a re-render may not do is
  come back dimmer.
- Nothing was promoted: `arena_backdrop`, `ground_tile_0..2` and `crystal_prop_0..2` in the catalog are still the
  reviewed v2 pixels, and `docs/asset_hashes.json` is unchanged. The ledger is the proof.

## What this unblocks, and what it does not

Two things were fixed on the way here and stay fixed:

1. The arena review and promotion tools named a crystal revision (`arena-crystal-premium-v2`) the builder has not
   emitted since the vibrant pass, so the batch could never pass its own audit. Both tools now name
   `arena-crystal-premium-v4-vibrant`, its emissive core and its `studio-v4-vibrant` tier, and the batch renders
   and validates end to end.
2. The byte-identity rule in the arena audit is now about *revisions* rather than bytes: a key whose builder moved
   to a new revision and whose pixels did not move is still refused — that is a stalled upgrade — while a key
   whose revision is unchanged and whose bytes match is recorded as `reproducedBaseline`. A re-run of a batch that
   already shipped twelve cover props is the pipeline proving it is deterministic, and the second dispatch of this
   batch reproduced all twelve cover props byte for byte.

What it does not fix, because it is not a review's job to fix it: the master render's exposure. The path to
shipping the refreshed arena is one of

- re-base the master engine's exposure/lighting so full-frame art lands within a tenth of the accepted tier, then
  re-run the batch and promote through `promote_arena_batch.py` (the tooling is ready: the audit and promotion
  contracts now agree with what the builder emits); or
- ship the refreshed *construction* with the accepted *lighting* by re-rendering only the crystals and the ground
  once the exposure is settled, which is the smaller diff and the one this record recommends.

Until one of those happens, the arena's pixels stay the pixels the display-quality pass reviewed.
