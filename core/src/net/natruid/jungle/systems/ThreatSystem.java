package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import net.natruid.jungle.components.AttributesComponent;
import net.natruid.jungle.components.BehaviorComponent;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.utils.types.AttributeType;

public class ThreatSystem extends BaseEntitySystem {
    private ComponentMapper<BehaviorComponent> mBehavior;
    private ComponentMapper<UnitComponent> mUnit;
    private ComponentMapper<AttributesComponent> mAttributes;
    private UnitManageSystem unitManageSystem;
    private TileSystem tileSystem;

    public ThreatSystem() {
        super(Aspect.all(BehaviorComponent.class));
    }

    public void checkAlert(int unit) {
        checkAlert(unit, -1);
    }

    public void checkAlert(int unit, int tile) {
        IntBag entityIds = getEntityIds();
        if (entityIds.isEmpty()) return;
        if (!mUnit.has(unit)) return;
        int t = (tile >= 0) ? tile : mUnit.get(unit).tile;
        int[] data = entityIds.getData();
        for (int i = 0; i < entityIds.size(); i++) {
            int u = data[i];
            if (!unitManageSystem.isEnemy(u, unit)) continue;
            if (!mBehavior.get(u).threatMap.isEmpty()) continue;
            float maxDistance = 6f * 1f + (mAttributes.get(u).get(AttributeType.AWARENESS) - 10) * 0.05f;
            if (tileSystem.getDistance(mUnit.get(u).tile, t) <= maxDistance) {
                mBehavior.get(u).threatMap.put(u, 1f);
            }
        }
    }

    @Override
    protected void processSystem() {
    }
}
