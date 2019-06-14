package net.natruid.jungle.utils;

public class Point {
    public static final ImmutablePoint[] adjacent = new ImmutablePoint[]{
        new ImmutablePoint(new Point(1, 0)),
        new ImmutablePoint(new Point(0, 1)),
        new ImmutablePoint(new Point(-1, 0)),
        new ImmutablePoint(new Point(0, -1))
    };

    public static final ImmutablePoint[] diagonals = new ImmutablePoint[]{
        new ImmutablePoint(new Point(1, 1)),
        new ImmutablePoint(new Point(-1, 1)),
        new ImmutablePoint(new Point(-1, -1)),
        new ImmutablePoint(new Point(1, -1))
    };

    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public Point set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Point set(Point target) {
        x = target.x;
        y = target.y;
        return this;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public Point plus(Point other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Point plus(ImmutablePoint other) {
        this.x += other.getX();
        this.y += other.getY();
        return this;
    }

    public Point minus(Point other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Point minus(ImmutablePoint other) {
        this.x -= other.getX();
        this.y -= other.getY();
        return this;
    }

    public Point times(Point other) {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    public Point times(ImmutablePoint other) {
        this.x *= other.getX();
        this.y *= other.getY();
        return this;
    }

    public Point times(int i) {
        this.x *= i;
        this.y *= i;
        return this;
    }

    public Point div(Point other) {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    public Point div(ImmutablePoint other) {
        this.x /= other.getX();
        this.y /= other.getY();
        return this;
    }
}
