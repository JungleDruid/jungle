package net.natruid.jungle.components.render;

public class PivotComponent extends Vector2Component {
    public static final PivotComponent DEFAULT = new PivotComponent();

    public PivotComponent() {
        this(.5f, .5f);
    }

    public PivotComponent(float x, float y) {
        super(x, y);
    }

    @Override
    protected void reset() {
        set(DEFAULT);
    }
}
