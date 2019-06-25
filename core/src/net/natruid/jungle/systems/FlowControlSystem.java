package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import net.natruid.jungle.components.render.ActingComponent;

public class FlowControlSystem extends BaseEntitySystem {

    private ComponentMapper<ActingComponent> mActing;

    public FlowControlSystem() {
        super(Aspect.all(ActingComponent.class));
    }

    public boolean isReady() {
        return getEntityIds().isEmpty();
    }

    public void addAct(int entityId) {
        mActing.create(entityId).actions += 1;
    }

    public void delAct(int entityId) {
        ActingComponent actingComponent = mActing.get(entityId);
        actingComponent.actions -= 1;
        if (actingComponent.actions == 0) {
            mActing.remove(entityId);
        }
    }

    @Override
    protected void processSystem() {
    }
}
