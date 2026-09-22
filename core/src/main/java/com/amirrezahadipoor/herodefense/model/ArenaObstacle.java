package com.amirrezahadipoor.herodefense.model;

/**
 * One solid piece of ground: a crystal outcrop nobody walks through, and -- when it stands tall -- an arrow does
 * not pass either.
 *
 * <p>Two heights, and the difference is the layout's whole vocabulary. A <em>standing</em> outcrop is taller than
 * a body: it stops feet and shafts alike, which is cover you can shoot from and cover you have to shoot around. A
 * low one -- a hedge, a broken kerb, a rubble of the fallen ring -- stops feet only, so a fight can be fought
 * across it. {@code ArenaTerrainTest} holds both halves: the same body is refused by a low outcrop and an arrow
 * crosses it.
 *
 * <p>Radius is the collision circle; {@code drawn} is the width the renderer paints, which is larger than the
 * circle because a body stands on the ground rather than inside the painted sprite. Height drives the shadow's
 * offset so a taller outcrop casts from its own base.
 */
public final class ArenaObstacle {

    public float x;
    public float y;
    public float radius;
    public float drawn;
    public float height;
    public int variant;
    /** True when this outcrop is tall enough to stop an arrow as well as a body. */
    public boolean shelters;

    public ArenaObstacle(
        float x, float y, float radius, float drawn, float height, int variant, boolean shelters
    ) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.drawn = drawn;
        this.height = height;
        this.variant = variant;
        this.shelters = shelters;
    }

    /** True when a body of {@code bodyRadius} centred at the point overlaps this obstacle. */
    public boolean covers(float pointX, float pointY, float bodyRadius) {
        float limit = radius + Math.max(0f, bodyRadius);
        float dx = pointX - x;
        float dy = pointY - y;
        return dx * dx + dy * dy < limit * limit;
    }

    /**
     * The squared distance from this obstacle's centre to the segment, in the slab form that avoids a division on
     * every call: the point projection is clamped by comparing the projection parameter to 0 and 1.
     */
    public float squaredDistanceToSegment(float fromX, float fromY, float toX, float toY) {
        float dx = toX - fromX;
        float dy = toY - fromY;
        float lengthSquared = dx * dx + dy * dy;
        float t = 0f;
        if (lengthSquared > 0f) {
            t = ((x - fromX) * dx + (y - fromY) * dy) / lengthSquared;
            t = Math.max(0f, Math.min(1f, t));
        }
        float nearestX = fromX + dx * t;
        float nearestY = fromY + dy * t;
        float offsetX = x - nearestX;
        float offsetY = y - nearestY;
        return offsetX * offsetX + offsetY * offsetY;
    }

    /** True when a body of {@code bodyRadius} sweeping the segment would touch this obstacle. */
    public boolean blocksSegment(float fromX, float fromY, float toX, float toY, float bodyRadius) {
        float limit = radius + Math.max(0f, bodyRadius);
        return squaredDistanceToSegment(fromX, fromY, toX, toY) < limit * limit;
    }
}
