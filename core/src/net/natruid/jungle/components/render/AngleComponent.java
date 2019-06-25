package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class AngleComponent extends Component {
    public static final AngleComponent NONE = new AngleComponent();

    public float rotation = 0;
}
