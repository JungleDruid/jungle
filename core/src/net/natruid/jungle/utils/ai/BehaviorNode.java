package net.natruid.jungle.utils.ai;

import com.artemis.ComponentMapper;
import com.artemis.World;
import net.natruid.jungle.components.BehaviorComponent;
import net.natruid.jungle.systems.BehaviorSystem;

public abstract class BehaviorNode {
    public String name = "";
    protected BehaviorSystem behaviorSystem;
    protected ComponentMapper<BehaviorComponent> mBehavior;
    private int self = -1;

    protected int getSelf() {
        return self;
    }

    public void init(World world, int self) {
        assert self >= 0;
        this.self = self;
        world.inject(this);
    }

    public abstract void reset();

    public abstract boolean run();
}
