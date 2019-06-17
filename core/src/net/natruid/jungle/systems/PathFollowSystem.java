package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import net.natruid.jungle.components.PathFollowerComponent;
import net.natruid.jungle.components.render.PosComponent;
import net.natruid.jungle.utils.PathNode;

import java.util.Deque;

public class PathFollowSystem extends IteratingSystem {
    private static final float SPEED = 1000f;

    private final Vector2 v = new Vector2();

    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<PathFollowerComponent> mPathFollower;
    private ThreatSystem threatSystem;
    private FlowControlSystem flowControlSystem;

    public PathFollowSystem() {
        super(Aspect.all(PosComponent.class, PathFollowerComponent.class));
    }

    @Override
    protected void inserted(int entityId) {
        super.inserted(entityId);
        flowControlSystem.addAct(entityId);
    }

    @Override
    protected void removed(int entityId) {
        super.removed(entityId);
        flowControlSystem.delAct(entityId);
    }

    @Override
    protected void process(int entityId) {
        PosComponent pos = mPos.get(entityId);
        PathFollowerComponent pathFollower = mPathFollower.get(entityId);
        Deque<PathNode> path = pathFollower.path;
        if (path == null) return;

        assert path.peek() != null;
        Vector2 destination = mPos.get(path.peek().tile).xy;
        v.set(destination);
        v.sub(pos.xy);
        float len2 = v.len2();
        if (len2 != 0f && len2 != 1f) {
            v.scl(1f / (float) Math.sqrt(len2));
        }
        v.scl(SPEED * world.delta);
        if (len2 > v.len2()) {
            v.add(pos.xy);
            pos.set(v);
        } else {
            pos.set(destination);
        }

        if (pos.xy.equals(destination)) {
            assert path.peek() != null;
            threatSystem.checkAlert(entityId, path.peek().tile);
            if (path.size() > 1) {
                path.remove();
            } else {
                Runnable callback = mPathFollower.get(entityId).callback;
                if (callback != null) callback.run();
                mPathFollower.remove(entityId);
            }
        }
    }
}
