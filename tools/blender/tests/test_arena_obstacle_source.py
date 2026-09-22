from __future__ import annotations

import ast
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
ENVIRONMENT = ROOT / "tools/blender/hd_pipeline/environment.py"
GENERATOR = ROOT / "tools/blender/generate_assets.py"

# The four families and the construction each one is built out of. The point of naming the primitives here is that
# a family is a construction, not a material: if two families ever answer with the same set of primitives, one of
# them has become a recolour of the other and the field is back to reading as one rock.
FAMILY_PRIMITIVES = {
    "_standing_stone": {"add_ico", "add_cube", "add_torus", "add_leaf", "add_cone"},
    "_ruin_slab": {"add_ico", "add_cube", "add_leaf", "add_torus", "add_cylinder_between", "add_cone"},
    "_thorn_hedge": {"add_ico", "add_cylinder_between", "add_cone", "add_leaf"},
    "_mossy_boulder": {"add_ico", "add_leaf", "add_cone", "add_torus", "add_cylinder_between"},
}
SHELTER_FAMILIES = ("standing_stone", "ruin_slab")
LOW_FAMILIES = ("thorn_hedge", "mossy_boulder")


class ArenaObstacleSourceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.environment = ENVIRONMENT.read_text(encoding="utf-8")
        cls.generator = GENERATOR.read_text(encoding="utf-8")
        cls.tree = ast.parse(cls.environment)

    def test_the_field_carries_two_kinds_of_cover_in_four_families(self) -> None:
        families = self._literal("OBSTACLE_FAMILIES")
        self.assertEqual(4, len(families), "the arena's cover is four families")
        by_name = {name: (cover, height) for name, cover, height in families}
        self.assertEqual({"standing_stone", "ruin_slab", "thorn_hedge", "mossy_boulder"}, set(by_name))
        self.assertEqual(
            {"shelter", "low"},
            {cover for cover, _ in by_name.values()},
            "a family is one of the two kinds the simulation has: it stops bodies, and arrows too or only bodies",
        )
        for family in SHELTER_FAMILIES:
            self.assertEqual("shelter", by_name[family][0], family)
        for family in LOW_FAMILIES:
            self.assertEqual("low", by_name[family][0], family)
        self.assertEqual(3, self._literal("OBSTACLE_VARIANTS"))

    def test_standing_cover_is_at_least_twice_the_height_of_low_cover(self) -> None:
        by_name = {name: height for name, _, height in self._literal("OBSTACLE_FAMILIES")}
        tallest_low = max(by_name[family] for family in LOW_FAMILIES)
        for family in SHELTER_FAMILIES:
            self.assertGreaterEqual(
                by_name[family], tallest_low * 2.0,
                f"{family} stands at {by_name[family]} against {tallest_low} for the tallest low cover: the two "
                "kinds have to differ by silhouette alone, before colour is read",
            )

    def test_every_family_is_a_construction_and_not_a_recolour(self) -> None:
        for function, expected in FAMILY_PRIMITIVES.items():
            body = self._function(self.tree, function)
            source = ast.get_source_segment(self.environment, body) or ""
            used = {name for name in expected if f"{name}(" in source}
            self.assertTrue(
                used, f"{function} builds nothing out of the shared primitives"
            )
            for primitive in ("add_ico", "add_leaf"):
                self.assertIn(primitive, used, f"{function} is missing {primitive}")
            self.assertNotIn("make_material", source, f"{function} must reuse the arena's sheet, not author colours")
        # Two families may share a toolbox -- a monolith and a leaning slab both need cubes -- but not a mix: the
        # count of each primitive a family builds is its construction, and four families have four of them.
        mixes = []
        for function in FAMILY_PRIMITIVES:
            body = self._function(self.tree, function)
            source = ast.get_source_segment(self.environment, body) or ""
            mixes.append(tuple(
                (name, source.count(f"{name}(")) for name in
                ("add_ico", "add_cube", "add_torus", "add_leaf", "add_cone", "add_cylinder_between")
            ))
        self.assertEqual(len(mixes), len(set(mixes)), f"two families build the same thing: {mixes}")
        for mix, (function, _) in zip(mixes, FAMILY_PRIMITIVES.items()):
            self.assertGreaterEqual(
                sum(1 for _, count in mix if count), 3,
                f"{function} is a single primitive repeated, not a construction: {mix}",
            )

    def test_cover_never_glows_and_records_its_own_contract(self) -> None:
        function = self._function(self.tree, "build_obstacle_prop")
        source = ast.get_source_segment(self.environment, function) or ""
        self.assertIn('"runtimeGlow": False', source)
        for field in ('"assetKind": "obstacle"', '"cover": cover', '"coverFamily": family',
                      '"heightUnits": height'):
            self.assertIn(field, source)
        self.assertIn("modelRevision", source)
        # `family` in the manifest is the output directory the asset lives in; the cover's own family rides
        # beside it, because the promotion tools key the metadata sidecar off `family`.
        self.assertIn('"family"', 'family')
        self.assertIn("arena-obstacle-premium-v1", source)
        self.assertIn('raise ValueError(f"Unknown obstacle family', source)
        self.assertIn('raise ValueError(f"Unknown obstacle variant', source)

    def test_the_render_batch_covers_every_family_and_variant(self) -> None:
        self.assertIn("for family, cover, height in OBSTACLE_FAMILIES:", self.generator)
        self.assertIn("for variant in range(OBSTACLE_VARIANTS):", self.generator)
        self.assertIn('key = f"obstacle_{family}_{variant}"', self.generator)
        self.assertIn('build_obstacle_prop(name, value)', self.generator)
        self.assertIn('"assetKind": "obstacle"', self.generator)

    def test_the_cover_batch_is_reachable_and_renders_only_the_cover(self) -> None:
        # A batch name that reaches the generator but not a render function generates nothing at all, and the
        # workflow reports that as zero assets rather than as an error. Both halves are pinned.
        self.assertIn('if args.batch == "arena-cover":', self.generator)
        self.assertIn("generated.extend(render_arena_cover(output, only))", self.generator)
        self.assertIn('choices=', self.generator)
        self.assertIn('"arena-cover"', self.generator)
        cover_body = self._generator_source("render_arena_cover")
        self.assertIn('key = f"obstacle_{family}_{variant}"', cover_body)
        self.assertIn("build_obstacle_prop(name, value)", cover_body)
        self.assertNotIn("build_arena_backdrop", cover_body)

    def _literal(self, name: str):
        for node in self.tree.body:
            if isinstance(node, ast.Assign) and any(
                isinstance(target, ast.Name) and target.id == name for target in node.targets
            ):
                return ast.literal_eval(node.value)
        raise AssertionError(f"{name} is not a module-level literal")

    def _generator_source(self, name: str) -> str:
        tree = ast.parse(self.generator)
        return ast.get_source_segment(self.generator, self._function(tree, name)) or ""

    def _function(self, tree: ast.Module, name: str):
        for node in tree.body:
            if isinstance(node, ast.FunctionDef) and node.name == name:
                return node
        raise AssertionError(f"{name} is not defined at module level")


if __name__ == "__main__":
    unittest.main()
