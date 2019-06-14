package net.natruid.jungle.utils.ai.conditions;

import net.natruid.jungle.utils.types.UnitCondition;
import net.natruid.jungle.utils.types.UnitTargetType;
import net.natruid.jungle.utils.ai.BehaviorCondition;

public class SimpleUnitTargeter extends BehaviorCondition {
    private final UnitTargetType targetType;
    private final UnitCondition condition;

    public SimpleUnitTargeter(UnitTargetType targetType, UnitCondition condition, boolean saveResult) {
        super(saveResult);
        this.targetType = targetType;
        this.condition = condition;
    }

    public SimpleUnitTargeter(UnitTargetType targetType, UnitCondition condition) {
        this(targetType, condition, true);
    }

    @Override
    public boolean run() {
        int target = behaviorSystem.getUnit(getSelf(), targetType, condition);
        if (target < 0) return false;
        if (saveResult) getTargets().add(target);
        return true;
    }

    @Override
    public void reset() {
    }
}
