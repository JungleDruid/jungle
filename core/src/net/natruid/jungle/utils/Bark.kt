/**
 * Skin with TTF support
 */
package net.natruid.jungle.utils

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.natruid.jungle.core.Marsh

class Bark(skinFile: String) : Skin() {
    init {
        val file = Scout[skinFile]

        for (f in Marsh.getFontDefs()) {
            add(f.name, Marsh.Fonts[f.name])
        }

        val atlasFile = file.sibling(file.nameWithoutExtension() + ".atlas")
        if (atlasFile.exists()) {
            addRegions(TextureAtlas(atlasFile))
        }

        load(file)
    }
}