package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.types.IndicatorType;

@PooledWeaver
public class IndicatorOwnerComponent extends Component {
    public final ObjectMap<IndicatorType, PathNode[]> resultMap = new ObjectMap<>();
    public final ObjectMap<IndicatorType, int[]> indicatorMap = new ObjectMap<>();

    protected void reset() {
        resultMap.clear();
        indicatorMap.clear();
    }
}
