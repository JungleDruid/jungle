package net.natruid.jungle.components.render;

public class ScaleComponent extends Vector2Component {
    public static final ScaleComponent DEFAULT = new ScaleComponent();

    public ScaleComponent() {
        super(1, 1);
    }

    public ScaleComponent(float x, float y) {
        super(x, y);
    }

    @Override
    protected void reset() {
        set(DEFAULT);
    }
}
