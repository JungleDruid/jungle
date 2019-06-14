package net.natruid.jungle.utils.ai;

public class PrioritySelector extends BehaviorBranch {
    @Override
    public boolean run() {
        for (BehaviorNode node : children) {
            if (node.run()) {
                return true;
            }
        }
        return false;
    }
}
