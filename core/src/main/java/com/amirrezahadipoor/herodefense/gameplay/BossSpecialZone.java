package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Where a boss special will land, and who is standing in it (audit item 2, closing roadmap A5).
 *
 * <p><b>The problem this closes.</b> A special was a damage number with a warning light attached: the telegraph was
 * drawn as a ring centred on the hero, the damage landed on the hero wherever the hero was, and the only way to
 * reduce it was the brace. The audit measured the result -- the loudest warning in the game was worth 0.46% of the
 * bar, and no amount of movement could change the outcome -- and it is the reason the game had no positional
 * decision in it: the step and the brace were two answers to nothing.
 *
 * <p><b>What it is instead.</b> Every special plants a zone when its telegraph starts, in the shape its identity
 * implies, and the damage is resolved against the hero's position when the telegraph ends:
 *
 * <table>
 * <caption>Zone by identity</caption>
 * <tr><th>Identity</th><th>Zone</th><th>Why</th></tr>
 * <tr><td>Golem, Titan (ground slam)</td><td>circle at the <em>boss</em>, 130 units</td>
 *     <td>Backing off the body is the answer, so standing in melee is a decision.</td></tr>
 * <tr><td>Wyrm, Colossus (flame sweep)</td><td>cone from the <em>boss</em>, 30 degrees each side, 300 units</td>
 *     <td>Sidestep it: the answer is sideways, not backwards, and it is the one zone a run can outrun along its own
 *     axis.</td></tr>
 * <tr><td>Matriarch, Avatar (thorn cage)</td><td>circle at the <em>hero</em>, 80 units</td>
 *     <td>Leave the ground you are standing on -- 0.5 s of walking is 82 units, so it is exactly a decision made on
 *     reaction rather than a formality.</td></tr>
 * <tr><td>Knight, Lich (void strike)</td><td>circle at the <em>hero</em>, 80 units, after the dash closes</td>
 *     <td>Same as the cage, but the boss is already in your face when the warning appears.</td></tr>
 * </table>
 *
 * <p>The numbers are chosen against measured numbers rather than taste: {@code TELEGRAPH_SECONDS} is 0.5 s and the
 * hero walks at 165 units a second, so an 80-unit zone is 97% of what a walk buys in the warning window -- you have
 * to move the instant the tell appears, and the step is the reliable answer. The cone's point-blank radius is what
 * stops a hero from standing inside the boss as a universal dodge.
 */
public final class BossSpecialZone {

    /** The slam's radius around the boss: further than the boss's own reach, so melee is inside it. */
    /**
     * The disc the slam draws around the body that slammed.
     *
     * <p>It is sized against the ring its identity starts the special at -- the golem and the titan trigger it at
     * 145 units, the widest stand-off either of them ever takes -- so the ground the warning paints is ground the
     * hero was standing on when it appeared. A 130-unit disc would have warned about a ring the hero had already
     * left, which is a warning that lies: the telegraph is a question (leave, or pay for it), and a question with
     * one answer is not a decision.
     */
    public static final float SLAM_RADIUS = 165f;
    /** The sweep's cone: half-angle and reach, plus the radius inside which the cone always catches the hero. */
    public static final float SWEEP_HALF_ANGLE_RADIANS = (float) Math.toRadians(30.0);
    public static final float SWEEP_REACH = 300f;
    public static final float POINT_BLANK_RADIUS = 70f;
    /** The zones that land where the hero stands, sized so that 0.5 s of walking almost clears them. */
    public static final float PLANTED_RADIUS = 80f;

    private BossSpecialZone() {
    }

    /** Plants the pending special's zone. Called once, when the telegraph starts. */
    public static void plant(Boss boss, GameState state) {
        if (boss == null || state == null || state.hero == null) {
            return;
        }
        BossType type = boss.bossDefinition();
        boss.specialZoneCone = isSweep(type);
        boss.specialZoneHalfAngle = SWEEP_HALF_ANGLE_RADIANS;
        boss.specialZoneReach = SWEEP_REACH;
        // The sweep is an arc out of the boss aimed at where the hero is standing when the warning appears, so its
        // origin is the boss and its angle is the direction the hero was caught in; the planted zones -- the slam,
        // the cage, the void strike -- are discs on the ground itself.
        float aimedRadians = (float) Math.atan2(state.hero.y - boss.y, state.hero.x - boss.x);
        if (isSlam(type)) {
            boss.specialZoneX = boss.x;
            boss.specialZoneY = boss.y;
            boss.specialZoneRadius = SLAM_RADIUS;
            boss.specialZoneAngleRadians = aimedRadians;
            return;
        }
        if (boss.specialZoneCone) {
            boss.specialZoneX = boss.x;
            boss.specialZoneY = boss.y;
            boss.specialZoneRadius = PLANTED_RADIUS;
            boss.specialZoneAngleRadians = aimedRadians;
            return;
        }
        boss.specialZoneX = state.hero.x;
        boss.specialZoneY = state.hero.y;
        boss.specialZoneRadius = PLANTED_RADIUS;
        boss.specialZoneAngleRadians = aimedRadians;
    }

    /** True when the hero's position at detonation is inside the planted zone. */
    public static boolean contains(Boss boss, float x, float y) {
        if (boss == null) {
            return false;
        }
        if (boss.specialZoneCone) {
            return inCone(boss, x, y);
        }
        return distanceSquared(boss.specialZoneX, boss.specialZoneY, x, y)
            <= boss.specialZoneRadius * boss.specialZoneRadius;
    }

    /** True when the encounter's special is an arc from the boss rather than a circle on the ground. */
    public static boolean isSweep(BossType type) {
        return type == BossType.EMBER_WYRM || type == BossType.STORM_COLOSSUS;
    }

    private static boolean isSlam(BossType type) {
        return type == BossType.ANCIENT_GOLEM || type == BossType.FROST_TITAN;
    }

    private static boolean inCone(Boss boss, float x, float y) {
        float dx = x - boss.specialZoneX;
        float dy = y - boss.specialZoneY;
        float distanceSquared = dx * dx + dy * dy;
        if (distanceSquared <= POINT_BLANK_RADIUS * POINT_BLANK_RADIUS) {
            return true;
        }
        if (distanceSquared > boss.specialZoneReach * boss.specialZoneReach) {
            return false;
        }
        float heroAngle = (float) Math.atan2(dy, dx);
        float delta = Math.abs(normalise(heroAngle - boss.specialZoneAngleRadians));
        return delta <= boss.specialZoneHalfAngle;
    }

    private static float normalise(float radians) {
        float value = radians;
        while (value > Math.PI) {
            value -= (float) (Math.PI * 2.0);
        }
        while (value < -Math.PI) {
            value += (float) (Math.PI * 2.0);
        }
        return value;
    }

    private static float distanceSquared(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return dx * dx + dy * dy;
    }
}
