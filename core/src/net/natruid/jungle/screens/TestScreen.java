package net.natruid.jungle.screens;

import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.natruid.jungle.components.LabelComponent;
import net.natruid.jungle.components.render.PosComponent;
import net.natruid.jungle.components.render.RenderComponent;
import net.natruid.jungle.components.render.TextureComponent;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.systems.render.ImageRenderSystem;
import net.natruid.jungle.systems.render.LabelRenderSystem;

public class TestScreen extends AbstractScreen {
    public TestScreen() {
        int e = world.create();
        world.getMapper(RenderComponent.class).create(e);
        world.getMapper(PosComponent.class).create(e);
        world.getMapper(TextureComponent.class).create(e).setRegion(
            new TextureRegion(new Texture(Sky.scout.locate("assets/img/test/badlogic.jpg"))));
        {
            LabelComponent it = world.getMapper(LabelComponent.class).create(e);
            it.text = "測試 test with a long text abcdefghijklmnopqrstuvwxyz";
            it.color.set(Color.RED);
            it.width = 300f;
        }
    }

    @Override
    WorldConfigurationBuilder getConfiguration(WorldConfigurationBuilder builder) {
        return builder.with(
            new ImageRenderSystem(),
            new LabelRenderSystem()
        );
    }
}
