#!/usr/bin/env python3
"""Extended validator for Blender render artifacts.

Covers the original premium-v2 manifest/page checks plus:
  • edge safety (transparent 1px border per frame, where automatable)
  • pivot stability (normalized-bottom-left + per-class tolerance)
  • silhouette coverage + grade alpha preservation (where PIL available)
  • Phase 78 integrity gate: no sheet may be a resampled copy of a smaller sheet
    (the Phase 76 "NEAREST upscale" shortcut is a hard CI failure from now on)
  • Phase 78 accounting gate: declared decoded bytes must equal the real total

All new checks degrade gracefully: if Pillow is not installed only
the manifest-level checks run. Any failure is a hard error so CI
fails fast on bad batches.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import struct
import sys
from pathlib import Path

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

# --- Optional Pillow -------------------------------------------------------
try:
    from PIL import Image  # type: ignore
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

# Review-strip helpers are optional; used only for grade/silhouette alpha checks.
try:
    from review_strips import STAGE_GRADES, apply_grade, grayscale_view, silhouette_view  # type: ignore
    HAS_REVIEW_STRIPS = HAS_PIL
except ImportError:
    HAS_REVIEW_STRIPS = False
    STAGE_GRADES = []  # type: ignore


# --- Pivot expectations per frameClass ------------------------------------
# (pivot_y_center, tolerance).  pivot_x must always be 0.5 ±0.06.
PIVOT_EXPECTATIONS = {
    "character": (0.12, 0.04),
    "boss": (0.12, 0.04),
    "tree": (0.06, 0.04),
    "arena": (0.50, 0.04),
    "environment": (0.50, 0.04),
    "item": (0.50, 0.04),
    "projectile": (0.50, 0.04),
    "vfx": (0.50, 0.04),
}


# Honest bookkeeping: a "gate" only counts as measured when it inspects the produced artifact.
# Checks that merely look for a string in the pipeline source are reported separately, because they
# prove that a word exists, not that the pixels are right (see docs/ROADMAP_TO_1000.md R1.6).
MEASURED_GATES: list[str] = []
tier_divergence: set[str] = set()
CONFIG_PRESENCE_CHECKS: list[str] = []


def _measured(name: str) -> None:
    if name not in MEASURED_GATES:
        MEASURED_GATES.append(name)


def _config_presence(name: str) -> None:
    if name not in CONFIG_PRESENCE_CHECKS:
        CONFIG_PRESENCE_CHECKS.append(name)


# Rendered tiers of the reviewed baseline (`5374f2c^`). `hd_pipeline/config.py` currently asks for a
# higher tier, but no re-render has been delivered since, so the higher number is a plan, not a fact.
REVIEWED_TIERS = {
    "character": (2, 28),
    "boss": (3, 36),
    "tree": (3, 36),
    "item": (2, 28),
    "environment": (2, 28),
    "vfx": (2, 28),
    "projectile": (2, 28),
    "ui": (2, 28),
}
REVIEWED_TIER_OVERLAY = (2, 12)


# Phase 54-55 raised the pipeline tiers (TOP_TIER 3/36 -> 4/48, mid tier 2/28 -> 3/32) but no
# re-render was delivered under the higher numbers: the reviewed sheets were rendered with the older
# settings. The map below says which reviewed tier corresponds to what the config now asks for.
REVIEWED_TIER_FOR_CONFIGURED = {
    (4, 48): (3, 36),
    (3, 32): (2, 28),
}


def _reviewed_tier(frame_class: str, key: str) -> tuple[int, int]:
    # Equipment is drawn as an overlay on the hero, so it was rendered at the overlay tier (2/12)
    # even though its frame class is "character".
    if key.startswith("equipment_") or frame_class == "equipment_overlay":
        return REVIEWED_TIER_OVERLAY
    try:
        from hd_pipeline.config import render_tier as _rt
        configured = _rt(key, frame_class)
    except ImportError:
        return REVIEWED_TIERS.get(frame_class, (2, 28))
    return REVIEWED_TIER_FOR_CONFIGURED.get(configured, configured)


def _check_review_label_binding(root: Path) -> None:
    """Every asset's revision label must appear in the review document that claims to cover it.

    Same rule as core's `ReviewLabelBinding`, so the asset pipeline fails in CI even when the Java tests
    are not the ones being run (roadmap R1.11).
    """
    manifest = json.loads((root / "asset_manifest.json").read_text())
    repository = Path(__file__).resolve().parents[2]
    documents: dict[str, str | None] = {}
    for asset in manifest["assets"]:
        key = asset["key"]
        revision = asset.get("modelRevision")
        review = asset.get("categoryReview") or {}
        document = review.get("document")
        if not document:
            raise ValueError(f"{key}: no review document for modelRevision={revision}")
        if review.get("status") != "accepted":
            raise ValueError(f"{key}: review {document} status is {review.get('status')}")
        if document not in documents:
            path = repository / document
            documents[document] = path.read_text() if path.is_file() else None
        text = documents[document]
        if text is None:
            raise ValueError(f"{key}: missing review document {document}")
        if "`" + str(revision) + "`" not in text:
            raise ValueError(f"{key}: revision {revision} is not listed in {document}")
        if "<!-- BEGIN GENERATED: texture revision labels -->" not in text:
            raise ValueError(f"{key}: {document} has no generated revision-label block")


def _check_pivot_stability(asset: dict) -> None:
    key = asset["key"]
    pivot = asset["pivot"]
    # base range already checked by caller; now per-class stability
    frame_class = asset.get("frameClass", "character")
    expected_y, tol = PIVOT_EXPECTATIONS.get(frame_class, (pivot["y"], 0.10))
    # x must be near centre
    if abs(pivot["x"] - 0.5) > 0.06:
        raise ValueError(f"{key}: pivot x {pivot['x']} not centred for {frame_class} (expected 0.5±0.06)")
    if abs(pivot["y"] - expected_y) > tol:
        raise ValueError(
            f"{key}: pivot y {pivot['y']} unstable for {frame_class} (expected {expected_y}±{tol})"
        )
    # edge safety for pivot itself: not too close to normalized border
    if pivot["x"] < 0.04 or pivot["x"] > 0.96 or pivot["y"] < 0.04 or pivot["y"] > 0.96:
        # arena and environment are allowed centre pivots; the bound above already covers;
        # this guards against pivots hugging the corner.
        if frame_class not in ("arena", "environment", "item"):
            raise ValueError(f"{key}: pivot too close to normalized edge: {pivot}")


def _check_edge_safety(asset: dict, root: Path, pages_images: list[Image.Image]) -> None:  # type: ignore
    """Ensure no opaque pixel touches the 1px inner border of any frame.

    Exempt: arena family (full-bleed backdrop).  All other families must have
    at least 1px of transparent padding inside each frame edge.
    """
    if not HAS_PIL:
        return
    key = asset["key"]
    family = asset.get("family", "")
    frame_class = asset.get("frameClass", "")
    # Arena backdrop is a full-screen opaque image — edge touching is expected.
    if family == "arena" or key == "arena_backdrop":
        return
    frame_width = asset.get("frameWidth", asset["frameSize"])
    frame_height = asset.get("frameHeight", asset["frameSize"])
    for clip, frames in asset.get("clips", {}).items():
        for frame in frames:
            page = frame["page"]
            sheet_img = pages_images[page]
            x, y = frame["x"], frame["y"]
            crop = sheet_img.crop((x, y, x + frame_width, y + frame_height))
            alpha = crop.getchannel("A")
            w, h = crop.size
            # scan 1px border
            for ix in range(w):
                if alpha.getpixel((ix, 0)) > 10 or alpha.getpixel((ix, h - 1)) > 10:
                    raise ValueError(
                        f"{key}/{clip} index {frame['index']}: edge safety violation — opaque pixel on horizontal border (x={ix})"
                    )
            for iy in range(h):
                if alpha.getpixel((0, iy)) > 10 or alpha.getpixel((w - 1, iy)) > 10:
                    raise ValueError(
                        f"{key}/{clip} index {frame['index']}: edge safety violation — opaque pixel on vertical border (y={iy})"
                    )


def _check_silhouette(asset: dict, root: Path, pages_images: list[Image.Image]) -> None:  # type: ignore
    """Silhouette coverage sanity — where automatable via alpha.

    Bounds are generous so existing premium-v2 assets pass; the check catches
    empty, fully-opaque, or drastically off-coverage batches.
    """
    if not HAS_PIL:
        return
    key = asset["key"]
    family = asset.get("family", "")
    if family == "arena" or key == "arena_backdrop":
        return  # full-coverage exempt
    frame_width = asset.get("frameWidth", asset["frameSize"])
    frame_height = asset.get("frameHeight", asset["frameSize"])
    total = frame_width * frame_height
    for clip, frames in asset.get("clips", {}).items():
        for frame in frames:
            page = frame["page"]
            sheet_img = pages_images[page]
            x, y = frame["x"], frame["y"]
            crop = sheet_img.crop((x, y, x + frame_width, y + frame_height)).convert("RGBA")
            alpha = crop.getchannel("A")
            # fast count via getdata
            opaque = sum(1 for v in alpha.getdata() if v > 20)  # type: ignore[attr-defined]
            ratio = opaque / total if total else 0
            # per-key allowances for known tiny overlays / sapling
            if key.startswith("equipment_"):
                lo, hi = 0.002, 0.20
            elif "sapling" in key:
                lo, hi = 0.002, 0.60
            elif asset.get("frameClass") in ("item", "environment"):
                lo, hi = 0.02, 0.85
            else:
                lo, hi = 0.02, 0.85
            if ratio < lo or ratio > hi:
                raise ValueError(
                    f"{key}/{clip} index {frame['index']}: silhouette coverage {ratio:.4f} outside [{lo},{hi}] — empty or full frame?"
                )
            # silhouette_view must preserve alpha exactly
            if HAS_REVIEW_STRIPS:
                sil = silhouette_view(crop)
                if list(sil.getchannel("A").getdata()) != list(alpha.getdata()):  # type: ignore[attr-defined]
                    raise ValueError(f"{key}/{clip} index {frame['index']}: silhouette view altered alpha")


def _check_no_resampled_sheet(asset: dict, root: Path, pages_images: list[Image.Image]) -> None:  # type: ignore
    """Phase 78 gate: reject sheets whose pixels are a pixel-doubled/tripled copy of a smaller sheet.

    A 2x or 3x NEAREST resize puts every colour/alpha transition on the same grid residue
    (index ≡ factor-1 modulo factor). Real renders scatter edges evenly, so an overwhelming
    alignment is proof of resampling rather than rendering. Phase 76 shipped 100 sheets that
    were exactly this; the gate exists so it cannot happen again.
    """

    if not HAS_PIL:
        return
    key = asset["key"]
    min_boundaries = 500
    aligned_threshold = 0.98
    for image in pages_images:
        rgb = image.convert("RGB")
        width, height = rgb.size
        if width < 64 or height < 64:
            continue
        pixels = list(rgb.getdata())
        for factor, axis in ((2, "x"), (3, "x"), (2, "y"), (3, "y")):
            aligned = 0
            total = 0
            if axis == "x":
                if width % factor or width <= factor:
                    continue
                for row in range(0, height, max(1, height // 256)):
                    base = row * width
                    for column in range(width - 1):
                        left = pixels[base + column]
                        right = pixels[base + column + 1]
                        if left != right:
                            total += 1
                            if column % factor == factor - 1:
                                aligned += 1
            else:
                if height % factor or height <= factor:
                    continue
                for column in range(0, width, max(1, width // 256)):
                    for row in range(height - 1):
                        top = pixels[row * width + column]
                        bottom = pixels[(row + 1) * width + column]
                        if top != bottom:
                            total += 1
                            if row % factor == factor - 1:
                                aligned += 1
            if total >= min_boundaries and aligned / total >= aligned_threshold:
                raise ValueError(
                    f"{key}: sheet {width}x{height} is a {factor}x resampled copy along {axis} "
                    f"({aligned}/{total} transitions on the {factor}-pixel grid). Resampling adds "
                    "no detail and multiplies decoded memory; re-render at the target size instead."
                )


def _check_asset_ledger(root: Path) -> None:
    """Phase 78 gate: the shipped bytes must match the committed hash ledger.

    `docs/asset_hashes.json` pins every PNG under `android/assets/generated`. The ledger plus this
    gate is what makes silent art replacement impossible: changing a sheet without regenerating the
    ledger fails the build, in the validator and in `AssetIntegrityTest` alike.

    The gate is about the tree that ships. A single-category render batch (`--batch enemies`) writes
    only its own sheets and icons into its output directory, so its PNG set can never equal a ledger
    that pins the whole catalog — the R3.4 render failed here with "listed but absent:
    environment/…" and that failure blocked the artifact upload, hiding a render that was fine. A
    partial batch is therefore reported instead of failed, while the batch's own manifest is still
    checked for undeclared or missing PNGs (below) and promotion stays hash-audited by
    `promote_*_batch.py`. The exact gate still runs on `android/assets/generated` itself and on any
    rerender that ships the whole catalog.
    """

    repository = Path(__file__).resolve().parents[2]
    ledger_path = repository / "docs/asset_hashes.json"
    if not ledger_path.is_file():
        print("asset ledger not present in this checkout — ledger gate skipped")
        return
    ledger = json.loads(ledger_path.read_text(encoding="utf-8"))["sheets"]
    if not ledger:
        print("asset ledger is empty — ledger gate skipped")
        return

    shipped = {
        str(path.relative_to(root)).replace(os.sep, "/")
        for path in root.rglob("*.png")
    }
    pinned = set(ledger)
    shipped_tree = (repository / "android/assets/generated").resolve()
    if shipped != pinned and root.resolve() != shipped_tree:
        unlisted = sorted(shipped - pinned)[:5]
        print(
            f"asset ledger pins the shipped tree: this candidate batch ships {len(shipped - pinned)} "
            f"PNG(s) the ledger does not list {unlisted} and omits {len(pinned - shipped)} committed "
            f"PNG(s); the exact hash comparison applies to android/assets/generated and to full-catalog "
            f"rerenders"
        )
        return
    if shipped != pinned:
        unlisted = sorted(shipped - pinned)[:5]
        absent = sorted(pinned - shipped)[:5]
        raise ValueError(
            f"asset ledger does not match the shipped PNG set "
            f"(unlisted: {unlisted}, listed but absent: {absent})"
        )
    for relative, expected in ledger.items():
        actual = hashlib.sha256((root / relative).read_bytes()).hexdigest()
        if actual != expected:
            raise ValueError(f"{relative} does not match the committed hash ledger")


def _check_grade_alpha() -> None:
    """Global grade check: every STAGE_GRADE must preserve alpha exactly."""
    if not HAS_REVIEW_STRIPS or not STAGE_GRADES:
        return
    # tiny deterministic sprite
    sprite = Image.new("RGBA", (8, 8), (100, 150, 200, 255))
    sprite.putpixel((0, 0), (10, 10, 10, 255))
    sprite.putpixel((7, 7), (0, 0, 0, 0))
    orig_alpha = list(sprite.getchannel("A").getdata())  # type: ignore[attr-defined]
    for grade in STAGE_GRADES:
        graded = apply_grade(sprite, grade)
        if list(graded.getchannel("A").getdata()) != orig_alpha:  # type: ignore[attr-defined]
            raise ValueError(f"grade {grade[0]} altered alpha channel")
        # grayscale / silhouette also preserve alpha
        if list(grayscale_view(sprite).getchannel("A").getdata()) != orig_alpha:  # type: ignore[attr-defined]
            raise ValueError("grayscale_view altered alpha")
        if list(silhouette_view(sprite).getchannel("A").getdata()) != orig_alpha:  # type: ignore[attr-defined]
            raise ValueError("silhouette_view altered alpha")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("root", type=Path, help="path to android/assets/generated")
    args = parser.parse_args()
    root = args.root.resolve()
    manifest_path = root / "asset_manifest.json"
    if not manifest_path.is_file():
        raise FileNotFoundError(f"missing manifest: {manifest_path}")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    page_limit = manifest["maxAtlasPageSize"]
    # --- premium-v2 floors (unchanged) -----------------------------------
    if manifest.get("pipelineVersion", 0) < 3:
        raise ValueError("premium-v2 output requires pipeline version 3 or newer")
    if manifest.get("renderSupersample") not in (2, 3):
        raise ValueError("premium-v2 output must use 2x or 3x working renders")
    if manifest.get("opaqueRenderSamples", 0) < 28:
        raise ValueError("premium-v2 opaque renders require at least 28 samples")
    if manifest.get("overlayRenderSamples", 0) < 12:
        raise ValueError("premium-v2 overlays require at least 12 samples")
    # --- studio-v3 floors ------------------------------------------------
    # VisualQuality must be studio-v3 or studio-v4-vibrant for all assets once promoted
    for _asset in manifest.get("assets", []):
        vq = _asset.get("visualQuality")
        if vq not in ("studio-v3", "studio-v4-vibrant", "studio-v5-hd-pbr"):
            raise ValueError(f"{_asset.get('key','?')}: visualQuality must be studio-v3 or studio-v4-vibrant (found {vq})")
    # Line-weight contrast ratio: silhouette (2.4) vs interior (1.2) = 2.0 must be within 1.5-2.5
    # Automatable from scene.py source
    try:
        from pathlib import Path as _P
        import re as _re
        _scene_src = (_P(__file__).resolve().parents[1] / "blender" / "hd_pipeline" / "scene.py").read_text(encoding="utf-8")
        # Find silhouette and crease thickness values
        _sil = _re.search(r"silhouette.*thickness\s*=\s*([0-9.]+)", _scene_src)
        _cre = _re.search(r"crease.*thickness\s*=\s*([0-9.]+)", _scene_src)
        if _sil and _cre:
            _ratio = float(_sil.group(1)) / float(_cre.group(1)) if float(_cre.group(1)) != 0 else 0
            if not (1.5 <= _ratio <= 2.5):
                raise ValueError(f"studio-v3 line-weight ratio {_ratio:.2f} outside 1.5-2.5 (silhouette { _sil.group(1) } vs crease { _cre.group(1) })")
        # Highlight coverage bound: check that toon_material has LayerWeight and Glossy mixed via ShaderToRGB with threshold 0.92 or 0.85 (Phase 45)
        if "ShaderNodeLayerWeight" not in _scene_src or "ShaderNodeBsdfGlossy" not in _scene_src:
            raise ValueError("studio-v3 highlight/rim nodes missing (LayerWeight + Glossy required)")
        if "highlight_ramp" not in _scene_src or ("0.92" not in _scene_src and "0.85" not in _scene_src):
            raise ValueError("studio-v3/v4 highlight threshold 0.92 or 0.85 missing")
    except ValueError:
        raise
    except Exception:
        # Manifest-only fallback when source not available
        pass

    # global grade/silhouette alpha sanity
    _check_asset_ledger(root)
    _measured("asset hash ledger")
    _check_review_label_binding(root)
    _measured("review label binding")
    _check_grade_alpha()
    _measured("grade alpha round-trip")
    # Config-presence checks: they read the pipeline source and therefore say nothing about the
    # rendered pixels. Reported separately from the measured gates on purpose.
    try:
        from pathlib import Path as _P
        _cfg = (_P(__file__).resolve().parents[1] / "blender" / "hd_pipeline" / "config.py").read_text()
        if "#2ECC71" not in _cfg or "#FFD700" not in _cfg:
            raise ValueError("config presence: vibrant palette #2ECC71/#FFD700 missing from hd_pipeline/config.py")
        _config_presence("vibrant palette strings in config.py")
        _scene = (_P(__file__).resolve().parents[1] / "blender" / "hd_pipeline" / "scene.py").read_text()
        if "use_bloom" not in _scene or "use_gtao" not in _scene:
            raise ValueError("config presence: bloom and GTAO must stay enabled in hd_pipeline/scene.py")
        _config_presence("bloom/GTAO flags in scene.py")
        if "OUTLINE_COLORS" not in _cfg:
            raise ValueError("config presence: per-category OUTLINE_COLORS missing from hd_pipeline/config.py")
        _config_presence("per-category outline colours in config.py")
    except ValueError:
        raise
    except Exception:
        pass

    decoded_total = 0
    referenced: set[Path] = set()
    # keep sheet images loaded for edge/silhouette passes where PIL is available
    for asset in manifest["assets"]:
        key = asset["key"]
        pivot = asset["pivot"]
        if pivot["units"] != "normalized-bottom-left":
            raise ValueError(f"{key}: unsupported pivot units")
        if not (0.0 <= pivot["x"] <= 1.0 and 0.0 <= pivot["y"] <= 1.0):
            raise ValueError(f"{key}: pivot outside normalized frame")
        # new pivot stability per class
        _check_pivot_stability(asset)
        _measured("pivot stability")
        # Tier and engineVersion checks apply to every committed manifest: the manifest must describe
        # art that exists, at a tier that was reviewed.
        if True:
            if "renderSupersample" in asset and "renderSamples" in asset and "frameClass" in asset:
                import sys
                from pathlib import Path as _P
                _blender_tools = _P(__file__).resolve().parents[1] / "blender"
                if str(_blender_tools) not in sys.path:
                    sys.path.insert(0, str(_blender_tools))
                # The manifest must record a tier the reviewed art was actually rendered at. The
                # pipeline config was later raised (Phase 54-55: 2/28 -> 3/32, 3/36 -> 4/48) without a
                # delivered re-render, so comparing against the live config would either fail the
                # reviewed art or silently bless a claim the pixels do not support. Instead the
                # reviewed tiers are pinned here and the divergence is reported out loud.
                reviewed_tier = _reviewed_tier(asset["frameClass"], asset["key"])
                if (asset["renderSupersample"], asset["renderSamples"]) != reviewed_tier:
                    raise ValueError(
                        f"{asset['key']}: manifest tier ({asset['renderSupersample']},"
                        f"{asset['renderSamples']}) is not the reviewed tier {reviewed_tier} for "
                        f"{asset['frameClass']}"
                    )
                try:
                    from hd_pipeline.config import render_tier as _rt
                    configured = _rt(asset["key"], asset["frameClass"])
                    if configured != reviewed_tier:
                        tier_divergence.add(
                            f"{asset['frameClass']}: config {configured} vs reviewed {reviewed_tier}"
                        )
                except ImportError:
                    pass
            ev = asset.get("engineVersion") or manifest.get("engineVersion")
            if ev is None:
                raise ValueError(f"{asset['key']}: missing engineVersion (28.7 required)")
            if not str(ev).startswith(("28.7", "33.0", "34.0", "53.", "54.", "55.", "56.", "57.", "58.", "59.", "60.", "61.", "62.", "63.", "64.", "65.", "66.", "67.", "68.", "69.", "70.", "71.", "72.", "73.", "74.", "75.", "77.", "78.")):
                raise ValueError(
                    f"{asset['key']}: stale engineVersion {ev} — expected a reviewed pipeline line"
                )


        if asset["alphaMode"] != "STRAIGHT_RGBA":
            raise ValueError(f"{key}: invalid alpha contract")

        pages: list[tuple[int, int]] = []
        pages_images: list[Image.Image] = []  # parallel to pages when PIL
        for sheet in asset["sheets"]:
            path = _inside(root, sheet["file"])
            width, height, bit_depth, color_type = _png_header(path)
            if (width, height) != (sheet["width"], sheet["height"]):
                raise ValueError(f"{key}: sheet metadata does not match {path.name}")
            if width > page_limit or height > page_limit:
                raise ValueError(f"{key}: {path.name} exceeds {page_limit}px")
            if bit_depth != 8 or color_type != 6:
                raise ValueError(f"{key}: {path.name} must be 8-bit RGBA PNG")
            expected_bytes = width * height * 4
            if sheet["decodedBytes"] != expected_bytes:
                raise ValueError(f"{key}: invalid decoded byte count")
            decoded_total += expected_bytes
            referenced.add(path)
            pages.append((width, height))
            if HAS_PIL:
                try:
                    pages_images.append(Image.open(path).convert("RGBA"))
                except Exception as exc:
                    raise ValueError(f"{key}: failed to load sheet image {path.name}: {exc}") from exc

        frame_width = asset.get("frameWidth", asset["frameSize"])
        frame_height = asset.get("frameHeight", asset["frameSize"])
        for clip, frames in asset["clips"].items():
            for expected_index, frame in enumerate(frames):
                if frame["index"] != expected_index:
                    raise ValueError(f"{key}/{clip}: unstable frame order")
                if frame["width"] != frame_width or frame["height"] != frame_height:
                    raise ValueError(f"{key}/{clip}: changed frame contract")
                page = frame["page"]
                if page < 0 or page >= len(pages):
                    raise ValueError(f"{key}/{clip}: invalid page index")
                width, height = pages[page]
                if (frame["x"] < 0 or frame["y"] < 0
                    or frame["x"] + frame_width > width
                    or frame["y"] + frame_height > height):
                    raise ValueError(f"{key}/{clip}: frame outside page")

        # automated visual checks where PIL is available
        if HAS_PIL and pages_images:
            _check_edge_safety(asset, root, pages_images)
            _measured("edge safety")
            _check_silhouette(asset, root, pages_images)
            _measured("silhouette")
            _check_no_resampled_sheet(asset, root, pages_images)
            _measured("no resampling signature")

        if "icon" in asset:
            icon = _inside(root, asset["icon"])
            width, height, bit_depth, color_type = _png_header(icon)
            if (width, height, bit_depth, color_type) != (96, 96, 8, 6):
                raise ValueError(f"{key}: invalid equipment icon PNG")
            if icon not in referenced:
                decoded_total += width * height * 4
                referenced.add(icon)
            # icon edge safety: icon border must also be transparent padding
            if HAS_PIL:
                try:
                    icon_img = Image.open(icon).convert("RGBA")
                    alpha = icon_img.getchannel("A")
                    # at least outer 1px should not be fully opaque wall
                    # allow icons to touch edge but not be fully opaque ring
                    border_opaque = 0
                    for ix in range(96):
                        if alpha.getpixel((ix, 0)) > 10:
                            border_opaque += 1
                        if alpha.getpixel((ix, 95)) > 10:
                            border_opaque += 1
                    for iy in range(96):
                        if alpha.getpixel((0, iy)) > 10:
                            border_opaque += 1
                        if alpha.getpixel((95, iy)) > 10:
                            border_opaque += 1
                    # icons are centered with padding; a fully opaque border is an error
                    if border_opaque > 96 * 2:  # heuristic: more than half border opaque
                        raise ValueError(f"{key}: icon edge safety — border too opaque ({border_opaque} border pixels)")
                except ValueError:
                    raise
                except Exception as exc:
                    raise ValueError(f"{key}: failed to validate icon image: {exc}") from exc

    committed = {path.resolve() for path in root.rglob("*.png")}
    if committed != referenced:
        missing = sorted(str(path.relative_to(root)) for path in (committed ^ referenced))
        raise ValueError(f"undeclared or missing PNG files: {missing}")
    budget = manifest["decodedCatalogBudgetBytes"]
    if decoded_total > budget:
        raise ValueError(f"decoded catalog {decoded_total} exceeds {budget}")
    # Phase 78: the declared figure must equal the real one; Phase 76 shipped a number that no
    # committed sheet could produce, which is how the fake upscale hid in plain sight.
    declared = manifest.get("decodedBytes")
    if declared is not None and int(declared) != decoded_total:
        raise ValueError(
            f"manifest decodedBytes {declared} does not match the committed sheets "
            f"({decoded_total}); regenerate the manifest instead of editing the number"
        )
    print(
        f"Validated {len(manifest['assets'])} assets, {len(referenced)} RGBA PNGs, "
        f"{decoded_total} decoded bytes, max page {page_limit}px"
    )
    if tier_divergence:
        _measured("reviewed-tier pinning")
        print(
            "Reviewed-tier divergence (config asks for more than the art was rendered at): "
            + "; ".join(sorted(tier_divergence))
        )
    print(
        f"Measured gates: {len(MEASURED_GATES)} ({', '.join(MEASURED_GATES) or 'none'}) | "
        f"config-presence checks: {len(CONFIG_PRESENCE_CHECKS)} "
        f"({', '.join(CONFIG_PRESENCE_CHECKS) or 'none'})"
    )
    if not HAS_PIL:
        print("Pillow not available - image-level gates skipped (manifest-only mode)")


def _inside(root: Path, relative: str) -> Path:
    path = (root / relative).resolve()
    # ensure inside root
    try:
        path.relative_to(root)
    except ValueError:
        raise ValueError(f"missing or unsafe generated path: {relative}")
    if not path.is_file():
        raise ValueError(f"missing or unsafe generated path: {relative}")
    return path


def _png_header(path: Path) -> tuple[int, int, int, int]:
    with path.open("rb") as stream:
        if stream.read(8) != PNG_SIGNATURE:
            raise ValueError(f"not a PNG: {path}")
        length = struct.unpack(">I", stream.read(4))[0]
        if stream.read(4) != b"IHDR" or length != 13:
            raise ValueError(f"invalid PNG IHDR: {path}")
        width, height, bit_depth, color_type, compression, filtering, interlace = struct.unpack(
            ">IIBBBBB", stream.read(13)
        )
        if compression != 0 or filtering != 0 or interlace not in (0, 1):
            raise ValueError(f"unsupported PNG encoding: {path}")
        return width, height, bit_depth, color_type


if __name__ == "__main__":
    main()
