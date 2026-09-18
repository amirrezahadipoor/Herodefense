# Static analysis: what the two analysers see, and what was done about it

Roadmap item **R2.5 — "Static analysis in CI (ErrorProne or SpotBugs + PMD), findings triaged not silenced."**
This file is the triage record: every finding class is either **fixed in code** or **excluded with a reason that is
written down here and in the config file that excludes it**. There is no third category, and no rule was switched
off because it was noisy.

## How it runs

| where | what | how to run locally |
| --- | --- | --- |
| `core/config/pmd/ruleset.xml` | PMD 7.7.0 over `core/src/main/java` — `errorprone`, `bestpractices`, `performance` | `./gradlew :core:pmdMain` |
| `core/config/pmd/ruleset-tests.xml` | the same rules over `core/src/test/java`, minus four test-shape rules (below) | `./gradlew :core:pmdTest` |
| `core/config/spotbugs/exclude.xml` | SpotBugs 4.8.6 (plugin 6.0.26, effort MAX, confidence LOW) over bytecode of main and test | `./gradlew :core:spotbugsMain :core:spotbugsTest` |
| `ciStaticAnalysis` | all four of the above in one task; fails the build on any finding that is not excluded | `./gradlew :core:ciStaticAnalysis` |

The analysers are **not** wired into `test`: the unit loop (`:core:test`) and the balance gate
(`:core:balanceGate`, roadmap R4.5) keep their pace, and CI runs `ciStaticAnalysis` as its own step next to both. Both analysers read the code that ships *and* the
tests, because a broken test is as expensive as a broken system.

## First pass: what was found

| analyser | findings at first run | after the fix pass |
| --- | --- | --- |
| PMD (main) | 306 | 0 |
| SpotBugs (main) | 171 | 0 |
| PMD (test) | 3,198 | 0 |
| SpotBugs (test) | 7 | 0 |

## Fixed in code (not excluded)

**Real defects and hazards**

* `BalanceSimulator.applyRootBonusesForTier` built a `nodesToApply` budget that nothing read (dead store, 4
  assignments) — removed.
* `EnemyMovementSystem`: the legacy second-tree branch stored a distance it never read again — removed, with a
  comment naming the branch's actual behaviour.
* `ArchitectureRatchet`: iterated `keySet()` and looked every value up again (`WMI_WRONG_MAP_ITERATOR`) — now
  iterates `entrySet()`.
* `EliteFragments.fragmentFor`: `% 2 == 1` is wrong for negative inputs (`IM_BAD_CHECK_FOR_ODD`) — now a parity
  mask, `(Math.max(1, killCount) & 1) == 1`.
* `ParticleSystem.emitTreeDestruction`: an integer division was cast to float (`ICAST_IDIV_CAST_TO_DOUBLE`) — the
  row is now a named `int`, which is what the code always meant.
* `Particle`: the jitter seed used a hand-rounded `6.2831f` circle (`CNT_ROUGH_CONSTANT_VALUE`) — now `2π`.
* `HeroDefenseGame`: `handledTouchUpCount` was a `volatile long` incremented from the input callback
  (`VO_VOLATILE_INCREMENT`) — now an `AtomicLong`.
* `RootNetworkSystem.purchase`: `toUpperCase()` without a locale (`DM_CONVERT_CASE`) — now `Locale.ROOT`.
* `RootNetworkSystem` kept a feedback node id that was written and never read, and a parameter that only fed it —
  both removed; `showFeedback` now takes just the line.
* `GameState`: the deprecated `secondTreePlanted` sync was assigned twice in one method (the first store was dead)
  and two `size() < 1` guards now read `isEmpty()`.
* `GameFonts`/`ScreenEdges`: a redundant field initialiser, and a deliberate `= null` release documented by the
  rule's exclusion instead of a magic value.
* Ten lenient parsers (`Enemy.type`, `Boss.bossDefinition`, `EquipmentSlot.parse`, `ItemTier.parse`,
  `SkillId.parse`, `VisualRarity.fromTier`, `CodexSystem.parseInt`, `DropPickupSystem`, `BossRewardCardSystem`,
  `CombatEntityRenderer.dropTexturePath`) caught `NullPointerException` to mean "bad persisted value"
  (`DCN_NULLPOINTER_EXCEPTION`) — they now check for `null` explicitly and keep the `IllegalArgumentException`
  path. Parsing behaviour is unchanged; the intent is now visible.
* `TestIntegrityTest` / `AssetIntegrityTest`: `path.getFileName()` can be null on a root path
  (`NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`) — guarded.
* `RuntimeResidency.IconSizes`: documented and implemented as "empty array, never null" for an unreadable icon
  header (`PZLA_PREFER_ZERO_LENGTH_ARRAYS`), with the caller checking `size.length == 2`.
* `WaveOmenTest` called the same deterministic function twice and discarded the first result.

**Dead code and duplication**

* `HeroDefenseGame.startNewRun()` and `SaplingTreeRenderer.drawFrame(...)` were uncalled private methods (PMD and
  SpotBugs agreed) — removed.
* `EquipmentCatalog`: `"verdant_covenant"` ×4 and `"bastion_oath"` ×4 became named constants;
  `CodexOverlayRenderer`'s `" / "` ×4 likewise.
* `PlantingCeremony.heroFrame()` had two identical switch branches (the two gesture clips are the same length) and
  a two-branch switch that reads better as an `if`.
* `FloatingDamageTextRenderer.setColor`: `CRITICAL` and `COIN` shared a literal colour — one branch now.
* `GameFlowController` / `InternalAssetReferences`: `EnumSet` / `TreeSet` in signatures became `Set`.
* `EquipmentSetBonus`: the "last element" test compared references — now an index loop.
* `EnemyWaveSpawner.spawnRegularEnemies` reassigned its `count` parameter — now a local.
* `BossTitleCards`: a `continue` as the last statement of the loop body became a positive condition.
* `SaplingTreeRenderer`, `ScreenEdges`, `CombatEntityRenderer`: one declaration per line, no redundant
  initialiser.

**Test hygiene**

* `UiFrameRendererTest` now holds the renderer in try-with-resources (`CloseResource`), so a failing assertion can
  no longer leak GL resources.
* Two float loop indices (`PremiumVfxRestraintTest`, `BossTelegraphPresentationTest`) became integer steps times a
  float timestep (`DontUseFloatTypeForLoopIndices`).
* `EnumSet` for enum-keyed sets (`EnemyWaveSpawnerTest`, `WaveOmenTest`, `ElitePresentationTest`), `Set` interfaces
  in `EquipmentCatalogTest` / `RewardCardPoolTest`, literal-first string comparisons in eight places, a
  one-declaration-per-line split, a reused `StringBuilder` in `AssetIntegrityTest`, and unused locals/fields removed
  from `MythicOwnArtContractTest`, `PremiumAssetContractTest`, `FrameDriverTest`, `StoryLayoutTest`,
  `PremiumArenaAssetContractTest`.
* `TrialSimulationTest`'s pressured-wave floor is a named constant computed once instead of `Math.ceil` on a
  constant expression at every call.
* R7.3's shaping pipeline arrived with four findings and all four were fixed rather than excluded: `reverse` in
  `BidiReordering` swapped through two locals instead of walking its `from`/`to` parameters (`AvoidReassigningParameters`),
  `containsArabicScript` became `text.codePoints().anyMatch(…)` instead of a `for` loop that advanced its own
  control variable by `Character.charCount` (`AvoidReassigningLoopVariables`), and the three embedding-level
  parity tests changed from `level % 2 == 1` to `(level & 1) == 1` (`IM_BAD_CHECK_FOR_ODD` — levels are never
  negative here, but the modulo form would be wrong if one ever were, and the bitwise form says what is meant).

## Excluded, with the reason

Each entry is also a comment in the config file, next to the exclusion it explains.

| rule (analyser) | hits | why it is excluded |
| --- | --- | --- |
| `AvoidFieldNameMatchingMethodName` (PMD) | 145 | The project names an accessor after the field it exposes (`state()`/`state`, `bossType()`/`bossType`), as records do. Renaming 145 members would churn the simulation's API without changing behaviour. |
| `AvoidLiteralsInIfCondition` (PMD) | 99 | Gameplay thresholds are inline where the number *is* the rule (`if (life > 0.4f)`). Shared or tuned values already live in named constants. |
| `NullAssignment` (PMD) | 15 | Reference releases: a closed font set, a run being reset, a consumed drop. Setting the reference to `null` is the intent; a tombstone object would say it less clearly. |
| `UnusedAssignment` (PMD) | 2 | Record compact constructors (`events = events == null ? … : List.copyOf(events)`), where reassigning the component parameter is exactly how the canonical constructor stores the field. PMD 7 does not model that. |
| `CompareObjectsWithEquals` (PMD) | 7 | Deliberate identity checks: excluding one entity instance from a target scan, a same-object fast path in item details, and the shared-font owner checks (`shared == this`, `shared.owner == application`). |
| `AvoidInstantiatingObjectsInLoops` (PMD) | 6 | Construction-time catalog lists and per-frame renderer labels. Frame cost is measured on device (R8.4, R13.1); the profiler, not a style rule, decides whether a draw-loop allocation is worth removing. |
| `AbstractClassWithoutAbstractMethod` (PMD) | 1 | `ArenaEntity` is a shared base holding common combat fields for its subclasses — reuse, not an abstract contract. |
| `UseVarargs` (PMD) | 5 | The Persian shaping pipeline (R7.3), where an `int[]` is a buffer of codepoints being worked through, not a caller's argument list: `reshape`, `stripHarakat`, `isAscii`, `matchesAt`, `baseLevel`. The arrays come from `String.codePoints().toArray()`, which is not a varargs call site, and varargs would let `reshape(0x0644, 0x0627)` compile and mean something else. |
| `UnitTestAssertionsShouldIncludeMessage` (PMD) | 2,505 | The sweeps assert a property per seed/wave; the 2,500 reports are those loops. The gates that fail a build print their measurement table instead. |
| `UnitTestContainsTooManyAsserts` (PMD) | 553 | Balance and contract tests deliberately check several properties of one object in one test, because they assert a single contract (a wave's roster, a sheet's metadata). |
| `SimplifiableTestAssertion` (PMD) | 28 | Several contract tests assert a boolean property (a glow flag, a distinct-art check) rather than equality; the suggested `assertEquals` would change what is documented. |
| `SystemPrintln` (PMD) | 25 | The sweeping gates print their CSV tables into the CI log on purpose — those lines are the evidence that a band was measured rather than asserted. |
| `AvoidBranchingStatementAsLastInLoop` (PMD, tests) | 5 | Seed sweeps whose body ends in `return` when the case is found. The explicit early return states "this is the case, stop sweeping" more clearly than a found-flag plus `break`. |
| `AvoidDuplicateLiterals` (PMD, tests) | 52 | A seed, wave number or id is spelled out at its use, which is what makes a failing message readable; sharing them into constants would hide which case failed. |
| `AvoidAccessibilityAlteration` / `DP_DO_INSIDE_DO_PRIVILEGED` (PMD / SpotBugs) | 2 + 2 | The layer-order test reads one private member **through reflection**; that is the point of the test, and the alternative is widening production visibility for a test's sake. |
| `PA_PUBLIC_PRIMITIVE_ATTRIBUTE`, `PA_PUBLIC_MUTABLE_OBJECT_ATTRIBUTE` (SpotBugs) | 95 + 2 | `GameState`, `Hero`, `Enemy`, `HeroStats`, `TrophyLedger` and `GameSettings` **are** the simulation's data model; the systems read and write those fields directly, which is why the balance simulators can replay a run without a rendering layer. |
| `EI_EXPOSE_REP2` (SpotBugs) | 27 | Constructor injection of collaborators: a system, controller or renderer is handed the objects it drives. Copying shared mutable state instead would break determinism. |
| `EI_EXPOSE_REP` (SpotBugs) | 3 | Accessors that deliberately hand back a live object (the host's own `GameState`, the forge behind the inventory controller). |
| `FE_FLOATING_POINT_EQUALITY` (SpotBugs) | 2 | Exact tie-breakers between two computed floats (rot-trail damage, focus-fire distance). An epsilon would silently change which candidate wins — a gameplay change, not a cleanup. |
| `URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD` (SpotBugs) | 5 | Fields the test suite and the save format read (`Boss.uniqueAttack`, `Enemy.spawnLane`, `Hero.currentTargetId`, `GameState.schemaVersion`, `GameState.bareHandedEligible`); SpotBugs only sees the main source set. |
| `LI_LAZY_INIT_STATIC`, `SING_SINGLETON_GETTER_NOT_SYNCHRONIZED`, `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` (SpotBugs) | 2 + 1 + 1 | All `GameFonts.shared`. The game runs entirely on the LibGDX render thread; the shared font set is created there, replaced when a new `Application` starts and released from `close()`. A lock would protect nothing. |

## What this buys

* A rule that fires is now a defect by definition: the report is empty on a green run, so a new finding is visible
  in the diff of the report rather than buried in 300 standing warnings.
* The exclusions are per-rule and per-reason, so a *new* class of finding under an excluded rule is still a
  surprise the reviewer sees in this table growing.
* The reasons above double as the record of design decisions (public simulation model, render-thread lifecycle,
  deterministic tie-breaks), which is the part a reviewer actually needs.
