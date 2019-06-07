package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.ai.BehaviorAction;
import net.natruid.jungle.utils.ai.BehaviorTree;

import java.util.ArrayList;
import java.util.HashMap;

@PooledWeaver
public class BehaviorComponent extends Component {
    public final ArrayList<Integer> targets = new ArrayList<>();
    public final HashMap<Integer, Float> threatMap = new HashMap<>();
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
