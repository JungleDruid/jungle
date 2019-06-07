package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

@PooledWeaver
public class CircleComponent extends Component {
    public final Color color = new Color(Color.WHITE);
    public float radius = 0;
    public ShapeRenderer.ShapeType type = ShapeRenderer.ShapeType.Line;

    protected void reset() {
        radius = 0;
        color.set(Color.WHITE);
        type = ShapeRenderer.ShapeType.Line;
    }
}
