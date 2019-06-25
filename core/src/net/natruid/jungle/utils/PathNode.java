package net.natruid.jungle.utils;

import com.badlogic.gdx.utils.BinaryHeap;

public class PathNode extends BinaryHeap.Node {
    public int tile;
    public float cost;
    public PathNode prev;

    public PathNode(int tile, float cost, PathNode prev) {
        super(0);
        this.tile = tile;
        this.cost = cost;
        this.prev = prev;
    }

    public PathNode(int tile, float cost) {
        this(tile, cost, null);
    }
}
