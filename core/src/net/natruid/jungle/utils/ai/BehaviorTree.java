package net.natruid.jungle.utils.ai;

import net.natruid.jungle.components.BehaviorComponent;

public class BehaviorTree extends BehaviorBranch {
    public String overrideBehavior = "";

    @Override
    public boolean run() {
        if (!overrideBehavior.isEmpty()) {
            for (BehaviorNode child : children) {
                if (child.name.equals(overrideBehavior)) return child.run();
            }
        }
        boolean hasOne = false;
        for (BehaviorNode child : children) {
            if (child.run()) hasOne = true;
        }
        return hasOne;
    }

    @Override
    public void reset() {
        if (getSelf() < 0) return;
        {
            BehaviorComponent it = mBehavior.get(getSelf());
            it.targets.clear();
            it.moveArea = null;
            it.fullMoveArea = null;
            it.score = 0f;
            it.execution = null;
        }
        super.reset();
    }
}
