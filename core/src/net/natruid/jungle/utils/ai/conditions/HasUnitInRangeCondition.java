package net.natruid.jungle.utils.ai.conditions;

import com.artemis.ComponentMapper;
import net.natruid.jungle.components.AttributesComponent;
import net.natruid.jungle.systems.TileSystem;
import net.natruid.jungle.utils.types.UnitTargetType;
import net.natruid.jungle.utils.ai.BehaviorCondition;
import net.natruid.jungle.utils.types.AttributeType;

import java.util.List;

public class HasUnitInRangeCondition extends BehaviorCondition {
    private final float range;
    private final UnitTargetType group;
    private final boolean awarenessMod;

    private ComponentMapper<AttributesComponent> mAttributes;
    private TileSystem tileSystem;

    public HasUnitInRangeCondition(float range, UnitTargetType group, boolean awarenessMod, boolean saveResult) {
        super(saveResult);
        this.range = range;
        this.group = group;
        this.awarenessMod = awarenessMod;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean run() {
        List<Integer> targets = behaviorSystem.getUnitGroup(group);
        float maxDistance = range * ((!awarenessMod) ? 1f : 1f) + (mAttributes.get(getSelf()).get(AttributeType.AWARENESS) - 10) * 0.05f;
        boolean result = false;
        for (int target : targets) {
            if (mUnit.get(target) != null) {
                if (tileSystem.getDistance(mUnit.get(getSelf()).tile, mUnit.get(target).tile) <= maxDistance) {
                    if (saveResult) {
                        result = true;
                        mBehavior.get(getSelf()).targets.add(target);
                    } else {
                        return true;
                    }
                }
            }
        }
        return result;
    }
}
