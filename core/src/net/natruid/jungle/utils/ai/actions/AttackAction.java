package net.natruid.jungle.utils.ai.actions;

import com.artemis.annotations.EntityId;
import net.natruid.jungle.events.UnitSkillEvent;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.ai.BehaviorAction;
import net.natruid.jungle.utils.skill.Skill;

import java.util.ArrayList;
import java.util.Deque;

public class AttackAction extends BehaviorAction {
    @EntityId
    private int target = -1;
    private Deque<PathNode> path = null;

    @Override
    public Float evaluate() {
        Skill skill = mUnit.get(getSelf()).skills.get(0);
        ArrayList<Integer> targets = mBehavior.get(getSelf()).targets;
        if (targets.isEmpty()) return null;
        int bestTarget = -1;
        float bestScore = Float.NaN;
        for (int target : targets) {
            float damage = 10f;
            boolean kill = mUnit.get(target).hp < damage;
            float score = behaviorSystem.getScore(kill ? "kill" : "damage", skill.cost, damage);
            if (bestTarget == -1 || score > bestScore) {
                bestTarget = target;
                bestScore = score;
            }
        }
        if (bestTarget < 0) return null;
        Deque<PathNode> path = unitManageSystem.getMoveAndActPath(getSelf(), bestTarget, 2, 1f);
        if (path == null) throw new RuntimeException("No path.");
        if (path.isEmpty()) throw new RuntimeException("Path is empty");
        target = bestTarget;
        this.path = path;
        return bestScore - path.getLast().cost / 100;
    }

    @Override
    public boolean execute() {
        {
            UnitSkillEvent it = es.dispatch(UnitSkillEvent.class);
            it.unit = getSelf();
            it.skill = 0;
            it.target = target;
            assert path != null;
            it.path = path;
        }
        return true;
    }

    @Override
    public void reset() {
        target = -1;
        path = null;
    }
}
