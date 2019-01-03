package pl.themolka.janusz.geometry;

import java.util.Objects;

public class Cuboid extends AbstractRegion {
    private final Vector3d min;
    private final Vector3d max;

    public Cuboid(Vector3d min, Vector3d max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");

        this.min = new Vector3d(Math.min(min.getX(), max.getX()),
                                Math.min(min.getY(), max.getY()),
                                Math.min(min.getZ(), max.getZ()));
        this.max = new Vector3d(Math.max(min.getX(), max.getX()),
                                Math.max(min.getY(), max.getY()),
                                Math.max(min.getZ(), max.getZ()));

        this.bounds = this.createBounds();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuboid cuboid = (Cuboid) o;
        return Objects.equals(min, cuboid.min) &&
                Objects.equals(max, cuboid.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public boolean contains(Vector3d vector) {
        Objects.requireNonNull(vector, "vector");
        return  this.min.getX() <= vector.getX() && vector.getX() <= this.max.getX() &&
                this.min.getY() <= vector.getY() && vector.getY() <= this.max.getY() &&
                this.min.getZ() <= vector.getZ() && vector.getZ() <= this.max.getZ();
    }

    public Vector3d getMin() {
        return this.min;
    }

    public Vector3d getMax() {
        return this.max;
    }

    @Override
    public String toString() {
        return "(" + this.min + " -> " + this.max + ")";
    }

    protected Bounds createBounds() {
        return new Bounds(this, this.min, this.max);
    }
}
