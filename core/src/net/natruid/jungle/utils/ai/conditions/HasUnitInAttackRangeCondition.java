package net.natruid.jungle.utils.ai.conditions;

import com.badlogic.gdx.utils.IntArray;
import net.natruid.jungle.systems.TileSystem;
import net.natruid.jungle.utils.ai.BehaviorCondition;
import net.natruid.jungle.utils.skill.Skill;
import net.natruid.jungle.utils.types.UnitCondition;
import net.natruid.jungle.utils.types.UnitTargetType;

import java.util.ArrayList;

public class HasUnitInAttackRangeCondition extends BehaviorCondition {
    private final boolean plusMovement;
    private final int preserveAp;
    private TileSystem tileSystem;

    public HasUnitInAttackRangeCondition(boolean plusMovement, int preserveAp, boolean saveResult) {
        super(saveResult);
        this.plusMovement = plusMovement;
        this.preserveAp = preserveAp;
    }

    public HasUnitInAttackRangeCondition(boolean plusMovement) {
        this(plusMovement, 0, true);
    }

    public HasUnitInAttackRangeCondition(boolean plusMovement, int preserveAp) {
        this(plusMovement, preserveAp, true);
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean run() {
        Skill skill = mUnit.get(getSelf()).skills.get(0);
        float attackRange = unitManageSystem.getModdedValue(getSelf(), skill.range);
        int attackCost = skill.cost;
        float movement = (plusMovement)
            ? unitManageSystem.getMovement(getSelf(), attackCost + preserveAp)
            : 0f;
        ArrayList<Integer> targets = behaviorSystem.getSortedUnitList(getSelf(), UnitTargetType.HOSTILE, UnitCondition.CLOSE);
        float maxDistance = attackRange + movement;
        targets.removeIf((it) ->
            mUnit.get(it) == null || tileSystem.getDistance(mUnit.get(getSelf()).tile, mUnit.get(it).tile) > maxDistance);
        targets.removeIf((it) -> unitManageSystem.getMoveAndActPath(getSelf(), it, 2, 1f) == null);
        if (saveResult) {
            IntArray it = mBehavior.get(getSelf()).targets;
            for (int target : targets) {
                it.add(target);
            }
        }
        return !targets.isEmpty();
    }
}
