package net.natruid.jungle.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.core.Sky;

public class FontData {
    private final Array<FontDef> fontDefs = new Array<>();
    private final ObjectMap<String, FreeTypeFontGenerator> fontGeneratorMap = new ObjectMap<>();
    private final ObjectMap<String, BitmapFont> fonts = new ObjectMap<>();

    public void readJson(Json json, JsonValue value) {
        for (JsonValue defJson : value) {
            FontDef def = json.readValue(FontDef.class, defJson);
            String fontPath = String.format("assets/fonts/%s", def.file);
            FreeTypeFontGenerator generator = fontGeneratorMap.get(fontPath);
            if (generator == null) {
                generator = new FreeTypeFontGenerator(Sky.scout.locate(fontPath));
                fontGeneratorMap.put(fontPath, generator);
            }

            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.incremental = true;
            parameter.size = def.size;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.borderWidth = 1;
            BitmapFont font = generator.generateFont(parameter);

            fontDefs.add(def);
            fonts.put(def.name, font);
        }
    }

    public Array<FontDef> getFontDefs() {
        return fontDefs;
    }

    public BitmapFont get(String name) {
        return fonts.get(name);
    }

    public static class FontDef {
        public String name = "";
        public String file = "";
        public int size = 0;
    }
}
