package net.natruid.jungle.utils.ai;

public abstract class BehaviorCondition extends BehaviorLeaf {
    protected final boolean saveResult;

    public BehaviorCondition(boolean saveResult) {
        this.saveResult = saveResult;
    }
}
