package net.natruid.jungle.utils.ai;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.utils.IntArray;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.systems.PathfinderSystem;
import net.natruid.jungle.systems.UnitManageSystem;
import net.natruid.jungle.utils.PathNode;

public abstract class BehaviorLeaf extends BehaviorNode {
    protected PathfinderSystem pathfinderSystem;
    protected UnitManageSystem unitManageSystem;
    protected ComponentMapper<UnitComponent> mUnit;

    protected IntArray getTargets() {
        return mBehavior.get(getSelf()).targets;
    }

    protected PathNode[] getFullMoveArea() {
        PathNode[] moveArea = mBehavior.get(getSelf()).fullMoveArea;
        if (moveArea == null) {
            moveArea = pathfinderSystem.area(mUnit.get(getSelf()).tile, true);
            mBehavior.get(getSelf()).fullMoveArea = moveArea;
        }

        return moveArea;
    }

    protected PathNode[] getMoveArea() {
        PathNode[] moveArea = mBehavior.get(getSelf()).moveArea;
        if (moveArea == null) {
            moveArea = pathfinderSystem.area(mUnit.get(getSelf()).tile, unitManageSystem.getMovement(getSelf(), 0), true);
            mBehavior.get(getSelf()).moveArea = moveArea;
        }
        return moveArea;
    }
}
