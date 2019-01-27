/**
 * Data Manager
 */
package net.natruid.jungle.core

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.ObjectMap
import ktx.collections.GdxArray
import ktx.collections.PooledList
import ktx.collections.set
import ktx.collections.toGdxList
import ktx.freetype.generateFont
import net.natruid.jungle.utils.Scout

object Marsh {
    private val fontDefs = GdxArray<FontDef>()
    private val fontGeneratorMap = ObjectMap<String, FreeTypeFontGenerator>()
    private var fonts = ObjectMap<String, BitmapFont>()

    object Fonts {
        operator fun get(key: String): BitmapFont {
            var font = fonts[key]
            if (font == null) {
                println("[Warning] Cannot find font name: $key")
                font = fonts["normal"]
                if (font == null) {
                    error("[Error] Cannot find fallback font: normal")
                }
            }

            return font
        }
    }

    private val i18nMap = ObjectMap<String, I18NBundle>()

    object I18N {
        operator fun get(key: String): I18NBundle {
            var ret = i18nMap[key]
            if (ret == null) {
                ret = I18NBundle.createBundle(Scout[key])
                ret ?: error("[Error] Cannot find bundle $key")
                i18nMap[key] = ret
            }

            return ret
        }
    }

    fun load() {
        val jsonFiles = getFiles("assets/data/", "json", true)
        val json = Json()

        for (f in jsonFiles) {
            readJson(json, f)
        }
    }

    fun getFontDefs(): PooledList<FontDef> {
        return fontDefs.toGdxList()
    }

    private fun getFiles(path: String, ext: String, recursive: Boolean): PooledList<FileHandle> {
        val map = getFilesImpl(path, ext, recursive)
        try {
            getFilesImpl(path, ext, recursive, map, true)
        } catch (e: Exception) {
        }

        return map.values().toGdxList()
    }

    private fun getFilesImpl(
            path: String,
            ext: String,
            recursive: Boolean,
            map: ObjectMap<String, FileHandle> = ObjectMap(),
            useZip: Boolean = false
    ): ObjectMap<String, FileHandle> {
        val dir = Scout[path, useZip]

        if (!dir.isDirectory) {
            error("[Error] Marsh getFiles: $path is not a directory.")
        }

        for (f in dir.list()) {
            if (f.isDirectory && recursive) {
                getFilesImpl(f.path(), ext, true, map, useZip)
            } else if (f.extension() == ext && !map.containsKey(f.path())) {
                map[f.path()] = f
            }
        }

        return map
    }

    private fun readJson(json: Json, file: FileHandle) {
        val root = JsonReader().parse(file)
        if (!root.isObject) {
            error("[Marsh] readJson: ${file.path()} is not a json object.")
        }

        for (j in root) {
            when (j.name) {
                "fonts" -> {
                    for (fontDefJson in j) {
                        val def = json.readValue(FontDef::class.java, fontDefJson)
                        val fontPath = "assets/fonts/" + def.file
                        var generator = fontGeneratorMap[fontPath]
                        if (generator == null) {
                            generator = FreeTypeFontGenerator(Scout[fontPath])
                            fontGeneratorMap[fontPath] = generator
                        }

                        val font = generator.generateFont {
                            incremental = true
                            size = def.size
                            minFilter = Texture.TextureFilter.Linear
                            magFilter = Texture.TextureFilter.Linear
                            borderWidth = 1f
                        }

                        fonts[def.name] = font
                        fontDefs.add(def)
                    }
                }
                else -> println("[Warning] Found unknown property ${j.name} in ${file.name()}")
            }
        }
    }

    class FontDef {
        val name: String = ""
        val file: String = ""
        val size: Int = 0
    }
}