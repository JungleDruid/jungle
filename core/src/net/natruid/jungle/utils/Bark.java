package net.natruid.jungle.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.data.FontData;

public class Bark extends Skin {
    public Bark(String skinPath) {
        FileHandle skin = Sky.scout.locate(skinPath);

        for (FontData.FontDef f : Sky.marsh.font.getFontDefs()) {
            add(f.name, Sky.marsh.font.get(f.name));
        }

        FileHandle atlasPath = skin.sibling(skin.nameWithoutExtension() + ".atlas");
        if (atlasPath.exists()) {
            addRegions(new TextureAtlas(atlasPath));
        }

        load(skin);
    }
}
