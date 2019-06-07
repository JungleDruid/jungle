package net.natruid.jungle.events;

import com.artemis.annotations.EntityId;

public class UnitHealthChangedEvent extends PooledEvent {
    @EntityId
    public int unit = -1;
    @EntityId
    public int source = -1;
    public int amount = 0;
    public boolean killed = false;

    @Override
    public void reset() {
        unit = -1;
        source = -1;
        amount = 0;
        killed = false;
    }
}
