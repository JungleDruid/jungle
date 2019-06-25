package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import net.natruid.jungle.components.AnimationComponent;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.components.render.PosComponent;

public class AnimateSystem extends IteratingSystem {
    private final Vector2 vector2 = new Vector2();
    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<UnitComponent> mUnit;
    private FlowControlSystem flowControlSystem;

    public AnimateSystem() {
        super(Aspect.all(UnitComponent.class, AnimationComponent.class));
    }

    private void move(int self, int target) {
        vector2.set(mPos.get(target).xy);
        vector2.sub(mPos.get(self).xy);
        vector2.nor();
        vector2.scl(400f * world.delta);
        vector2.add(mPos.get(self).xy);
        mPos.get(self).set(vector2);
    }

    @Override
    protected void inserted(int entityId) {
        super.inserted(entityId);
        //noinspection SwitchStatementWithTooFewBranches
        switch (mAnimation.get(entityId).type) {
            case ATTACK:
                flowControlSystem.addAct(entityId);
                break;
        }
    }

    @Override
    protected void removed(int entityId) {
        super.removed(entityId);
        //noinspection SwitchStatementWithTooFewBranches
        switch (mAnimation.get(entityId).type) {
            case ATTACK:
                flowControlSystem.delAct(entityId);
                break;
        }
    }

    @Override
    protected void process(int entityId) {
        AnimationComponent it = mAnimation.get(entityId);
        it.time += world.delta;
        //noinspection SwitchStatementWithTooFewBranches
        switch (it.type) {
            case ATTACK:
                if (it.time <= 0.05f) {
                    move(entityId, it.target);
                } else {
                    if (it.callback != null) {
                        it.callback.run();
                        it.callback = null;
                    }
                    int tile = mUnit.get(entityId).tile;
                    move(entityId, tile);
                    if (it.time >= 0.1f) {
                        mPos.get(entityId).set(mPos.get(tile).xy);
                        mAnimation.remove(entityId);
                    }
                }
                break;
        }
    }
}
