package net.natruid.jungle.utils.ai.actions;

import com.artemis.World;
import net.natruid.jungle.utils.ai.BehaviorAction;

public class AllAction extends BehaviorAction {
    private final BehaviorAction[] actions;

    public AllAction(BehaviorAction... actions) {
        this.actions = actions;
    }

    @Override
    public Float evaluate() {
        float score = 0;
        for (BehaviorAction action : actions) {
            Float evaluate = action.evaluate();
            if (evaluate == null) return null;
            score += evaluate;
        }
        return score;
    }

    @Override
    public boolean execute() {
        boolean result = false;
        for (BehaviorAction action : actions) {
            if (action.execute()) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void reset() {
    }

    @Override
    public void init(World world, int self) {
        super.init(world, self);
        for (BehaviorAction action : actions) {
            action.init(world, self);
        }
    }
}
