package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.math.Vector2;

@PooledWeaver
public class Vector2Component extends Component {
    public final Vector2 xy;

    public Vector2Component() {
        this(0f, 0f);
    }

    public Vector2Component(float x, float y) {
        xy = new Vector2(x, y);
    }

    public void set(float x, float y) {
        xy.set(x, y);
    }

    public void set(Vector2Component other) {
        xy.set(other.xy);
    }

    public void set(Vector2 other) {
        xy.set(other);
    }

    protected void reset() {
        xy.set(0f, 0f);
    }
}
