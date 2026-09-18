# Standing rules

These seven rules are all that survives of `docs/ROADMAP_TO_1000.md`, which was deleted on 2026-09-18 at the
owner's direction: the plan, the phase index, the item lists `R1`–`R16`, the baseline and rubric sections, the
1,511-line progress log and the definition of done all went, and the rules stayed. They are the rules the work
was done under and the rules any further work is done under, and they are the part of that document that was
about *how* rather than *what*.

## The rules

1. **No fake evidence.** A claim is made only when the repository contains something a stranger can re-run: a
   command, a generated artifact, a CI run, a screenshot at a fixed seed. Prose is not evidence.
2. **No test may be weakened.** Removing an assertion, adding `assertTrue(true)`, `|| true`, `@Disabled`, or
   lowering a threshold is never the fix. If an assertion must change it becomes *stricter or equally strict
   about the real contract*, and the commit message says why.
3. **Documentation numbers are generated** by a tool in the repository, never typed by hand.
4. **One item, one commit, one push — immediately.** Local batches are not allowed to grow large; work is
   pushed as soon as it is verified, so neither the workspace nor the review can lose it.
5. **Failure is recorded**, never deleted: an abandoned item becomes `[!]` with its reason.
6. **No completion claim on prose alone.** A claim that something is finished has to name the measurement or
   the gate that would fail if it stopped being true. Where two things are being measured — the repository and
   the game as a person plays it — a claim needs both, and neither may rest on a description.
7. **Nothing here is designed around revenue.** Monetisation, store pages and release preparation stay out of
   scope by owner direction; no system may depend on timers, paywalls or engagement metrics that exist to sell
   something.

## Two things a reader will meet that these rules produced

**Item identifiers.** Source comments, test javadocs, exception messages and `docs/playtests/findings.json`
cite ids like `R2.4`, `R8.2` or `R3.4`. Those ids belonged to the deleted roadmap's item lists. They are
historical labels: they identify which piece of work a gate came from, and they resolve to no document now.
Nothing may be added to that scheme, and nothing may be renamed inside it, because the citations in the tree
are part of its record.

**Status marks.** `[x]` verified · `[~]` in progress · `[ ]` not started · `[!]` stopped, with reason. Rule 5
is what the last one is for: a stopped item stays visible with its reason instead of disappearing.

## What this file is not

It is not a plan and not a record of what was done. The plan and the record were deleted with the roadmap; the
work itself is in the tree, and the gates that keep it honest are the tests, `tools/`, and the CI workflows.
If a number about this repository is needed, `tools/audit/measure_round.py` produces it from the tree rather
than from a document that can go stale.
