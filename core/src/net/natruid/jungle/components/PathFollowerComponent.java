package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.PathNode;

import java.util.Deque;

@PooledWeaver
public class PathFollowerComponent extends Component {
    public Deque<PathNode> path = null;
    public Runnable callback = null;

    protected void reset() {
        path = null;
        callback = null;
    }
}
