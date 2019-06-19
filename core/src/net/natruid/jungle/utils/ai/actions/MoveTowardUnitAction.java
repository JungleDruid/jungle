package net.natruid.jungle.utils.ai.actions;

import net.natruid.jungle.core.Sky;
import net.natruid.jungle.events.UnitMoveEvent;
import net.natruid.jungle.systems.TileSystem;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.ai.BehaviorAction;
import net.natruid.jungle.utils.types.ExtractPathType;

import java.util.Deque;

public class MoveTowardUnitAction extends BehaviorAction {
    private final int preserveAp;

    private TileSystem tileSystem;
    private Deque<PathNode> path = null;

    public MoveTowardUnitAction(int preserveAp) {
        this.preserveAp = preserveAp;
    }

    public MoveTowardUnitAction() {
        this(0);
    }

    @Override
    public Float evaluate() {
        int target = getTargets().get(0);
        assert target >= 0;
        float maxMovement = unitManageSystem.getMovement(getSelf(), preserveAp);
        Deque<PathNode> path = pathfinderSystem.path(
            mUnit.get(getSelf()).tile,
            mUnit.get(target).tile,
            true,
            getSelf(),
            ExtractPathType.CLOSEST,
            maxMovement
        );
        if (path == null || path.isEmpty()) {
//            Sky.log.debug(getSelf() + " cannot find path");
            return null;
        }
        if (path.getLast().tile == mUnit.get(getSelf()).tile) {
//            Sky.log.debug(getSelf() + " is already at the destination");
            return null;
        }
        float newDist = tileSystem.getDistance(mUnit.get(target).tile, path.getLast().tile);
        this.path = path;
        return behaviorSystem.getScore(
            "move forward",
            unitManageSystem.getMovementCost(getSelf(), path.getLast().cost, true),
            -newDist
        );
    }

    @Override
    public boolean execute() {
        Sky.log.debug(getSelf() + " moving with path size: " + path.size());
        {
            UnitMoveEvent it = es.dispatch(UnitMoveEvent.class);
            it.unit = getSelf();
            assert path != null;
            it.path = path;
        }
        return true;
    }

    @Override
    public void reset() {
        path = null;
    }
}
