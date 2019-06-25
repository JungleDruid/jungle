package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.callbacks.RenderCallback;

@PooledWeaver
public class CustomRenderComponent extends Component {
    public RenderCallback renderCallback = null;

    protected void reset() {
        renderCallback = null;
    }
}
