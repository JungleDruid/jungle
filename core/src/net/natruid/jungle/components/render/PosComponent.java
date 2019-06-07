package net.natruid.jungle.components.render;

public class PosComponent extends Vector2Component {
    public static final PosComponent DEFAULT = new PosComponent();

    public PosComponent() {
        super();
    }

    public PosComponent(float x, float y) {
        super(x, y);
    }
}
