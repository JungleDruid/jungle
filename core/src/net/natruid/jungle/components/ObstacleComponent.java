package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.ObstacleType;

@PooledWeaver
public class ObstacleComponent extends Component {
    public ObstacleType type = ObstacleType.TREE;
    public boolean destroyable = false;
    public float maxHp = 0;
    public float hp = 0;

    protected void reset() {
        type = ObstacleType.TREE;
    }
}
