package net.natruid.jungle.utils.ai;

public class SequenceSelector extends BehaviorBranch {
    @Override
    public boolean run() {
        for (BehaviorNode node : children) {
            if (!node.run()) {
                return false;
            }
        }
        return true;
    }
}
