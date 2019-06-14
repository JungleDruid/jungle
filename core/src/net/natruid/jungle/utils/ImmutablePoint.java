package net.natruid.jungle.utils;

public class ImmutablePoint {
    private final Point point;

    public ImmutablePoint(Point point) {
        this.point = point;
    }

    public int getX() {
        return point.x;
    }

    public int getY() {
        return point.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", point.x, point.y);
    }
}
