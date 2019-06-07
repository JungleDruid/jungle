package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.IndicatorType;

@PooledWeaver
public class IndicatorComponent extends Component {
    @EntityId
    public int entityId = -1;
    public IndicatorType type = IndicatorType.MOVE_AREA;

    protected void reset() {
        type = IndicatorType.MOVE_AREA;
    }
}
