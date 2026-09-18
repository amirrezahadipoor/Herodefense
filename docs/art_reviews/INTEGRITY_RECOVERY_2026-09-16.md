# Integrity recovery — runtime tier restored (2026-09-16)

Baseline reviewed commit `5374f2c^` · fake-upscale commit `5374f2c` · real master commit `6449a6d`

- sheets restored to the reviewed baseline in this run: **107**
- sheets published as a LANCZOS runtime LOD: **0** (kept off by default, see below)
- decoded sheet memory after recovery: **344.5 MiB** (was 1,277 MiB at `49fa799`)

## What the batch actually was (measured, not asserted)

`tools/visual/classify_master_tier.py --write-report` classifies every sheet from git history with two
grading-robust measurements (fine-detail gain and structure correlation) and writes
`docs/art_reviews/MASTER_TIER_PROVENANCE.md`. Result:

| measurement | count |
|---|---|
| sheets that never changed after the resize + grade (enlarged pixels shipped to the end) | 131 / 153 |
| sheets measured as resampled copies (correlation >= 0.95, no detail gain) | 59 |
| sheets carrying measurably more fine detail than the reviewed tier (genuine renders) | 27 |
| inconclusive (flat artwork where neither measure separates) | 67 |

The genuine higher-resolution renders are the Phase 77 batch produced by the `Generate visual asset
batch` GitHub Actions job (hero, four bosses, four regular enemies, arena/environment, some potions and
icons). They remain in git history at `6449a6d` and can be composed into a runtime tier once the new
frame layout has its own accepted review (roadmap R5.2).

Default mode restores every sheet to the reviewed baseline, because the art evidence in
`docs/art_reviews/**` is hash-bound: replacing a sheet without a new accepted review would
leave the repository claiming reviews that no longer match the pixels. Pass
`--promote-masters` only after a fresh review has been accepted for the new batch.
- decoded sheet memory after recovery: **344.5 MiB**

## Sheets that received a genuine master LOD

| sheet | master pixels | runtime pixels | scale |
|---|---|---|---|

## Sheets restored from the reviewed baseline

These sheets were byte-identical to `NEAREST(baseline)` at Phase 76 and their silhouette never changed afterwards, so the reviewed render is the honest source of truth.

| sheet |
|---|---|
| `sprites/ancient_golem.png` |
| `environment/arena_backdrop.png` |
| `environment/crystal_prop_0.png` |
| `environment/crystal_prop_1.png` |
| `environment/crystal_prop_2.png` |
| `sprites/ember_wyrm.png` |
| `equipment/acorn_band.png` |
| `equipment/antler_circlet.png` |
| `equipment/ashwood_bow.png` |
| `equipment/bark_first_root.png` |
| `equipment/bark_tunic.png` |
| `equipment/bloodroot_signet.png` |
| `equipment/boots_of_three_winds.png` |
| `equipment/copper_leaf_ring.png` |
| `equipment/crown_hollow_eye.png` |
| `equipment/crown_of_first_leaves.png` |
| `equipment/crystalbark_plate.png` |
| `equipment/dewstone_loop.png` |
| `equipment/echo_band.png` |
| `equipment/emberless_core.png` |
| `equipment/eternal_seed.png` |
| `equipment/fern_guard.png` |
| `equipment/golemsbane_warbow.png` |
| `equipment/hawk_eye_band.png` |
| `equipment/heartwood_aegis.png` |
| `equipment/hide_greaves.png` |
| `equipment/hunter_loop.png` |
| `equipment/jade_sap_ring.png` |
| `equipment/leather_cap.png` |
| `equipment/moonwood_longbow.png` |
| `equipment/mossweave_coat.png` |
| `equipment/owlguard_helm.png` |
| `equipment/padded_vest.png` |
| `equipment/ranger_mail.png` |
| `equipment/river_pebble_ring.png` |
| `equipment/rootguard_sabatons.png` |
| `equipment/sapphire_luck_ring.png` |
| `equipment/scout_hood.png` |
| `equipment/shadeleaf_mantle.png` |
| `equipment/silver_briar_ring.png` |
| `equipment/starfall_bow.png` |
| `equipment/stormrunner_boots.png` |
| `equipment/sunfall_last_arrow.png` |
| `equipment/thornwood_bow.png` |
| `equipment/trail_boots.png` |
| `equipment/twine_circle.png` |
| `equipment/verdant_oath.png` |
| `equipment/verdant_recurve.png` |
| `equipment/windrunner_last_steps.png` |
| `equipment/windstep_boots.png` |
| `equipment/worldbranch.png` |
| `equipment/yew_shortbow.png` |
| `sprites/fungal_brute.png` |
| `sprites/gloom_wolf.png` |
| `environment/ground_tile_0.png` |
| `environment/ground_tile_1.png` |
| `environment/ground_tile_2.png` |
| `icons/health_potion_1.png` |
| `icons/health_potion_2.png` |
| `icons/health_potion_3.png` |
| `icons/health_potion_4.png` |
| `icons/health_potion_5.png` |
| `icons/health_potion_6.png` |
| `sprites/hero.png` |
| `sprites/hero_ceremony.png` |
| `sprites/rootling.png` |
| `sprites/stonekin.png` |
| `sprites/thorn_matriarch.png` |
| `icons/ui_agility.png` |
| `icons/ui_close.png` |
| `icons/ui_coin.png` |
| `icons/ui_continue.png` |
| `icons/ui_dodge.png` |
| `ui/ui_frame_button_disabled.png` |
| `ui/ui_frame_button_normal.png` |
| `ui/ui_frame_button_pressed.png` |
| `ui/ui_frame_button_selected.png` |
| `ui/ui_frame_panel_disabled.png` |
| `ui/ui_frame_panel_normal.png` |
| `ui/ui_frame_panel_pressed.png` |
| `ui/ui_frame_panel_selected.png` |
| `ui/ui_frame_slot_disabled.png` |
| `ui/ui_frame_slot_normal.png` |
| `ui/ui_frame_slot_pressed.png` |
| `ui/ui_frame_slot_selected.png` |
| `icons/ui_general_power.png` |
| `icons/ui_health.png` |
| `icons/ui_inventory.png` |
| `icons/ui_lifesteal.png` |
| `icons/ui_luck.png` |
| `icons/ui_new_game.png` |
| `icons/ui_pause.png` |
| `icons/ui_restart.png` |
| `icons/ui_settings.png` |
| `icons/ui_shop.png` |
| `icons/ui_skill_chain_lightning.png` |
| `icons/ui_skill_critical_mastery.png` |
| `icons/ui_skill_long_range.png` |
| `icons/ui_skill_multi_shot.png` |
| `icons/ui_skill_stun_chance.png` |
| `icons/ui_speed.png` |
| `icons/ui_strength.png` |
| `icons/ui_wave.png` |
| `sprites/void_knight.png` |
| `sprites/world_tree_damaged.png` |
| `sprites/world_tree_healthy.png` |
| `sprites/world_tree_sapling.png` |

## Why this is the honest fix

1. A NEAREST resize cannot create detail, so reverting it costs the player nothing: the
   renderer draws each frame at the same world size it always did.
2. Reducing a *real* render to the runtime size with LANCZOS is a normal LOD decision, not a
   fake: the pixels come from Blender and the manifest records the master resolution.
3. `validate_generated_assets.py` now fails any sheet whose pixels are a NEAREST resize of
   another committed sheet, so the Phase 76 shortcut cannot return unnoticed.
4. `AssetIntegrityTest` records every committed sheet hash in `docs/asset_hashes.json`, so art
   cannot change silently again.

