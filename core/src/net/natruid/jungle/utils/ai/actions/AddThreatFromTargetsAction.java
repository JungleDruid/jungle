package net.natruid.jungle.utils.ai.actions;

import net.natruid.jungle.components.BehaviorComponent;
import net.natruid.jungle.utils.ai.BehaviorAction;

public class AddThreatFromTargetsAction extends BehaviorAction {
    @Override
    public Float evaluate() {
        return 0f;
    }

    @Override
    public boolean execute() {
        for (int target : getTargets()) {
            BehaviorComponent behavior = mBehavior.get(getSelf());
            Float threat = behavior.threatMap.get(target);
            if (threat == null) threat = 0f;
            behavior.threatMap.put(target, threat + 1);
        }
        return true;
    }

    @Override
    public void reset() {
    }
}
