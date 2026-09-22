package com.amirrezahadipoor.herodefense.model;

/** Optional visual/gameplay projectile emitted by Hero attacks or boss abilities. */
public final class Projectile extends ArenaEntity {
    public long sourceId;
    public long targetId;
    public float velocityX;
    public float velocityY;
    public float damage;
    public float remainingLifetimeSeconds;
    public boolean critical;
    /** Extra arrows from Multi Shot deal a reduced share and never chain again. */
    public boolean secondary;
    /** True once the arrow has buried itself in solid ground: it deals nothing and is drawn where it stopped. */
    public boolean lodged;
    /** Seconds a lodged arrow stays drawn before it disappears. */
    public float lodgedSeconds;
    /**
     * The flight angle, in degrees, of an arrow that lodged.
     *
     * <p>The velocity is zeroed the moment an arrow lodges -- it has to stop, that is what lodging means -- so an
     * arrow drawn from its velocity afterwards would keep flying into the rock at whatever angle the renderer
     * guessed. The angle it arrived at is the one detail of the shot the stone keeps.
     */
    public float lodgedAngleDegrees;

    public Projectile() {
        super();
    }

    public Projectile(long id, long sourceId, long targetId, float x, float y) {
        super(id, x, y);
        this.sourceId = sourceId;
        this.targetId = targetId;
    }
}
