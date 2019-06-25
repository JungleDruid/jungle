package net.natruid.jungle.utils.ai.conditions;

import net.natruid.jungle.utils.ai.BehaviorCondition;

public class IsOverrideBehaviorCondition extends BehaviorCondition {
    private final String behaviorName;

    public IsOverrideBehaviorCondition(String behaviorName) {
        super(false);
        this.behaviorName = behaviorName;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean run() {
        return mBehavior.get(getSelf()).tree.overrideBehavior.equals(behaviorName);
    }
}
