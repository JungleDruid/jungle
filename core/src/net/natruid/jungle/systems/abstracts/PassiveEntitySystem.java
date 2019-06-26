package net.natruid.jungle.systems.abstracts;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;

public abstract class PassiveEntitySystem extends BaseEntitySystem {
    public PassiveEntitySystem(Aspect.Builder aspect) {
        super(aspect);
    }

    @Override
    protected void processSystem() {
    }

    @Override
    protected boolean checkProcessing() {
        setEnabled(false);
        return false;
    }
}
