package net.natruid.jungle.events;

import com.artemis.annotations.EntityId;
import net.natruid.jungle.utils.PathNode;

import java.util.Deque;

public class UnitMoveEvent extends PooledEvent {
    @EntityId
    public int unit = -1;
    @EntityId
    public int targetTile = -1;
    public Deque<PathNode> path = null;
    public boolean free = false;

    @Override
    public void reset() {
        unit = -1;
        targetTile = -1;
        path = null;
        free = false;
    }
}
