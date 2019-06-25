package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.ai.BehaviorAction;
import net.natruid.jungle.utils.ai.BehaviorTree;

@PooledWeaver
public class BehaviorComponent extends Component {
    public final IntArray targets = new IntArray();
    public final ObjectMap<Integer, Float> threatMap = new ObjectMap<>();
    public BehaviorTree tree = null;
    public PathNode[] moveArea = null;
    public PathNode[] fullMoveArea = null;
    public BehaviorAction execution = null;
    public float score = 0;

    protected void reset() {
        tree = null;
        moveArea = null;
        fullMoveArea = null;
        execution = null;
        targets.clear();
        threatMap.clear();
    }
}
