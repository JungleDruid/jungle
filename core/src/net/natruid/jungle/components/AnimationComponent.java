package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.types.AnimationType;

@PooledWeaver
public class AnimationComponent extends Component {
    @EntityId
    public int target = -1;
    public AnimationType type = null;
    public Runnable callback = null;
    public float time = 0;

    protected void reset() {
        type = null;
        callback = null;
    }
}
