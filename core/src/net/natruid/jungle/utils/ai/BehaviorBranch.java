package net.natruid.jungle.utils.ai;

import com.artemis.World;

import java.util.ArrayList;
import java.util.Collections;

public abstract class BehaviorBranch extends BehaviorNode {
    public final ArrayList<BehaviorNode> children = new ArrayList<>();

    public void addBehaviors(BehaviorNode... nodes) {
        Collections.addAll(children, nodes);
    }

    @Override
    public void init(World world, int self) {
        super.init(world, self);
        for (BehaviorNode child : children) {
            child.init(world, self);
        }
    }

    @Override
    public void reset() {
        for (BehaviorNode child : children) {
            child.reset();
        }
    }
}
