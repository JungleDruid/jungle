package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import net.natruid.jungle.utils.types.AttributeType;

@PooledWeaver
public class AttributesComponent extends Component {
    public final int[] base = new int[AttributeType.length];
    public final int[] modified = new int[AttributeType.length];
    public boolean dirty = true;

    public int get(AttributeType attribute) {
        return modified[attribute.ordinal()];
    }

    protected void reset() {
        for (int i = 0; i < AttributeType.length; i++) {
            base[i] = 10;
            modified[i] = 10;
        }
    }
}
