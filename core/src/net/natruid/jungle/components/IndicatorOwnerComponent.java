package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.IndicatorType;
import net.natruid.jungle.utils.PathNode;

import java.util.HashMap;

@PooledWeaver
public class IndicatorOwnerComponent extends Component {
    public final HashMap<IndicatorType, PathNode[]> resultMap = new HashMap<>();
    public final HashMap<IndicatorType, int[]> indicatorMap = new HashMap<>();

    protected void reset() {
        resultMap.clear();
        indicatorMap.clear();
    }
}
