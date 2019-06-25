package net.natruid.jungle.utils.ai.actions;

import net.natruid.jungle.utils.ai.BehaviorAction;

public class OverrideBehaviorAction extends BehaviorAction {
    private final String behaviorName;

    public OverrideBehaviorAction(String behaviorName) {
        this.behaviorName = behaviorName;
    }

    @Override
    public Float evaluate() {
        return 0f;
    }

    @Override
    public boolean execute() {
        mBehavior.get(getSelf()).tree.overrideBehavior = behaviorName;
        return true;
    }

    @Override
    public void reset() {
    }
}
