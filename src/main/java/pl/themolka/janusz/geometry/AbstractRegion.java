package pl.themolka.janusz.geometry;

public abstract class AbstractRegion implements Region {
    protected Bounds bounds;

    @Override
    public Bounds getBounds() {
        return this.bounds;
    }
}
