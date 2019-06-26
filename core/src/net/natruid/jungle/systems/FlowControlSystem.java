package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import net.natruid.jungle.components.render.ActingComponent;
import net.natruid.jungle.systems.abstracts.PassiveEntitySystem;

public class FlowControlSystem extends PassiveEntitySystem {

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
}
