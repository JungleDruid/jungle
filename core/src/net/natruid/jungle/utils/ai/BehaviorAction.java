package net.natruid.jungle.utils.ai;

import net.mostlyoriginal.api.event.common.EventSystem;
import net.natruid.jungle.components.BehaviorComponent;

public abstract class BehaviorAction extends BehaviorLeaf {
    protected EventSystem es;

    @Override
    public boolean run() {
        Float score = evaluate();
        if (score == null) return false;
        BehaviorComponent behaviorComponent = mBehavior.get(getSelf());
        if (score > behaviorComponent.score || behaviorComponent.execution == null) {
            behaviorComponent.score = score;
            behaviorComponent.execution = this;
        }
        return true;
    }

    public abstract Float evaluate();

    public abstract boolean execute();
}
