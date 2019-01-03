package pl.themolka.janusz.geometry;

import java.util.Objects;

public class Vector3d {
    private final double x;
    private final double y;
    private final double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3d vector3d = (Vector3d) o;
        return  Double.compare(vector3d.x, x) == 0 &&
                Double.compare(vector3d.y, y) == 0 &&
                Double.compare(vector3d.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public Vector3d add(double x, double y, double z) {
        return new Vector3d(this.x + x, this.y + y, this.z + z);
    }

    public double getX() {
        return this.x;
    }

    public int getFineX() {
        return (int) this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getFineY() {
        return (int) this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getFineZ() {
        return (int) this.z;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + "," + this.z + ")";
    }
}
