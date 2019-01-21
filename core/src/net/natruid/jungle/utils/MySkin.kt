package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.natruid.jungle.core.Data

class MySkin(skinFile: String) : Skin() {
    init {
        val file = Gdx.files.local(skinFile)

        for (f in Data.getFontDefs()) {
            add(f.name, Data.Fonts[f.name])
        }

        val atlasFile = file.sibling(file.nameWithoutExtension() + ".atlas")
        if (atlasFile.exists()) {
            addRegions(TextureAtlas(atlasFile))
        }

        load(file)
    }
}