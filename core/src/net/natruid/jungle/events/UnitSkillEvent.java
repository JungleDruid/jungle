package net.natruid.jungle.events;

import net.natruid.jungle.utils.PathNode;

import java.util.Deque;

public class UnitSkillEvent extends PooledEvent {
    public int unit = -1;
    public int skill = -1;
    public int target = -1;
    public Deque<PathNode> path = null;

    @Override
    public void reset() {
        unit = -1;
        skill = -1;
        target = -1;
        path = null;
    }
}
