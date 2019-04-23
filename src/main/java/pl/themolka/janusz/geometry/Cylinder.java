package pl.themolka.janusz.geometry;

import org.apache.commons.lang.Validate;

import java.util.Objects;

public class Cylinder extends AbstractRegion {
    private final Vector3d origin;
    private final double radius;
    private final double height;

    public Cylinder(Vector3d origin, double radius, double height) {
        Objects.requireNonNull(origin, "origin");
        Validate.isTrue(radius > 0, "radius must be positive");
        Validate.isTrue(height >= 0, "radius cannot be negative");

        this.origin = origin;
        this.radius = radius;
        this.height = height;

        this.bounds = this.createBounds();
    }

    @Override
    public boolean contains(Vector3d vector) {
        Objects.requireNonNull(vector, "vector");
        double power = Math.pow(vector.getX() - this.origin.getX(), 2) +
                       Math.pow(vector.getZ() - this.origin.getZ(), 2);

        if (this.height == 0D) {
            return power <= Math.pow(this.radius, 2);
        }

        return  this.origin.getY() <= vector.getY() &&
                this.getHighestY() >= vector.getY() &&
                power <= Math.pow(this.radius, 2);
    }

    public Vector3d getOrigin() {
        return this.origin;
    }

    public double getRadius() {
        return this.radius;
    }

    public double getDiameter() {
        return this.radius * 2D;
    }

    public double getHeight() {
        return this.height;
    }

    public double getHighestY() {
        return this.origin.getY() + this.height;
    }

    protected Bounds createBounds() {
        return new Bounds(this,
                this.origin.add(-this.radius, 0D, -this.radius),
                this.origin.add(this.radius, this.height, this.radius));
    }
}
