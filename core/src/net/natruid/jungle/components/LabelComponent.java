package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

@PooledWeaver
public class LabelComponent extends Component {
    public final Color color = new Color(Color.WHITE);
    public String text = "";
    public String fontName = "normal";
    public float width = 0;
    public int align = Align.topLeft;

    protected void reset() {
        color.set(Color.WHITE);
        align = Align.topLeft;
    }
}
