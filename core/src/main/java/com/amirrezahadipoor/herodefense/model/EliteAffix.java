package com.amirrezahadipoor.herodefense.model;

/**
 * The fixed Elite affix pool (Phase 25.2): blightburst explodes on death,
 * rootward_ward periodically shields, weeping_rot leaves a damaging trail.
 *
 * <p>The pool is now twelve, in three bands of three: the shipped three above, roadmap D2's death-and-armour
 * band below them, and the deep band's six -- stoneshell, gravebloom, swarmcall, spitebarb, hammerfall and
 * bloodhowl -- which are the affixes of a night that has already learned you. Every one of them is a question
 * about the wave rather than a bigger number: harden, refill, punish closeness, warn, and quicken.
 *
 * <p>Roadmap D2 doubles the pool, but only the deep run sees the new three: the spawner draws from the
 * first {@link #BASE_POOL_SIZE} until {@code EnemyWaveSpawner.LATE_AFFIX_WAVE}, so every early and mid
 * roll stays bit-identical to the shipped curve and the frozen balance baseline keeps its meaning.
 * hollowmolt splits into husks on death, gravemoss regrows its own health, cinderhalo burns whoever
 * stands too close too long.
 */
public enum EliteAffix {
    BLIGHTBURST("blightburst"),
    ROOTWARD_WARD("rootward_ward"),
    WEEPING_ROT("weeping_rot"),
    HOLLOWMOLT("hollowmolt"),
    GRAVEMOSS("gravemoss"),
    CINDERHALO("cinderhalo"),
    /** Harden: armours itself for a long window on a slow clock; no share, no ally, just armour. */
    STONESHELL("stoneshell"),
    /** Refill: dies and leaves the ground angry where it stood. */
    GRAVEBLOOM("gravebloom"),
    /** Replace: keeps calling children out of the treeline, so killing it opens a door instead of ending a wave. */
    SWARMCALL("swarmcall"),
    /** Punish closeness: a tight, slow, heavy return on anything that hugs it. */
    SPITEBARB("spitebarb"),
    /** Warn: raises its arm over the ground it is about to break, and the ground says where. */
    HAMMERFALL("hammerfall"),
    /** Quicken: an always-on aura that hurries the line it stands in. */
    BLOODHOWL("bloodhowl");

    /** How many affixes the shallow run draws from; the enum's first three, in shipped order. */
    public static final int BASE_POOL_SIZE = 3;

    private final String id;

    EliteAffix(String id) {
        this.id = id;
    }

    /** Stable id used by saves, codex triggers, and kill counts; never rename. */
    public String id() {
        return id;
    }

    public static EliteAffix fromId(String id) {
        if (id == null) return null;
        for (EliteAffix affix : values()) {
            if (affix.id.equals(id)) return affix;
        }
        return null;
    }
}
