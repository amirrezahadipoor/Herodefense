"""The composed-tier check reads history, so the job that runs it has to check history out.

`compose_master_tier.py` is defined against the render batch's own manifest -- "the master render's geometry"
is a fact about commit `6449a6d`, not about today's tree -- so a checkout with one commit of history cannot run
it. On 2026-09-17 it did not: the check died inside `subprocess` with `git show` exiting 128 and the log said
nothing a reader could act on. This is the guard for the two halves of that: the tool now names its own fix,
and the job that runs it carries the history.
"""
import pathlib
import unittest

import yaml

REPOSITORY = pathlib.Path(__file__).resolve().parents[3]
WORKFLOWS = REPOSITORY / ".github/workflows"
#: Steps that read a blob out of history rather than out of the working tree.
HISTORY_READERS = ("compose_master_tier.py", "create_master_tier_review.py", "master_tier.py --check")


def checkout_step(job: dict) -> dict | None:
    for step in job.get("steps", []):
        if str(step.get("uses", "")).startswith("actions/checkout@"):
            return step
    return None


class CheckoutDepthTest(unittest.TestCase):
    def test_every_job_that_runs_the_composed_tier_check_checks_out_its_history(self) -> None:
        found = 0
        for path in sorted(WORKFLOWS.glob("*.yml")):
            workflow = yaml.safe_load(path.read_text(encoding="utf-8"))
            for name, job in (workflow.get("jobs") or {}).items():
                runs = "\n".join(str(step.get("run", "")) for step in job.get("steps", []))
                if not any(reader in runs for reader in HISTORY_READERS):
                    continue
                found += 1
                checkout = checkout_step(job)
                self.assertIsNotNone(checkout, f"{path.name}/{name} has no checkout at all")
                depth = (checkout.get("with") or {}).get("fetch-depth")
                self.assertEqual(0, depth,
                                 f"{path.name}/{name} runs a history-reading step and checks out {depth} commits")
        self.assertTrue(found, "no job was found that runs the composed-tier check: this test guards nothing")

    def test_the_composed_tier_check_is_still_the_step_that_needs_it(self) -> None:
        text = (WORKFLOWS / "test-core.yml").read_text(encoding="utf-8")
        self.assertIn("Re-check the composed master tier and its review", text)

    def test_the_composer_says_what_a_shallow_clone_costs(self) -> None:
        source = (REPOSITORY / "tools/visual/compose_master_tier.py").read_text(encoding="utf-8")
        self.assertIn("fetch-depth: 0", source,
                      "the failure has to name its own fix, not raise out of subprocess")


if __name__ == "__main__":
    unittest.main()
