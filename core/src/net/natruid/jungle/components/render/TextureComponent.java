package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

public class TextureComponent extends Component {
    private TextureRegion region = null;

    public Boolean flipX = false;
    public Boolean flipY = false;
    public final Color color = new Color(Color.WHITE);

    public TextureRegion getRegion() {
        return region;
    }

    public void setRegion(TextureRegion region) {
        region.getTexture().setFilter(Linear, Linear);
        this.region = region;
    }
}
