package com.amirrezahadipoor.herodefense.balance;

import com.amirrezahadipoor.herodefense.gameplay.DifficultyCurve;
import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.gameplay.ItemDropSystem;
import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import com.amirrezahadipoor.herodefense.rewards.RewardCardId;
import com.amirrezahadipoor.herodefense.trials.TrialId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The machine-checked blocks of {@code docs/BALANCE.md} (roadmap R4.4).
 *
 * <p>A balance document that is written by hand is a document that lies the first time a constant moves, and this
 * repository has the receipts: the wave-100 health checkpoint published in this file was two curve revisions out of
 * date, and every number in the R4.6 table had been typed from a terminal instead of computed. So the numbers moved
 * into code. Everything between a {@code <!-- balance:generated name -->} marker and its closing marker is built
 * here from the same constants and the same simulator the game uses, {@code BalanceDocumentTest} refuses to let the
 * file disagree with this class, and {@code :core:regenerateBalanceDoc} rewrites the blocks in place for a
 * contributor who changed a number on purpose.
 *
 * <p>What is <em>not</em> generated is the prose: the reasoning, the rejected candidates, the measurements that
 * belong to a gate rather than to a block. That text is written by a person and paid for by a person, and the point
 * of the markers is to make it obvious which sentences those are.
 */
public final class BalanceDocument {

    /** Where the document lives, relative to the module or the repository root. */
    private static final Path[] CANDIDATES = {Path.of("docs", "BALANCE.md"), Path.of("..", "docs", "BALANCE.md")};

    /** The blocks this class owns, in the order they appear in the document. */
    public static final List<String> BLOCKS = List.of(
        "growth-checkpoints", "second-half", "drop-economy", "economy-audit", "ascension-bumps");

    /** The balance sweep's five fixed seeds, the same ones every curve measurement in the document uses. */
    private static final long[] CURVE_SEEDS = {
        0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2, 0x747269616C7331L, 0x4341524453494DL};

    /** The trial gate's five fixed seeds. */
    private static final long[] TRIAL_SEEDS = {
        0x747269616C7331L, 0x747269616C7331L + 1, 0x747269616C7331L + 2,
        0x747269616C7331L + 3, 0x747269616C7331L + 4};

    /** The three pairs the trial gate measures highest; the full seventy-eight-pair matrix is the gate's job. */
    private static final TrialId[][] RISKIEST_PAIRS = {
        {TrialId.BOSS_BOUNTY, TrialId.FAMISHED_EARTH},
        {TrialId.GLASS_ARROWS, TrialId.FAMISHED_EARTH},
        {TrialId.MISERS_PACT, TrialId.FAMISHED_EARTH}};

    /** The reward-card scenario the card gate has come closest to failing: the lowest-offence forced build. */
    private static final long CARD_SEED = 0x4341524453494DL;

    private static final int[] CHECKPOINT_WAVES = {1, 25, 50, 75, 100, 125, 150, 175, 200};

    /** The drop budget the table's last row and the published total share with the drop tests. */
    private static final float TOTAL_DROP_RATE = 0.099515f;

    private BalanceDocument() {
    }

    public static String open(String block) {
        return "<!-- balance:generated " + block + " -->";
    }

    public static String close(String block) {
        return "<!-- balance:end " + block + " -->";
    }

    public static String block(String name) {
        return switch (name) {
            case "growth-checkpoints" -> growthCheckpoints();
            case "second-half" -> secondHalf();
            case "drop-economy" -> dropEconomy();
            case "economy-audit" -> economyAudit();
            case "ascension-bumps" -> ascensionBumps();
            default -> throw new IllegalArgumentException("no generated block is called " + name);
        };
    }

    /** What the document between a block's markers has to say, according to the code. */
    private static String published(String document, String name) {
        String from = open(name) + "\n";
        int start = document.indexOf(from);
        if (start < 0) {
            throw new IllegalStateException("docs/BALANCE.md is missing the opening marker for " + name);
        }
        int blockStart = start + from.length();
        int end = document.indexOf(close(name), blockStart);
        if (end < 0) {
            throw new IllegalStateException("docs/BALANCE.md is missing the closing marker for " + name);
        }
        return document.substring(blockStart, end);
    }

    /** Checks every block, and names the one that drifted rather than dumping the whole file. */
    public static List<String> driftedBlocks(String document) {
        List<String> drifted = new ArrayList<>();
        for (String name : BLOCKS) {
            if (!published(document, name).equals(block(name))) {
                drifted.add(name);
            }
        }
        return drifted;
    }

    /** The document with every block replaced by what the code computes. */
    public static String regenerate(String document) {
        String result = document;
        for (String name : BLOCKS) {
            String from = open(name) + "\n";
            int start = result.indexOf(from);
            if (start < 0) {
                throw new IllegalStateException("docs/BALANCE.md is missing the opening marker for " + name);
            }
            int blockStart = start + from.length();
            int end = result.indexOf(close(name), blockStart);
            if (end < 0) {
                throw new IllegalStateException("docs/BALANCE.md is missing the closing marker for " + name);
            }
            result = result.substring(0, blockStart) + block(name) + result.substring(end);
        }
        return result;
    }

    public static Path locate() {
        for (Path candidate : CANDIDATES) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("docs/BALANCE.md was not found from " + Path.of("").toAbsolutePath());
    }

    private static String read(Path document) {
        try {
            return Files.readString(document, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    /**
     * Rewrites the blocks: the entry point of {@code :core:regenerateBalanceDoc}, the task a contributor runs after
     * changing a number on purpose. It prints nothing on purpose — the document is a file, and the task reports what
     * changed by comparing the file before and after the run.
     */
    public static void main(String[] args) {
        Path document = locate();
        String before = read(document);
        String after = regenerate(before);
        if (after.equals(before)) {
            return;
        }
        try {
            Files.writeString(document, after, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static String growthCheckpoints() {
        DifficultyCurve curve = new DifficultyCurve();
        List<String> lines = new ArrayList<>();
        lines.add("| Wave | Baseline HP | Expected hero bar | Baseline damage | Damage as a share of the bar |"
            + " Damage at tier 10 |");
        lines.add("|---:|---:|---:|---:|---:|---:|");
        for (int wave : CHECKPOINT_WAVES) {
            float bar = DifficultyCurve.expectedHeroMaxHealth(wave);
            lines.add(String.format(Locale.ROOT, "| %d | %.2f | %.2f | %.4f | %.2f%% | %.4f |",
                wave, curve.baselineRegularHealth(wave), bar, curve.baselineRegularDamage(wave),
                100f * curve.baselineRegularDamage(wave) / bar, curve.baselineRegularDamage(wave, 10)));
        }
        return join(lines);
    }

    /** The measurements R4.6 is judged on, taken fresh every time the document is regenerated. */
    private static String secondHalf() {
        BalanceSimulator simulator = new BalanceSimulator();
        List<Float> averages = new ArrayList<>();
        List<Float> dips = new ArrayList<>();
        double[] quarters = new double[4];
        for (long seed : CURVE_SEEDS) {
            BalanceReport report = simulator.run(seed);
            float[] perQuarter = new float[4];
            for (WaveSample wave : report.waves()) {
                perQuarter[Math.min(3, (wave.wave() - 1) / 50)] += wave.damageFraction() / 50f;
            }
            for (int quarter = 0; quarter < 4; quarter++) {
                quarters[quarter] += perQuarter[quarter] / CURVE_SEEDS.length;
            }
            averages.add(report.averageDamageFraction());
            for (int quarter = 1; quarter < 4; quarter++) {
                if (perQuarter[quarter - 1] > 0f) {
                    dips.add(1f - perQuarter[quarter] / perQuarter[quarter - 1]);
                }
            }
        }
        List<Float> pairSpikes = new ArrayList<>();
        for (TrialId[] pair : RISKIEST_PAIRS) {
            List<Float> spikes = new ArrayList<>();
            for (long seed : TRIAL_SEEDS) {
                float peak = 0f;
                for (WaveSample wave : simulator.runWithTrials(seed, pair[0], pair[1]).waves()) {
                    peak = Math.max(peak, wave.damageFraction());
                }
                spikes.add(peak);
            }
            pairSpikes.add(median(spikes));
        }
        float cardSpike = 0f;
        for (WaveSample wave : simulator.runWithForcedCard(CARD_SEED, RewardCardId.AGILITY, 1).waves()) {
            cardSpike = Math.max(cardSpike, wave.damageFraction());
        }

        List<String> lines = new ArrayList<>();
        lines.add("| Quantity | Measured now | Where it comes from |");
        lines.add("|---|---:|---|");
        lines.add(String.format(Locale.ROOT, "| Quarter means (fixed sweep) | `%.4f / %.4f / %.4f / %.4f` | "
            + "`WavePressureCurveTest`'s five seeds |", quarters[0], quarters[1], quarters[2], quarters[3]));
        lines.add(String.format(Locale.ROOT, "| Quarter steps | `x%.3f / x%.3f / x%.3f` | the same sweep |",
            quarters[1] / quarters[0], quarters[2] / quarters[1], quarters[3] / quarters[2]));
        lines.add(String.format(Locale.ROOT, "| Sweep average range | `%.4f - %.4f` | the same sweep, "
            + "inside the 0.15-0.55 band |", min(averages), max(averages)));
        lines.add(String.format(Locale.ROOT, "| Deepest single-seed quarter dip | `%.2f%%` against the "
            + "`25.00%%` plateau floor | the same sweep |", max(dips) * 100f));
        lines.add(String.format(Locale.ROOT, "| Elite contact multiplier, first half / second half | "
            + "`x%.1f / x%.1f` | `EnemyWaveSpawner` |",
            EnemyWaveSpawner.ELITE_DAMAGE_MULT, EnemyWaveSpawner.ELITE_SECOND_HALF_DAMAGE_MULT));
        lines.add(String.format(Locale.ROOT, "| Riskiest trial pairs, median spike | `%.4f / %.4f / %.4f` | "
            + "`TrialSimulationTest`'s five seeds, against the 1.90 ceiling |",
            pairSpikes.get(0), pairSpikes.get(1), pairSpikes.get(2)));
        List<String> names = new ArrayList<>();
        for (TrialId[] pair : RISKIEST_PAIRS) {
            names.add("`" + pair[0] + " + " + pair[1] + "`");
        }
        lines.add("");
        lines.add("The three pairs are the matrix's highest median spikes, in the order of the row: "
            + String.join(", ", names) + " (the other seventy-five pairs of the matrix run in the gate, not here).");
        lines.add(String.format(Locale.ROOT, "| Reward-card spike, AGILITY forced at boss 1 | `%.5f` | "
            + "`RewardCardSimulationTest`'s seed, against the 1.75 ceiling |", cardSpike));
        return join(lines);
    }

    private static String dropEconomy() {
        List<String> lines = new ArrayList<>();
        lines.add("| tier | rate per kill | share of the item budget | pool | sell price | coins per kill |");
        lines.add("|---|---|---|---|---|---|");
        float items = 0f;
        float coins = 0f;
        for (ItemTier tier : ItemTier.values()) {
            float rate = baseRate(tier);
            items += rate;
            coins += rate * sellPrice(tier);
            lines.add(String.format(Locale.ROOT, "| `%s` | %.4f%% | %.2f%% | %d | %d | %.4f |",
                tier, rate * 100f, rate / TOTAL_DROP_RATE * 100f, poolSize(tier), sellPrice(tier),
                rate * sellPrice(tier)));
        }
        lines.add(String.format(Locale.ROOT, "| **total** | %.4f%% | 100%% | %d | | **%.4f** |",
            items * 100f, EquipmentCatalog.all().size(), coins));
        return join(lines);
    }

    /**
     * The coin flow of the baseline seed, read from the simulator's own ledger rather than typed from a terminal.
     * The notes column is prose and stays in the document around this table.
     */
    private static String economyAudit() {
        BalanceSimulator simulator = new BalanceSimulator();
        simulator.run(CURVE_SEEDS[0]);
        BalanceSimulator.Ledger ledger = simulator.lastLedger();
        List<String> lines = new ArrayList<>();
        lines.add("| Flow | Coins | Count |");
        lines.add("|---|---:|---:|");
        lines.add(String.format(Locale.ROOT, "| Kill income | %d | |", ledger.killIncome));
        lines.add(String.format(Locale.ROOT, "| Item sales | %d | |", ledger.sellIncome));
        lines.add(String.format(Locale.ROOT, "| Stat shop | %d | %d levels |", ledger.statSpend, ledger.statLevels));
        lines.add(String.format(Locale.ROOT, "| Skill shop | %d | %d levels |", ledger.skillSpend, ledger.skillLevels));
        lines.add(String.format(Locale.ROOT, "| Anvil | %d | %d steps |", ledger.forgeSpend, ledger.forgeSteps));
        lines.add("");
        lines.add(spendSplit(simulator));
        return join(lines);
    }

    /**
     * The stability claim this section makes about the split, computed over the nine distinct gate seeds rather
     * than asserted: a spend table that holds across the seeds is what makes the baseline row above meaningful.
     */
    private static String spendSplit(BalanceSimulator simulator) {
        List<Long> seeds = new ArrayList<>();
        for (long seed : CURVE_SEEDS) {
            seeds.add(seed);
        }
        for (long seed : TRIAL_SEEDS) {
            if (!seeds.contains(seed)) {
                seeds.add(seed);
            }
        }
        List<Integer> statShares = new ArrayList<>();
        List<Integer> skillShares = new ArrayList<>();
        List<Integer> forgeShares = new ArrayList<>();
        for (long seed : seeds) {
            simulator.run(seed);
            BalanceSimulator.Ledger ledger = simulator.lastLedger();
            long spend = ledger.statSpend + ledger.skillSpend + ledger.forgeSpend;
            if (spend <= 0L) {
                continue;
            }
            statShares.add(Math.round(ledger.statSpend * 100f / spend));
            skillShares.add(Math.round(ledger.skillSpend * 100f / spend));
            forgeShares.add(Math.round(ledger.forgeSpend * 100f / spend));
        }
        return String.format(Locale.ROOT,
            "Across the %d gate seeds the split is stable: stats %d-%d%%, skills %d-%d%%, Anvil %d-%d%% of spend.",
            seeds.size(), minInt(statShares), maxInt(statShares), minInt(skillShares), maxInt(skillShares),
            minInt(forgeShares), maxInt(forgeShares));
    }

    private static int minInt(List<Integer> values) {
        return values.stream().min(Integer::compare).orElse(0);
    }

    private static int maxInt(List<Integer> values) {
        return values.stream().max(Integer::compare).orElse(0);
    }

    private static String ascensionBumps() {
        List<String> lines = new ArrayList<>();
        DifficultyCurve curve = new DifficultyCurve();
        lines.add("| Tier | Health growth per wave | Base health charge at wave 1 | Base damage charge at wave 1 |"
            + " Second-half entry | Final quarter | Damage at wave 1 | Damage at wave 200 |");
        lines.add("|---:|---:|---:|---:|---:|---:|---:|---:|");
        for (int tier : new int[] {0, 3, 6, 10}) {
            lines.add(String.format(Locale.ROOT, "| %d | %.4f | x%.2f | x%.2f | %.4f | %.4f | %.4f | %.4f |", tier,
                DifficultyCurve.healthGrowthForTier(tier),
                DifficultyCurve.baseScaleForTier(1, tier), DifficultyCurve.baseDamageScaleForTier(1, tier),
                DifficultyCurve.secondHalfHealthGrowthForTier(tier),
                DifficultyCurve.finalQuarterHealthGrowthForTier(tier),
                curve.baselineRegularDamage(1, tier), curve.baselineRegularDamage(GameState.FINAL_WAVE, tier)));
        }
        lines.add("");
        lines.add(String.format(Locale.ROOT,
            "The base charge (R4.7) is health `+%.0f%%` and damage `+%.0f%%` per tier at wave 1, fading linearly"
                + " to `+0%%` by wave %d.",
            DifficultyCurve.ASCENSION_BASE_HEALTH_BUMP_PER_TIER * 100f,
            DifficultyCurve.ASCENSION_BASE_DAMAGE_BUMP_PER_TIER * 100f,
            DifficultyCurve.ASCENSION_BASE_CHARGE_SPAN_WAVES + 1));
        lines.add("");
        lines.add(String.format(Locale.ROOT,
            "Elite waves arrive every %d / %d / %d / %d waves at tiers 0 / 3 / 6 / 10, and never on the"
                + " %d-wave boss lap (R4.8: the cadence used to step through that lap and left tiers 6-8 with no"
                + " elites at all).",
            EnemyWaveSpawner.eliteWaveInterval(0), EnemyWaveSpawner.eliteWaveInterval(3),
            EnemyWaveSpawner.eliteWaveInterval(6), EnemyWaveSpawner.eliteWaveInterval(10),
            EnemyWaveSpawner.BOSS_WAVE_INTERVAL));
        return join(lines);
    }

    private static String join(List<String> lines) {
        return String.join("\n", lines) + "\n";
    }

    private static float min(List<Float> values) {
        return values.stream().min(Float::compare).orElse(0f);
    }

    private static float max(List<Float> values) {
        return values.stream().max(Float::compare).orElse(0f);
    }

    private static float median(List<Float> values) {
        List<Float> sorted = new ArrayList<>(values);
        sorted.sort(Float::compare);
        return sorted.get(sorted.size() / 2);
    }

    private static int poolSize(ItemTier tier) {
        return (int) EquipmentCatalog.all().stream().filter(item -> item.tier() == tier).count();
    }

    private static float baseRate(ItemTier tier) {
        return switch (tier) {
            case COMMON -> ItemDropSystem.COMMON_RATE;
            case UNCOMMON -> ItemDropSystem.UNCOMMON_RATE;
            case RARE -> ItemDropSystem.RARE_RATE;
            case LEGENDARY -> ItemDropSystem.LEGENDARY_RATE;
            case MYTHIC -> ItemDropSystem.LEGENDARY_RATE * ItemDropSystem.MYTHIC_SHARE_OF_LEGENDARY;
        };
    }

    private static int sellPrice(ItemTier tier) {
        return EquipmentCatalog.all().stream()
            .filter(item -> item.tier() == tier)
            .findFirst()
            .orElseThrow()
            .createItem()
            .sellPrice;
    }
}
