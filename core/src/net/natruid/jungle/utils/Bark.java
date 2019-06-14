package net.natruid.jungle.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.natruid.jungle.core.Marsh;
import net.natruid.jungle.core.Sky;

public class Bark extends Skin {
    public Bark(String skinPath) {
        FileHandle skin = Sky.scout.locate(skinPath);

        for (Marsh.FontDef f : Marsh.INSTANCE.getFontDefs()) {
            add(f.getName(), Marsh.Fonts.INSTANCE.get(f.getName()));
        }

        FileHandle atlasPath = skin.sibling(skin.nameWithoutExtension() + ".atlas");
        if (atlasPath.exists()) {
            addRegions(new TextureAtlas(atlasPath));
        }

        load(skin);
    }
}
